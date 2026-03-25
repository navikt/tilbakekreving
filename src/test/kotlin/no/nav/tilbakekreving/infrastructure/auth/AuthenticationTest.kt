package no.nav.tilbakekreving.infrastructure.auth

import arrow.core.left
import arrow.core.right
import io.kotest.assertions.ktor.client.shouldBeOK
import io.kotest.assertions.ktor.client.shouldHaveStatus
import io.kotest.core.spec.style.WordSpec
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.tilbakekreving.config.AuthenticationConfigName
import no.nav.tilbakekreving.config.EntraProxyConfig
import no.nav.tilbakekreving.domain.Krav
import no.nav.tilbakekreving.domain.Kravbeskrivelse
import no.nav.tilbakekreving.domain.Kravidentifikator
import no.nav.tilbakekreving.domain.Kravtype
import no.nav.tilbakekreving.infrastructure.auth.abac.policy.NavSaksbehandler
import no.nav.tilbakekreving.infrastructure.auth.abac.policy.lesKravAccessPolicy
import no.nav.tilbakekreving.infrastructure.auth.model.Enhetsnummer
import no.nav.tilbakekreving.infrastructure.auth.model.GroupId
import no.nav.tilbakekreving.infrastructure.auth.model.NavUserPrincipal
import no.nav.tilbakekreving.infrastructure.auth.model.OboToken
import no.nav.tilbakekreving.infrastructure.auth.model.ValidatedEntraToken
import no.nav.tilbakekreving.infrastructure.client.entra.proxy.EntraProxyClient
import no.nav.tilbakekreving.infrastructure.client.entra.proxy.HentEnheterError
import no.nav.tilbakekreving.infrastructure.unleash.StubFeatureToggle
import no.nav.tilbakekreving.setup.configureEntraAuthentication
import no.nav.tilbakekreving.util.specWideTestApplication
import org.slf4j.LoggerFactory
import java.util.Locale

