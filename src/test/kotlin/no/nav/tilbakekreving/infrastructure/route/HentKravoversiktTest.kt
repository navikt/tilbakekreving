package no.nav.tilbakekreving.infrastructure.route

import arrow.core.left
import arrow.core.right
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.assertions.ktor.client.shouldBeBadRequest
import io.kotest.assertions.ktor.client.shouldBeOK
import io.kotest.assertions.ktor.client.shouldHaveContentType
import io.kotest.assertions.ktor.client.shouldHaveStatus
import io.kotest.core.spec.style.WordSpec
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.withCharset
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.bearer
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.tilbakekreving.app.SøkEtterInnkrevingskrav
import no.nav.tilbakekreving.domain.Krav
import no.nav.tilbakekreving.domain.Kravbeskrivelse
import no.nav.tilbakekreving.domain.Kravidentifikator
import no.nav.tilbakekreving.domain.Kravoversikt
import no.nav.tilbakekreving.domain.KravoversiktSkyldner
import no.nav.tilbakekreving.domain.Kravtype
import no.nav.tilbakekreving.domain.Oppdragsgiver
import no.nav.tilbakekreving.infrastructure.auth.GroupId
import no.nav.tilbakekreving.infrastructure.auth.NavUserPrincipal
import no.nav.tilbakekreving.infrastructure.auth.abac.policy.lesKravAccessPolicy
import no.nav.tilbakekreving.infrastructure.unleash.StubFeatureToggles
import no.nav.tilbakekreving.setup.configureSerialization
import no.nav.tilbakekreving.util.specWideTestApplication
import java.util.Locale

