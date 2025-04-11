package no.nav.tilbakekreving.infrastructure.client.maskinporten

import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.types.shouldBeTypeOf
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import no.nav.tilbakekreving.AppEnv
import no.nav.tilbakekreving.setup.createHttpClient

class TexasMaskinportenClientTest :
    WordSpec({
        "hent access token" should {
            "returnere access token når alt er ok" {
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

                val httpClient = createHttpClient(mockEngine, AppEnv.DEV)
                val texasClient = TexasMaskinportenClient(httpClient, "http://localhost")

                val result = texasClient.getAccessToken("scope1", "scope2")

                result.shouldBeRight("token")
            }
        }
    })