class AuthenticationTest :
    WordSpec({
        val accessTokenValidator = mockk<AccessTokenValidator<ValidatedEntraToken>>()
        val navIdent = "Z123456"
        val groupIds = listOf("group1", "group2", "tilgang_til_krav").map(::GroupId).toSet()
        val authenticationConfigName = AuthenticationConfigName.ENTRA_ID
        val kravAccessPolicy =
            context(StubFeatureToggle(), LoggerFactory.getLogger(this::class.java)) {
                lesKravAccessPolicy(
                    GroupId("tilgang_til_krav"),
                    mapOf(Enhetsnummer("1111") to setOf(Kravtype("TYPE_A"))),
                )
            }

        val entraOboTokenExchanger = mockk<EntraOboTokenExchanger>()
        val entraProxyClient = mockk<EntraProxyClient>()
        val entraProxyConfig =
            EntraProxyConfig(
                baseUrl = java.net.URI("http://localhost").toURL(),
                apiTarget = "api://test/.default",
        )
        val client =
            specWideTestApplication {
                application {
                    dependencies {
                        provide<AccessTokenValidator<ValidatedEntraToken>> { accessTokenValidator }
                        provide<EntraOboTokenExchanger> { entraOboTokenExchanger }
                        provide<EntraProxyClient> { entraProxyClient }
                        provide<EntraProxyConfig> { entraProxyConfig }
                    }
                    configureEntraAuthentication()

                    routing {
                        authenticate(authenticationConfigName.configName) {
                            get("/protected") {
                                call.respond(HttpStatusCode.OK)
                            }

                            get("/protected-krav") {
                                val principal = call.principal<NavUserPrincipal>()
                                val krav =
                                    Krav(
                                        skeKravidentifikator = Kravidentifikator.Skatteetaten("skatte-123"),
                                        navKravidentifikator = Kravidentifikator.Nav("123"),
                                        navReferanse = null,
                                        kravtype = Kravtype("TYPE_A"),
                                        kravbeskrivelse = listOf(Kravbeskrivelse(Locale.forLanguageTag("nb"), "Test")),
                                        gjenståendeBeløp = 1000.0,
                                    )
                                val allowed =
                                    kravAccessPolicy
                                        .filter(
                                            NavSaksbehandler(
                                                principal?.groupIds ?: emptySet(),
                                                principal?.enheter ?: emptySet(),
                                            ),
                                            listOf(krav),
                                        ).isNotEmpty()
                                if (allowed) {
                                    call.respond(HttpStatusCode.OK)
                                } else {
                                    call.respond(HttpStatusCode.Forbidden)
                                }
                            }
                        }

                        get("/public") {
                            call.respond(HttpStatusCode.OK)
                        }
                    }
                }
            }.client

        "routes configured with authentication" should {
            "allow access to protected routes with valid token" {

                coEvery {
                    accessTokenValidator.validateToken("valid-token")
                } returns ValidatedEntraToken(navIdent, groupIds).right()
                coEvery {
                    entraOboTokenExchanger.exchange("valid-token", any())
                } returns OboToken("obo-token").right()
                coEvery {
                    entraProxyClient.hentEnheter(OboToken("obo-token"))
                } returns emptySet<Enhetsnummer>().right()

                // Public route should be accessible without a token
                client.get("/public").shouldBeOK()

                // Protected route should be accessible with a valid token
                client
                    .get("/protected") {
                        header(HttpHeaders.Authorization, "Bearer valid-token")
                    }.shouldBeOK()
            }

            "deny access to protected routes with invalid token" {
                coEvery {
                    accessTokenValidator.validateToken("invalid-token")
                } returns AccessTokenValidator.ValidationError.InvalidToken.left()

                // Protected route should be inaccessible with invalid token
                client
                    .get("/protected") {
                        header(HttpHeaders.Authorization, "Bearer invalid-token")
                    }.shouldHaveStatus(HttpStatusCode.Unauthorized)
            }

            "deny access to protected routes without token" {
                // Protected route should be inaccessible without a token
                client.get("/protected").shouldHaveStatus(HttpStatusCode.Unauthorized)
            }

            "deny access to protected routes when token verifier fails" {
                coEvery {
                    accessTokenValidator.validateToken("failing-token")
                } returns AccessTokenValidator.ValidationError.FailedToValidateToken.left()

                client
                    .get("/protected") {
                        header(HttpHeaders.Authorization, "Bearer failing-token")
                    }.shouldHaveStatus(HttpStatusCode.Unauthorized)
            }

            "deny access when OBO token exchange fails" {
                coEvery {
                    accessTokenValidator.validateToken("valid-token")
                } returns ValidatedEntraToken(navIdent, groupIds).right()
                coEvery {
                    entraOboTokenExchanger.exchange("valid-token", any())
                } returns OboTokenError.FailedToExchange.left()

                client
                    .get("/protected") {
                        header(HttpHeaders.Authorization, "Bearer valid-token")
                    }.shouldHaveStatus(HttpStatusCode.Unauthorized)
            }

            "deny access when enheter fetch fails" {
                coEvery {
                    accessTokenValidator.validateToken("valid-token")
                } returns ValidatedEntraToken(navIdent, groupIds).right()
                coEvery {
                    entraOboTokenExchanger.exchange("valid-token", any())
                } returns OboToken("obo-token").right()
                coEvery {
                    entraProxyClient.hentEnheter(OboToken("obo-token"))
                } returns HentEnheterError.FailedToFetchEnheter.left()

                client
                    .get("/protected") {
                        header(HttpHeaders.Authorization, "Bearer valid-token")
                    }.shouldHaveStatus(HttpStatusCode.Unauthorized)
            }
        }

        "routes using krav access control" should {
            "authorize access using krav access policy and user groups" {
                coEvery {
                    accessTokenValidator.validateToken("valid-token")
                } returns ValidatedEntraToken(navIdent, groupIds).right()
                coEvery {
                    entraOboTokenExchanger.exchange("valid-token", any())
                } returns OboToken("obo-token").right()
                coEvery {
                    entraProxyClient.hentEnheter(OboToken("obo-token"))
                } returns emptySet<Enhetsnummer>().right()

                client
                    .get("/protected-krav") {
                        header(HttpHeaders.Authorization, "Bearer valid-token")
                    }.shouldBeOK()
            }
        }
    })
