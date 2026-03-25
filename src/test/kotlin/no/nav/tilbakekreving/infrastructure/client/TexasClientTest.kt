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
import no.nav.tilbakekreving.config.NaisConfig
import no.nav.tilbakekreving.infrastructure.client.texas.TexasClient
import no.nav.tilbakekreving.infrastructure.client.texas.TexasError
import no.nav.tilbakekreving.infrastructure.client.texas.json.IdentityProviderJson
import no.nav.tilbakekreving.infrastructure.client.texas.json.ValidateTokenResponse
import no.nav.tilbakekreving.setup.createHttpClient
import java.net.URI

class TexasClientTest :
    WordSpec({
        val naisConfig =
            NaisConfig(
                URI("http://localhost").toURL(),
                URI("http://localhost").toURL(),
                URI("http://localhost").toURL(),
            )

        "introspectToken" should {
            "return ValidTokenResponse when token is active" {
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
                val texasClient = TexasClient(httpClient, naisConfig)

                val result = texasClient.introspectToken("azuread", "valid-token")

                val response = result.shouldBeRight()
                response.shouldBeTypeOf<ValidateTokenResponse.ValidTokenResponse>()
                response.NAVident.shouldBeEqual("Z123456")
                response.groups.shouldBeEqual(listOf("group1", "group2", "group3"))
            }

            "return InvalidTokenResponse when token is inactive" {
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
                val texasClient = TexasClient(httpClient, naisConfig)

                val result = texasClient.introspectToken("azuread", "invalid-token")

                val response = result.shouldBeRight()
                response.shouldBeTypeOf<ValidateTokenResponse.InvalidTokenResponse>()
                response.error.shouldBeEqual("token is expired")
            }

            "return RequestFailed when introspection fails with non-2xx status" {
                val mockEngine =
                    MockEngine { _ ->
                        respondError(HttpStatusCode.Unauthorized)
                    }

                val httpClient = with(AppEnv.DEV) { createHttpClient(mockEngine) }
                val texasClient = TexasClient(httpClient, naisConfig)

                val result = texasClient.introspectToken("azuread", "invalid-token")

                result.shouldBeLeft(TexasError.RequestFailed)
            }
        }

        "exchangeToken" should {
            "return TexasTokenResponse when exchange is successful" {
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
                                "identity_provider": "entra_id",
                                "skip_cache": false,
                                "target": "api://target-app/.default",
                                "user_token": "user-token-123"
                            }
                            """.trimIndent(),
                        )

                        respond(
                            // language=json
                            """
                            {
                                "access_token": "exchanged-obo-token",
                                "expires_in": 3600,
                                "token_type": "Bearer"
                            }
                            """.trimIndent(),
                            headers = headersOf("Content-Type" to listOf("application/json")),
                        )
                    }

                val httpClient = with(AppEnv.DEV) { createHttpClient(mockEngine) }
                val texasClient = TexasClient(httpClient, naisConfig)

                val result =
                    texasClient.exchangeToken(
                        identityProvider = IdentityProviderJson.ENTRA_ID,
                        target = "api://target-app/.default",
                        userToken = "user-token-123",
                    )

                val response = result.shouldBeRight()
                response.accessToken.shouldBeEqual("exchanged-obo-token")
            }

            "return RequestFailed when exchange responds with non-200 status" {
                val mockEngine =
                    MockEngine { _ ->
                        respondError(HttpStatusCode.Unauthorized)
                    }

                val httpClient = with(AppEnv.DEV) { createHttpClient(mockEngine) }
                val texasClient = TexasClient(httpClient, naisConfig)

                val result =
                    texasClient.exchangeToken(
                        identityProvider = IdentityProviderJson.ENTRA_ID,
                        target = "api://target",
                        userToken = "user-token",
                    )

                result.shouldBeLeft(TexasError.RequestFailed)
            }

            "return RequestFailed when exchange responds with 500" {
                val mockEngine =
                    MockEngine { _ ->
                        respondError(HttpStatusCode.InternalServerError)
                    }

                val httpClient = with(AppEnv.DEV) { createHttpClient(mockEngine) }
                val texasClient = TexasClient(httpClient, naisConfig)

                val result =
                    texasClient.exchangeToken(
                        identityProvider = IdentityProviderJson.ENTRA_ID,
                        target = "api://target",
                        userToken = "user-token",
                    )

                result.shouldBeLeft(TexasError.RequestFailed)
            }
        }

        "getToken" should {
            "return TexasTokenResponse when token acquisition is successful" {
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
                                "identity_provider": "maskinporten",
                                "target": "scope1 scope2"
                            }
                            """.trimIndent(),
                        )

                        respond(
                            // language=json
                            """
                            {
                                "access_token": "token",
                                "expires_in": 3600,
                                "token_type": "Bearer"
                            }
                            """.trimIndent(),
                            headers = headersOf("Content-Type" to listOf("application/json")),
                        )
                    }

                val httpClient = with(AppEnv.DEV) { createHttpClient(mockEngine) }
                val texasClient = TexasClient(httpClient, naisConfig)

                val result = texasClient.getToken(IdentityProviderJson.MASKINPORTEN, "scope1 scope2")

                val response = result.shouldBeRight()
                response.accessToken.shouldBeEqual("token")
            }

            "return RequestFailed when token acquisition fails" {
                val mockEngine =
                    MockEngine { _ ->
                        respondError(HttpStatusCode.InternalServerError)
                    }

                val httpClient = with(AppEnv.DEV) { createHttpClient(mockEngine) }
                val texasClient = TexasClient(httpClient, naisConfig)

                val result = texasClient.getToken(IdentityProviderJson.MASKINPORTEN, "scope1")

                result.shouldBeLeft(TexasError.RequestFailed)
            }
        }
    })
