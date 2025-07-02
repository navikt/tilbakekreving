package no.nav.tilbakekreving.infrastructure.client.skatteetaten

import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.datetime.LocalDate
import no.nav.tilbakekreving.AppEnv
import no.nav.tilbakekreving.domain.Krav
import no.nav.tilbakekreving.domain.Kravdetaljer
import no.nav.tilbakekreving.domain.Kravgrunnlag
import no.nav.tilbakekreving.domain.Kravidentifikator
import no.nav.tilbakekreving.domain.Kravlinje
import no.nav.tilbakekreving.domain.Skyldner
import no.nav.tilbakekreving.infrastructure.client.skatteetaten.json.KravidentifikatortypeQuery
import no.nav.tilbakekreving.setup.createHttpClient

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

                val client = createHttpClient(mockEngine, AppEnv.DEV)
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

        "hent kravoversikt" should {
            "returnere kravoversikt når alt er ok" {
                val mockEngine =
                    MockEngine { request ->
                        request.headers.contains("Klientid", "NAV/2.0").shouldBeTrue()
                        request.headers.contains(HttpHeaders.Accept, "application/json").shouldBeTrue()
                        request.body.contentType
                            .shouldNotBeNull()
                            .shouldBeEqual(ContentType.Application.Json)
                        request.body.toByteArray().decodeToString().shouldEqualJson(
                            // language=json
                            """
                            {
                                "skyldner": {
                                  "foedselsnummer": "12345678901"
                                }
                            }
                            """.trimIndent(),
                        )

                        respond(
                            // language=json
                            content =
                                """
                                {
                                    "krav": [
                                        {
                                        "kravidentifikator": "29ab06ef-9de1-4312-9677-163e4cc586dd",
                                        "oppdragsgiverKravidentifikator": "310ade77-8d45-48e8-b053-72659f53b4eb",
                                        "kravtype": "OB04",
                                        "kravbeskrivelse": {
                                            "spraakTekst": [
                                            {
                                                "tekst": "Eksempeltekst",
                                                "spraak": "NB"
                                            }
                                            ]
                                        }
                                        }
                                    ]
                                }
                                """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "application/json"),
                        )
                    }

                val client = createHttpClient(mockEngine, AppEnv.DEV)
                val skatteetatenInnkrevingsoppdragHttpClient =
                    SkatteetatenInnkrevingsoppdragHttpClient("http://localhost:8080", client)

                val result =
                    skatteetatenInnkrevingsoppdragHttpClient.hentKravoversikt(
                        Skyldner.Fødselnummer("12345678901"),
                    )

                result
                    .shouldBeRight()
                    .shouldContain(Krav(Kravidentifikator.Nav("310ade77-8d45-48e8-b053-72659f53b4eb"), "OB04"))
            }
        }
    })
