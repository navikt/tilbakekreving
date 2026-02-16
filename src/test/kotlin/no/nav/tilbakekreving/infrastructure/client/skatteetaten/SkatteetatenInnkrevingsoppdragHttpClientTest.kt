package no.nav.tilbakekreving.infrastructure.client.skatteetaten

import io.kotest.assertions.arrow.core.shouldBeLeft
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
import no.nav.tilbakekreving.app.HentKravdetaljer
import no.nav.tilbakekreving.app.SøkEtterInnkrevingskrav
import no.nav.tilbakekreving.domain.Krav
import no.nav.tilbakekreving.domain.KravDetalj
import no.nav.tilbakekreving.domain.Kravbeskrivelse
import no.nav.tilbakekreving.domain.Kravdetaljer
import no.nav.tilbakekreving.domain.KravdetaljerSkyldner
import no.nav.tilbakekreving.domain.Kravfilter
import no.nav.tilbakekreving.domain.Kravgrunnlag
import no.nav.tilbakekreving.domain.Kravidentifikator
import no.nav.tilbakekreving.domain.Kravlinje
import no.nav.tilbakekreving.domain.Kravtype
import no.nav.tilbakekreving.domain.Oppdragsgiver
import no.nav.tilbakekreving.domain.Skyldner
import no.nav.tilbakekreving.domain.SkyldnerId
import no.nav.tilbakekreving.domain.Skyldnersøk
import no.nav.tilbakekreving.setup.createHttpClient
import java.util.Locale