class HentKravoversiktTest :
    WordSpec({
        val søkEtterInnkrevingskrav = mockk<SøkEtterInnkrevingskrav>()
        val kravAccessPolicy =
            context(StubFeatureToggles(default = true)) {
                lesKravAccessPolicy(
                    GroupId("tilgang_til_krav"),
                    mapOf(Kravtype("Kravtype") to setOf(GroupId("enhet_1"))),
                )
            }
        val client =
            specWideTestApplication {
                application {
                    configureSerialization()
                    install(Authentication) {
                        bearer("entra-id") {
                            authenticate { _ ->
                                NavUserPrincipal("Z123456", setOf(GroupId("tilgang_til_krav"), GroupId("enhet_1")))
                            }
                        }
                    }
                    routing {
                        authenticate("entra-id") {
                            route("/kravoversikt") {
                                context(kravAccessPolicy) {
                                    hentKravoversikt(søkEtterInnkrevingskrav)
                                }
                            }
                        }
                    }
                }
            }.client

        "hent kravoversikt" should {
            "returnere 200 med kravoversikt" {
                coEvery { søkEtterInnkrevingskrav.søk(any()) } returns
                    Kravoversikt(
                        oppdragsgiver = Oppdragsgiver("123456789", "Test Oppdragsgiver"),
                        krav =
                            listOf(
                                Krav(
                                    skeKravidentifikator = Kravidentifikator.Skatteetaten("skatte-123456789"),
                                    navKravidentifikator = Kravidentifikator.Nav("123456789"),
                                    navReferanse = "ref1",
                                    kravtype = Kravtype("Kravtype"),
                                    kravbeskrivelse =
                                        listOf(
                                            Kravbeskrivelse(
                                                Locale.forLanguageTag("nb"),
                                                "Test beskrivelse",
                                            ),
                                        ),
                                    gjenståendeBeløp = 1000.0,
                                ),
                            ),
                        gjenståendeBeløpForSkyldner = 1000.0,
                        skyldner = KravoversiktSkyldner("123456789", "Test Skyldner"),
                    ).right()

                client
                    .post("/kravoversikt") {
                        header(HttpHeaders.Authorization, "Bearer any-token")
                        setBody(
                            // language=json
                            """
                            {
                              "skyldner": "123456789",
                              "kravfilter": "ALLE"
                            }
                            """.trimIndent(),
                        )
                        contentType(ContentType.Application.Json)
                    }.shouldBeOK()
                    .shouldHaveContentType(ContentType.Application.Json.withCharset(Charsets.UTF_8))
                    .bodyAsText()
                    .shouldEqualJson(
                        // language=json
                        """
                        {
                          "oppdragsgiver": {
                            "organisasjonsnummer": "123456789",
                            "organisasjonsnavn": "Test Oppdragsgiver"
                          },
                          "krav": [
                            {
                              "skeKravidentifikator": "skatte-123456789",
                              "navKravidentifikator": "123456789",
                              "navReferanse": "ref1",
                              "kravtype": "Kravtype",
                              "kravbeskrivelse": [
                                {
                                  "språk": "nb",
                                  "tekst": "Test beskrivelse"
                                }
                              ],
                              "gjenståendeBeløp": 1000.0
                            }
                          ],
                          "gjenståendeBeløpForSkyldner": 1000.0,
                          "skyldner": {
                            "identifikator": "123456789",
                            "skyldnersNavn": "Test Skyldner"
                          }
                        }
                        """.trimIndent(),
                    )
            }

            "returnere 500 ved feil i tjenesten" {
                coEvery { søkEtterInnkrevingskrav.søk(any()) } returns
                    SøkEtterInnkrevingskrav.Feil.SøkEtterInnkrevingskravFeil.left()

                client
                    .post("/kravoversikt") {
                        header(HttpHeaders.Authorization, "Bearer any-token")
                        setBody(
                            // language=json
                            """
                            {
                              "skyldner": "123456789",
                              "kravfilter": "ALLE"
                            }
                            """.trimIndent(),
                        )
                        contentType(ContentType.Application.Json)
                    }.shouldHaveStatus(HttpStatusCode.InternalServerError)
            }

            "returnere 400 når json ikke er riktig" {
                client
                    .post("/kravoversikt") {
                        header(HttpHeaders.Authorization, "Bearer any-token")
                        setBody(
                            // language=json
                            """
                            {}
                            """.trimIndent(),
                        )
                        contentType(ContentType.Application.Json)
                    }.shouldBeBadRequest()
            }

            "returnere 200 med utvalgt kravoversikt basert på roller" {
                coEvery { søkEtterInnkrevingskrav.søk(any()) } returns
                    Kravoversikt(
                        oppdragsgiver = Oppdragsgiver("123456789", "Test Oppdragsgiver"),
                        krav =
                            listOf(
                                Krav(
                                    skeKravidentifikator = Kravidentifikator.Skatteetaten("skatte-123456789"),
                                    navKravidentifikator = Kravidentifikator.Nav("123456789"),
                                    navReferanse = "ref1",
                                    kravtype = Kravtype("Kravtype"),
                                    kravbeskrivelse =
                                        listOf(
                                            Kravbeskrivelse(
                                                Locale.forLanguageTag("nb"),
                                                "Test beskrivelse",
                                            ),
                                        ),
                                    gjenståendeBeløp = 1000.0,
                                ),
                                Krav(
                                    skeKravidentifikator = Kravidentifikator.Skatteetaten("skatte-987654321"),
                                    navKravidentifikator = Kravidentifikator.Nav("987654321"),
                                    navReferanse = "ref2",
                                    kravtype = Kravtype("Kravtype1"),
                                    kravbeskrivelse =
                                        listOf(
                                            Kravbeskrivelse(
                                                Locale.forLanguageTag("nb"),
                                                "Test beskrivelse 2",
                                            ),
                                        ),
                                    gjenståendeBeløp = 2000.0,
                                ),
                            ),
                        gjenståendeBeløpForSkyldner = 3000.0,
                        skyldner = KravoversiktSkyldner("123456789", "Test Skyldner"),
                    ).right()

                client
                    .post("/kravoversikt") {
                        header(HttpHeaders.Authorization, "Bearer any-token")
                        setBody(
                            // language=json
                            """
                            {
                              "skyldner": "123456789",
                              "kravfilter": "ALLE"
                            }
                            """.trimIndent(),
                        )
                        contentType(ContentType.Application.Json)
                    }.shouldBeOK()
                    .shouldHaveContentType(ContentType.Application.Json.withCharset(Charsets.UTF_8))
                    .bodyAsText()
                    .shouldEqualJson(
                        // language=json
                        """
                        {
                            "oppdragsgiver": {
                                "organisasjonsnummer": "123456789",
                                "organisasjonsnavn": "Test Oppdragsgiver"
                            },
                            "krav": [
                                {
                                    "skeKravidentifikator": "skatte-123456789",
                                    "navKravidentifikator": "123456789",
                                    "navReferanse": "ref1",
                                    "kravtype": "Kravtype",
                                    "kravbeskrivelse": [
                                        {
                                            "språk": "nb",
                                            "tekst": "Test beskrivelse"
                                        }
                                    ],
                                    "gjenståendeBeløp": 1000.0
                                }
                            ],
                            "gjenståendeBeløpForSkyldner": 3000.0,
                            "skyldner": {
                                "identifikator": "123456789",
                                "skyldnersNavn": "Test Skyldner"
                            }
                        }
                        """.trimIndent(),
                    )
            }
        }
    })
