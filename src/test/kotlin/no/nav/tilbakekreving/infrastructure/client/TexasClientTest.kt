package no.nav.tilbakekreving.infrastructure.client

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.types.shouldBeTypeOf
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import no.nav.tilbakekreving.AppEnv
import no.nav.tilbakekreving.infrastructure.auth.GroupId
import no.nav.tilbakekreving.infrastructure.auth.NavUserPrincipal
import no.nav.tilbakekreving.setup.createHttpClient

class TexasClientTest :
    WordSpec({
        "verifyToken" should {
            "return NavUserPrincipal when token verification is successful" {
                val mockEngine =
                    MockEngine { request ->
                        request.body.contentType
                            .shouldNotBeNull()
                            .toString()
                            .shouldBeEqual("application/json")
                        request.headers.contains("Accept", "application/json").shouldBeTrue()
                        val textContent = request.body.shouldBeTypeOf<TextContent>()
                        textContent.text.shouldEqualJson(
                            // language=json
                            """
                            {
                                "identity_provider": "azuread",
                                "token": "valid-token"
                            }
                            """.trimIndent(),
                        )

                        respond(
                            // language=json
                            """
                            {
                                "active": true,
                                "exp": 1609459200,
                                "iat": 1609455600,
                                "NAVident": "Z123456",
                                "groups": ["group1", "group2", "group3"]
                            }
                            """.trimIndent(),
                            headers = headersOf("Content-Type" to listOf("application/json")),
                        )
                    }

                val httpClient = with(AppEnv.DEV) { createHttpClient(mockEngine) }
                val texasClient = TexasClient(httpClient, "http://localhost")

                val result = texasClient.verifyToken("valid-token")

                result.shouldBeRight(
                    NavUserPrincipal(
                        "Z123456",
                        listOf(
                            "group1",
                            "group2",
                            "group3",
                        ).map(::GroupId).toSet(),
                    ),
                )
            }

            "return VerificationError.InvalidToken when token is invalid but response is 200" {
                val mockEngine =
                    MockEngine { request ->
                        request.body.contentType
                            .shouldNotBeNull()
                            .toString()
                            .shouldBeEqual("application/json")
                        request.headers.contains("Accept", "application/json").shouldBeTrue()
                        val textContent = request.body.shouldBeTypeOf<TextContent>()
                        textContent.text.shouldEqualJson(
                            // language=json
                            """
                            {
                                "identity_provider": "azuread",
                                "token": "invalid-token"
                            }
                            """.trimIndent(),
                        )

                        respond(
                            // language=json
                            """
                            {
                                "active": false,
                                "error": "token is expired"
                            }
                            """.trimIndent(),
                            headers = headersOf("Content-Type" to listOf("application/json")),
                        )
                    }

                val httpClient = with(AppEnv.DEV) { createHttpClient(mockEngine) }
                val texasClient = TexasClient(httpClient, "http://localhost")

                val result = texasClient.verifyToken("invalid-token")

                result.shouldBeLeft(AccessTokenVerifier.VerificationError.InvalidToken)
            }

            "return FailedToVerifyToken when token verification fails with non-2xx status code" {
                val mockEngine =
                    MockEngine { _ ->
                        respondError(HttpStatusCode.Unauthorized)
                    }

                val httpClient = with(AppEnv.DEV) { createHttpClient(mockEngine) }
                val texasClient = TexasClient(httpClient, "http://localhost")

                val result = texasClient.verifyToken("invalid-token")

                result.shouldBeLeft(AccessTokenVerifier.VerificationError.FailedToVerifyToken)
            }
        }
    })
