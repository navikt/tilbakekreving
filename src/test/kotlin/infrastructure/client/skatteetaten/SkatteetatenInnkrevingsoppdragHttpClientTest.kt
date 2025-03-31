package no.nav.infrastructure.client.skatteetaten

import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContain
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.datetime.LocalDate
import no.nav.setup.createHttpClient
import no.nav.domain.Kravdetaljer
import no.nav.domain.Kravgrunnlag
import no.nav.domain.Kravidentifikator
import no.nav.domain.Kravlinje
import no.nav.infrastructure.client.skatteetaten.json.KravidentifikatortypeQuery

class SkatteetatenInnkrevingsoppdragHttpClientTest :
    WordSpec({
        "hent kravdetaljer" should {
            "returnerere kravdetaljer når alt er ok" {
                val kravidentifikator = "123"
                val kravdetaljer =
                    Kravdetaljer(
                        Kravgrunnlag(LocalDate.parse("2025-03-24")),
                        listOf(
                            Kravlinje("testtype", 100.0, 50.0),
                        ),
                    )
                val mockEngine =
                    MockEngine { request ->
                        request.headers.contains("Klientid", "NAV/2.0").shouldBeTrue()
                        request.headers.contains(HttpHeaders.Accept, "application/json").shouldBeTrue()
                        request.url.segments.shouldContain(kravidentifikator)
                        request.url.parameters
                            .contains(
                                "kravidentifikatortype",
                                KravidentifikatortypeQuery.OPPDRAGSGIVERS_KRAVIDENTIFIKATOR.name,
                            ).shouldBeTrue()

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
                                      "opprinneligBeloep": 100,
                                      "gjenstaaendeBeloep": 50
                                    }
                                  ]
                                }
                                """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "application/json"),
                        )
                    }

                val client = createHttpClient(mockEngine)
                val skatteetatenInnkrevingsoppdragHttpClient =
                    SkatteetatenInnkrevingsoppdragHttpClient("http://localhost:8080", client)

                val result =
                    skatteetatenInnkrevingsoppdragHttpClient.hentKravdetaljer(
                        Kravidentifikator.Nav(
                            kravidentifikator,
                        ),
                    )

                result.shouldBeRight(kravdetaljer)
            }
        }
    })
