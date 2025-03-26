package no.nav.infrastructure.client.skatteetaten

import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.datetime.LocalDate
import no.nav.domain.Kravdetaljer
import no.nav.domain.Kravgrunnlag
import no.nav.domain.Kravlinje

class SkatteetatenInnkrevingsoppdragHttpClientTest :
    WordSpec({
        "hent kravdetaljer" should {
            "returnerere kravdetaljer når alt er ok" {
                val mockEngine =
                    MockEngine { request ->
                        request.headers.contains("Klientid", "NAV/1.0").shouldBeTrue()
                        request.headers.contains(HttpHeaders.Accept, "application/json").shouldBeTrue()

                        respond(
                            // language=json
                            content =
                                """
                                {
                                  "kravgrunnlag": {
                                    "datoNaarKravVarBesluttetHosOppdragsgiver": "2025-03-24"
                                  },
                                  "kravlinjer": [
                                    {
                                      "kravlinjetype": "testtype",
                                      "opprinneligBeloep": 0,
                                      "gjenstaaendeBeloep": 0
                                    }
                                  ]
                                }
                                """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "application/json"),
                        )
                    }

                val client =
                    HttpClient(mockEngine) {
                        install(ContentNegotiation) {
                            json()
                        }
                    }
                val skatteetatenInnkrevingsoppdragHttpClient =
                    SkatteetatenInnkrevingsoppdragHttpClient("http://localhost:8080", client)

                val result =
                    skatteetatenInnkrevingsoppdragHttpClient.hentKravdetaljer("123", "OPPDRAGSGIVERS_KRAVIDENTIFIKATOR")

                result.shouldBeRight(
                    Kravdetaljer(
                        Kravgrunnlag(LocalDate.parse("2025-03-24")),
                        listOf(
                            Kravlinje("testtype", 0.0, 0.0),
                        ),
                    ),
                )
            }
        }
    })
