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
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.auth.bearer
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.tilbakekreving.infrastructure.client.AccessTokenVerifier
import no.nav.tilbakekreving.util.specWideTestApplication

class AuthenticationTest :
    WordSpec({
        val accessTokenVerifier = mockk<AccessTokenVerifier>()
        val groupIds = (listOf("group1", "group2").map(::GroupId))
        val client =
            specWideTestApplication {
                application {
                    install(Authentication) {
                        bearer("entra-id") {
                            authenticate { credentials ->
                                accessTokenVerifier.verifyToken(credentials.token).fold(
                                    { error ->
                                        when (error) {
                                            is AccessTokenVerifier.VerificationError.FailedToVerifyToken -> null
                                            is AccessTokenVerifier.VerificationError.InvalidToken -> null
                                        }
                                    },
                                    { validatedToken ->
                                        UserGroupIdsPrincipal(validatedToken.groupIds)
                                    },
                                )
                            }
                        }
                    }

                    routing {
                        authenticate("entra-id") {
                            get("/protected") {
                                call.respond(HttpStatusCode.OK)
                            }
                        }

                        get("/public") {
                            call.respond(HttpStatusCode.OK)
                        }
                    }
                }
            }.client

        "Authentication with AccessTokenVerifier" should {
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

            "make user groups available in the authentication context" {
                coEvery {
                    accessTokenVerifier.verifyToken("valid-token")
                } returns AccessTokenVerifier.ValidatedToken(groupIds).right()

                testApplication {
                    install(Authentication) {
                        bearer("entra-id") {
                            authenticate { credentials ->
                                accessTokenVerifier.verifyToken(credentials.token).fold(
                                    { error ->
                                        when (error) {
                                            is AccessTokenVerifier.VerificationError.FailedToVerifyToken -> null
                                            is AccessTokenVerifier.VerificationError.InvalidToken -> null
                                        }
                                    },
                                    { validatedToken ->
                                        UserGroupIdsPrincipal(validatedToken.groupIds)
                                    },
                                )
                            }
                        }
                    }

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
                    }
                    // Protected route should be accessible with a valid token containing the required group
                    this.client
                        .get("/protected") {
                            header(HttpHeaders.Authorization, "Bearer valid-token")
                        }.shouldBeOK()
                }
            }
        }
    })