class SkatteetatenInnkrevingsoppdragHttpClientTest :
    WordSpec({
        "hent kravdetaljer" should {
            "returnerere kravdetaljer når alt er ok" {
                val kravidentifikator = "123"
                val kravdetaljer =
                    Kravdetaljer(
                        krav =
                            KravDetalj(
                                forfallsdato = LocalDate.parse("2025-01-01"),
                                foreldelsesdato = LocalDate.parse("2030-01-01"),
                                fastsettelsesdato = LocalDate.parse("2024-01-01"),
                                kravtype = "OB04",
                                `opprinneligBeløp` = 100.0,
                                `gjenståendeBeløp` = 50.0,
                                skatteetatensKravidentifikator = "skatte-123",
                                kravlinjer =
                                    listOf(
                                        Kravlinje("testtype", 100.0, 50.0, emptyMap()),
                                    ),
                                kravgrunnlag = Kravgrunnlag("123", "ref-123"),
                            ),
                        oppdragsgiver = Oppdragsgiver("123456789", "Test Org"),
                        skyldner = KravdetaljerSkyldner("12345678901", "Test Person"),
                    )
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
                                "krav": {
                                    "oppdragsgiversKravidentifikator": "123"
                                }
                            }
                            """.trimIndent(),
                        )

                        respond(
                            // language=json
                            content =
                                """
                                {
                                  "krav": {
                                    "forfallsdato": "2025-01-01",
                                    "foreldelsesdato": "2030-01-01",
                                    "fastsettelsesdato": "2024-01-01",
                                    "kravtype": "OB04",
                                    "opprinneligBeloep": 100.0,
                                    "gjenstaaendeBeloep": 50.0,
                                    "skatteetatensKravidentifikator": "skatte-123",
                                    "kravlinjer": [
                                      {
                                        "kravlinjetype": "testtype",
                                        "opprinneligBeloep": 100,
                                        "gjenstaaendeBeloep": 50
                                      }
                                    ],
                                    "kravgrunnlag": {
                                      "oppdragsgiversKravidentifikator": "123",
                                      "oppdragsgiversReferanse": "ref-123"
                                    }
                                  },
                                  "oppdragsgiver": {
                                    "organisasjonsnummer": "123456789",
                                    "organisasjonsnavn": "Test Org"
                                  },
                                  "skyldner": {
                                    "identifikator": "12345678901",
                                    "skyldnersNavn": "Test Person"
                                  }
                                }
                                """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "application/json"),
                        )
                    }

                val client = with(AppEnv.DEV) { createHttpClient(mockEngine) }
                val skatteetatenInnkrevingsoppdragHttpClient =
                    SkatteetatenInnkrevingsoppdragHttpClient("http://localhost:8080", client)

                val result =
                    skatteetatenInnkrevingsoppdragHttpClient.hentKravdetaljer(
                        Kravidentifikator.Nav(kravidentifikator),
                    )

                result.shouldBeRight(kravdetaljer)
            }

            "returnere FantIkkeKravdetaljer ved 404" {
                val mockEngine =
                    MockEngine {
                        respond(
                            content = "",
                            status = HttpStatusCode.NotFound,
                            headers = headersOf(HttpHeaders.ContentType, "application/json"),
                        )
                    }

                val client = with(AppEnv.DEV) { createHttpClient(mockEngine) }
                val skatteetatenClient = SkatteetatenInnkrevingsoppdragHttpClient("http://localhost:8080", client)

                val result = skatteetatenClient.hentKravdetaljer(Kravidentifikator.Nav("unknown"))

                result.shouldBeLeft(HentKravdetaljer.HentKravdetaljerFeil.FantIkkeKravdetaljer)
            }

            "returnere FeilVedHentingAvKravdetaljer ved 500" {
                val mockEngine =
                    MockEngine {
                        respond(
                            content = """{"error": "server error"}""",
                            status = HttpStatusCode.InternalServerError,
                            headers = headersOf(HttpHeaders.ContentType, "application/json"),
                        )
                    }

                val client = with(AppEnv.DEV) { createHttpClient(mockEngine) }
                val skatteetatenClient = SkatteetatenInnkrevingsoppdragHttpClient("http://localhost:8080", client)

                val result = skatteetatenClient.hentKravdetaljer(Kravidentifikator.Nav("123"))

                result.shouldBeLeft(HentKravdetaljer.HentKravdetaljerFeil.FeilVedHentingAvKravdetaljer)
            }

            "returnere FeilVedHentingAvKravdetaljer ved 400" {
                val mockEngine =
                    MockEngine {
                        respond(
                            content = """{"error": "bad request"}""",
                            status = HttpStatusCode.BadRequest,
                            headers = headersOf(HttpHeaders.ContentType, "application/json"),
                        )
                    }

                val client = with(AppEnv.DEV) { createHttpClient(mockEngine) }
                val skatteetatenClient = SkatteetatenInnkrevingsoppdragHttpClient("http://localhost:8080", client)

                val result = skatteetatenClient.hentKravdetaljer(Kravidentifikator.Skatteetaten("123"))

                result.shouldBeLeft(HentKravdetaljer.HentKravdetaljerFeil.FeilVedHentingAvKravdetaljer)
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
                                "skyldner": "12345678901",
                                "kravfilter": "alle"
                            }
                            """.trimIndent(),
                        )

                        respond(
                            // language=json
                            content =
                                """
                                {
                                  "oppdragsgiver": {
                                    "organisasjonsnummer": "123456789",
                                    "organisasjonsnavn": "Test Org"
                                  },
                                  "krav": [
                                    {
                                      "skatteetatensKravidentifikator": "29ab06ef-9de1-4312-9677-163e4cc586dd",
                                      "kravtype": "OB04",
                                      "kravbeskrivelse": {
                                        "spraakTekst": [
                                          {
                                            "tekst": "Eksempeltekst",
                                            "spraak": "NB"
                                          }
                                        ]
                                      },
                                      "kravgrunnlag": {
                                        "oppdragsgiversKravidentifikator": "310ade77-8d45-48e8-b053-72659f53b4eb",
                                        "oppdragsgiversReferanse": "ref1"
                                      },
                                      "gjenstaaendeBeloep": 1000.0
                                    }
                                  ],
                                  "gjenstaaendeBeloepForSkyldner": 1000.0,
                                  "skyldner": {
                                    "identifikator": "12345678901",
                                    "skyldnersNavn": "Test Person"
                                  }
                                }
                                """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "application/json"),
                        )
                    }

                val client = with(AppEnv.DEV) { createHttpClient(mockEngine) }
                val skatteetatenInnkrevingsoppdragHttpClient =
                    SkatteetatenInnkrevingsoppdragHttpClient("http://localhost:8080", client)

                val result =
                    skatteetatenInnkrevingsoppdragHttpClient.søk(
                        Skyldnersøk(
                            Skyldner(SkyldnerId("12345678901")),
                            Kravfilter.ALLE,
                        ),
                    )

                result.shouldBeRight().krav.shouldContain(
                    Krav(
                        skeKravidentifikator = Kravidentifikator.Skatteetaten("29ab06ef-9de1-4312-9677-163e4cc586dd"),
                        navKravidentifikator = Kravidentifikator.Nav("310ade77-8d45-48e8-b053-72659f53b4eb"),
                        navReferanse = "ref1",
                        kravtype = Kravtype("OB04"),
                        kravbeskrivelse = listOf(Kravbeskrivelse(Locale.forLanguageTag("NB"), "Eksempeltekst")),
                        gjenståendeBeløp = 1000.0,
                    ),
                )
            }

            "returnere feil ved uventet statuskode" {
                val mockEngine =
                    MockEngine {
                        respond(
                            content = """{"error": "internal error"}""",
                            status = HttpStatusCode.InternalServerError,
                            headers = headersOf(HttpHeaders.ContentType, "application/json"),
                        )
                    }

                val client = with(AppEnv.DEV) { createHttpClient(mockEngine) }
                val skatteetatenClient = SkatteetatenInnkrevingsoppdragHttpClient("http://localhost:8080", client)

                val result = skatteetatenClient.søk(Skyldnersøk(Skyldner(SkyldnerId("12345678901")), Kravfilter.ALLE))

                result.shouldBeLeft(SøkEtterInnkrevingskrav.Feil.SøkEtterInnkrevingskravFeil)
            }
        }
    })
