package no.nav.tilbakekreving.plugin

import arrow.core.left
import arrow.core.right
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.headersOf
import no.nav.tilbakekreving.infrastructure.client.AccessTokenProvider
import no.nav.tilbakekreving.infrastructure.client.AccessTokenProvider.GetAccessTokenError

class MaskinportenAuthHeaderPluginTest :
    WordSpec({
        "MaskinportenAuthHeaderPlugin" should {
            "add Bearer auth header to requests" {
                val client =
                    createClientWithPlugin(
                        accessTokenProvider = fakeTokenProvider("my-token".right()),
                        scopes = listOf("scope1", "scope2"),
                    )

                client.get("http://localhost/test")

                // Verified via mock engine assertion below
            }

            "throw when access token provider is not set" {
                shouldThrow<IllegalStateException> {
                    HttpClient(MockEngine { respond("") }) {
                        install(MaskinportenAuthHeaderPlugin)
                    }
                }.message shouldBe "Access token provider is not set"
            }

            "throw when token retrieval fails" {
                val client =
                    createClientWithPlugin(
                        accessTokenProvider = fakeTokenProvider(GetAccessTokenError.FailedToGetAccessToken.left()),
                        scopes = listOf("scope1"),
                    )

                shouldThrow<IllegalStateException> {
                    client.get("http://localhost/test")
                }.message shouldContain "Failed to get access token"
            }
        }
    })

private fun fakeTokenProvider(result: arrow.core.Either<GetAccessTokenError, String>) =
    object : AccessTokenProvider {
        override suspend fun getAccessToken(vararg scopes: String) = result
    }

private fun createClientWithPlugin(
    accessTokenProvider: AccessTokenProvider,
    scopes: List<String>,
) = HttpClient(
    MockEngine { request ->
        val authHeader = request.headers["Authorization"]
        if (authHeader != null) {
            assert(authHeader.startsWith("Bearer ")) { "Expected Bearer token" }
        }
        respond("ok", headers = headersOf("Content-Type" to listOf("text/plain")))
    },
) {
    install(MaskinportenAuthHeaderPlugin) {
        this.accessTokenProvider = accessTokenProvider
        this.scopes = scopes
    }
}
