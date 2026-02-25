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
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.tilbakekreving.config.AuthenticationConfigName
import no.nav.tilbakekreving.domain.Krav
import no.nav.tilbakekreving.domain.Kravbeskrivelse
import no.nav.tilbakekreving.domain.Kravidentifikator
import no.nav.tilbakekreving.domain.Kravtype
import no.nav.tilbakekreving.infrastructure.auth.abac.policy.KravAccessSubject
import no.nav.tilbakekreving.infrastructure.auth.abac.policy.lesKravAccessPolicy
import no.nav.tilbakekreving.infrastructure.client.AccessTokenVerifier
import no.nav.tilbakekreving.infrastructure.route.util.navUserPrincipal
import no.nav.tilbakekreving.infrastructure.unleash.StubFeatureToggles
import no.nav.tilbakekreving.setup.configureAuthentication
import no.nav.tilbakekreving.util.specWideTestApplication
import java.util.Locale

class AuthenticationTest :
    WordSpec({
        val accessTokenVerifier = mockk<AccessTokenVerifier<NavUserPrincipal>>()
        val navIdent = "Z123456"
        val groupIds = (listOf("group1", "group2", "tilgang_til_krav").map(::GroupId))
        val authenticationConfigName = AuthenticationConfigName.ENTRA_ID
        val kravAccessPolicy =
            context(StubFeatureToggles()) {
                lesKravAccessPolicy(
                    GroupId("tilgang_til_krav"),
                    mapOf(Kravtype("TYPE_A") to setOf(GroupId("group1"))),
                )
            }
        val client =
            specWideTestApplication {
                application {
                    configureAuthentication(authenticationConfigName, accessTokenVerifier)

                    routing {
                        authenticate(authenticationConfigName.configName) {
                            get("/protected") {
                                call.respond(HttpStatusCode.OK)
                            }

                            get("/protected-krav") {
                                val principal = navUserPrincipal()
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
                                            KravAccessSubject(principal?.groupIds?.toSet() ?: emptySet()),
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
                    accessTokenVerifier.verifyToken("valid-token")
                } returns NavUserPrincipal(navIdent, groupIds).right()

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
                    accessTokenVerifier.verifyToken("invalid-token")
                } returns AccessTokenVerifier.VerificationError.InvalidToken.left()

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
                    accessTokenVerifier.verifyToken("failing-token")
                } returns AccessTokenVerifier.VerificationError.FailedToVerifyToken.left()

                client
                    .get("/protected") {
                        header(HttpHeaders.Authorization, "Bearer failing-token")
                    }.shouldHaveStatus(HttpStatusCode.Unauthorized)
            }
        }

        "routes using krav access control" should {
            "authorize access using krav access policy and user groups" {
                coEvery {
                    accessTokenVerifier.verifyToken("valid-token")
                } returns NavUserPrincipal(navIdent, groupIds).right()

                client
                    .get("/protected-krav") {
                        header(HttpHeaders.Authorization, "Bearer valid-token")
                    }.shouldBeOK()
            }
        }
    })
