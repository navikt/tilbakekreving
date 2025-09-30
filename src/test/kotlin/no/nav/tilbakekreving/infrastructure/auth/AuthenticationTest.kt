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
import io.ktor.server.auth.authentication
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.tilbakekreving.domain.Krav
import no.nav.tilbakekreving.domain.Kravidentifikator
import no.nav.tilbakekreving.domain.KravoversiktKravgrunnlag
import no.nav.tilbakekreving.domain.Kravtype
import no.nav.tilbakekreving.domain.MultiSpråkTekst
import no.nav.tilbakekreving.domain.SpråkTekst
import no.nav.tilbakekreving.infrastructure.client.AccessTokenVerifier
import no.nav.tilbakekreving.infrastructure.route.KravAccessControl
import no.nav.tilbakekreving.setup.AuthenticationConfigName
import no.nav.tilbakekreving.setup.configureAuthentication
import no.nav.tilbakekreving.util.specWideTestApplication

class AuthenticationTest :
    WordSpec({
        val accessTokenVerifier = mockk<AccessTokenVerifier>()
        val groupIds = (listOf("group1", "group2", "tilgang_til_krav").map(::GroupId))
        val authenticationConfigName = AuthenticationConfigName("entra-id")
        val kravAccessControl =
            KravAccessControl(
                mapOf(Kravtype("TYPE_A") to setOf(GroupId("group1"))),
                GroupId("tilgang_til_krav"),
            )
        val client =
            specWideTestApplication {
                application {
                    configureAuthentication(authenticationConfigName, accessTokenVerifier)

                    routing {
                        authenticate(authenticationConfigName.name) {
                            get("/protected") {
                                call.respond(HttpStatusCode.OK)
                            }

                            get("/protected-krav") {
                                val principal = call.authentication.principal<UserGroupIdsPrincipal>()
                                val krav =
                                    Krav(
                                        kravidentifikator = Kravidentifikator.Nav("123"),
                                        kravtype = Kravtype("TYPE_A"),
                                        kravbeskrivelse = MultiSpråkTekst(listOf(SpråkTekst("Test", "nb"))),
                                        kravgrunnlag = KravoversiktKravgrunnlag("123", null),
                                        gjenståendeBeløp = 1000.0,
                                    )
                                val allowed =
                                    kravAccessControl.isKravAccessibleTo(principal?.groupIds?.toSet() ?: emptySet())(
                                        krav,
                                    )
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
                } returns AccessTokenVerifier.ValidatedToken(groupIds).right()

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
            "authorize access using KravAccessControl and user groups" {
                coEvery {
                    accessTokenVerifier.verifyToken("valid-token")
                } returns AccessTokenVerifier.ValidatedToken(groupIds).right()

                client
                    .get("/protected-krav") {
                        header(HttpHeaders.Authorization, "Bearer valid-token")
                    }.shouldBeOK()
            }
        }
    })
