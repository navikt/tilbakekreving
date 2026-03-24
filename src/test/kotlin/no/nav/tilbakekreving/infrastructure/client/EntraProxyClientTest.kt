package no.nav.tilbakekreving.infrastructure.client

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import no.nav.tilbakekreving.AppEnv
import no.nav.tilbakekreving.infrastructure.auth.model.Enhetsnummer
import no.nav.tilbakekreving.infrastructure.auth.model.OboToken
import no.nav.tilbakekreving.infrastructure.client.entra.proxy.EntraProxyClient
import no.nav.tilbakekreving.infrastructure.client.entra.proxy.HentEnheterError
import no.nav.tilbakekreving.setup.createHttpClient
import java.net.URI

class EntraProxyClientTest :
    WordSpec({
        "hentEnheter" should {
            "return list of enheter when response is 200 OK" {
                val mockEngine =
                    MockEngine { request ->
                        request.method.shouldBeEqual(HttpMethod.Get)
                        request.url.encodedPath.shouldBeEqual("/api/v1/enhet")

                        respond(
                            // language=json
                            """
                            [
                                { "enhetsnummer": "1234", "navn": "NAV Arbeid og ytelser" },
                                { "enhetsnummer": "5678", "navn": "NAV Klageinstans" }
                            ]
                            """.trimIndent(),
                            headers = headersOf("Content-Type" to listOf("application/json")),
                        )
                    }

                val httpClient = with(AppEnv.DEV) { createHttpClient(mockEngine) }
                val client = EntraProxyClient(httpClient, URI("http://localhost").toURL())

                val result = client.hentEnheter(OboToken("oboToken"))

                result.shouldBeRight(
                    setOf(
                        Enhetsnummer("1234"),
                        Enhetsnummer("5678"),
                    ),
                )
            }

            "return empty list when response is 200 OK with no enheter" {
                val mockEngine =
                    MockEngine { _ ->
                        respond(
                            // language=json
                            "[]",
                            headers = headersOf("Content-Type" to listOf("application/json")),
                        )
                    }

                val httpClient = with(AppEnv.DEV) { createHttpClient(mockEngine) }
                val client = EntraProxyClient(httpClient, URI("http://localhost").toURL())

                val result = client.hentEnheter(OboToken("oboToken"))

                result.shouldBeRight(emptySet())
            }

            "return HentEnheterError.Unknown when response is 500 Internal Server Error" {
                val mockEngine =
                    MockEngine { _ ->
                        respondError(HttpStatusCode.InternalServerError)
                    }

                val httpClient = with(AppEnv.DEV) { createHttpClient(mockEngine) }
                val client = EntraProxyClient(httpClient, URI("http://localhost").toURL())

                val result = client.hentEnheter(OboToken("oboToken"))

                result.shouldBeLeft(HentEnheterError.FailedToFetchEnheter)
            }

            "return HentEnheterError.Unknown when response is 404 Not Found" {
                val mockEngine =
                    MockEngine { _ ->
                        respondError(HttpStatusCode.NotFound)
                    }

                val httpClient = with(AppEnv.DEV) { createHttpClient(mockEngine) }
                val client = EntraProxyClient(httpClient, URI("http://localhost").toURL())

                val result = client.hentEnheter(OboToken("oboToken"))

                result.shouldBeLeft(HentEnheterError.FailedToFetchEnheter)
            }
        }
    })
