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
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.tilbakekreving.infrastructure.client.AccessTokenVerifier
import no.nav.tilbakekreving.setup.configureAuthentication
import no.nav.tilbakekreving.util.specWideTestApplication

class ConfigureAuthenticationTest :
    WordSpec({
        val accessTokenVerifier = mockk<AccessTokenVerifier>()
        val groupIds = listOf(GroupId("group1"), GroupId("group2"))

        fun Application.testModule() {
            configureAuthentication(accessTokenVerifier)
            routing {
                authenticate("entra-id") {
                    get("/protected") {
                        val principal = call.authentication.principal<UserGroupIdsPrincipal>()
                        if (principal != null && principal.groupIds.contains(GroupId("group1"))) {
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

        val client =
            specWideTestApplication {
                application { testModule() }
            }.client

        "configureAuthentication" should {
            "allow access to protected routes with valid token and expose group IDs via principal" {
                coEvery { accessTokenVerifier.verifyToken("valid-token") } returns
                    AccessTokenVerifier.ValidatedToken(groupIds).right()

                // Public route is accessible without a token
                client.get("/public").shouldBeOK()

                // Protected route requires token; with valid token and group1 present returns 200
                client
                    .get("/protected") {
                        header(HttpHeaders.Authorization, "Bearer valid-token")
                    }.shouldBeOK()
            }

            "deny access to protected routes with invalid token" {
                coEvery { accessTokenVerifier.verifyToken("invalid-token") } returns
                    AccessTokenVerifier.VerificationError.InvalidToken.left()

                client
                    .get("/protected") {
                        header(HttpHeaders.Authorization, "Bearer invalid-token")
                    }.shouldHaveStatus(HttpStatusCode.Unauthorized)
            }

            "deny access to protected routes without token" {
                client.get("/protected").shouldHaveStatus(HttpStatusCode.Unauthorized)
            }

            "deny access to protected routes when token verifier fails" {
                coEvery { accessTokenVerifier.verifyToken("failing-token") } returns
                    AccessTokenVerifier.VerificationError.FailedToVerifyToken.left()

                client
                    .get("/protected") {
                        header(HttpHeaders.Authorization, "Bearer failing-token")
                    }.shouldHaveStatus(HttpStatusCode.Unauthorized)
            }
        }
    })
