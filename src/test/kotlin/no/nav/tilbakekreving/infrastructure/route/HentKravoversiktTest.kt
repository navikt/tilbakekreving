package no.nav.tilbakekreving.infrastructure.route

import arrow.core.right
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.assertions.ktor.client.shouldBeOK
import io.kotest.assertions.ktor.client.shouldHaveContentType
import io.kotest.core.spec.style.WordSpec
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.withCharset
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.bearer
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import no.nav.tilbakekreving.app.SøkEtterInnkrevingskrav
import no.nav.tilbakekreving.domain.Krav
import no.nav.tilbakekreving.domain.Kravbeskrivelse
import no.nav.tilbakekreving.domain.Kravidentifikator
import no.nav.tilbakekreving.domain.Kravoversikt
import no.nav.tilbakekreving.domain.KravoversiktSkyldner
import no.nav.tilbakekreving.domain.Kravtype
import no.nav.tilbakekreving.domain.Oppdragsgiver
import no.nav.tilbakekreving.infrastructure.audit.AuditLog
import no.nav.tilbakekreving.infrastructure.auth.GroupId
import no.nav.tilbakekreving.infrastructure.auth.NavUserPrincipal
import no.nav.tilbakekreving.setup.configureSerialization
import no.nav.tilbakekreving.util.specWideTestApplication
import java.util.Locale

class HentKravoversiktTest :
    WordSpec({
        val søkEtterInnkrevingskrav = mockk<SøkEtterInnkrevingskrav>()
        val auditLog = mockk<AuditLog>(relaxed = true)
        val kravAccessControl =
            KravAccessControl(mapOf(Kravtype("Kravtype") to setOf(GroupId("enhet_1"))), GroupId("tilgang_til_krav"))
        val client =
            specWideTestApplication {
                application {
                    configureSerialization()
                    install(Authentication) {
                        bearer("entra-id") {
                            authenticate { _ ->
                                NavUserPrincipal("Z123456", listOf(GroupId("tilgang_til_krav"), GroupId("enhet_1")))
                            }
                        }
                    }
                    routing {
                        authenticate("entra-id") {
                            route("/kravoversikt") {
                                context(kravAccessControl, auditLog) {
                                    hentKravoversikt(søkEtterInnkrevingskrav)
                                }
                            }
                        }
                    }
                }
            }.client
        // Reset audit mocks for å kunne telle kall per test
        afterTest { clearMocks(auditLog) }

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
                                    kravbeskrivelse = mapOf(Locale.forLanguageTag("nb") to Kravbeskrivelse("Test beskrivelse")),
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
                                    "kravbeskrivelse": {
                                        "nb": "Test beskrivelse"
                                    },
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

                verify(exactly = 1) { auditLog.info(any()) }
            }

            /**
             * TODO: Skru på test når [KravAccessControl.isKravAccessibleTo] er ferdig implementert.
             */
            "returnere 200 med utvalgt kravoversikt basert på roller".config(enabled = false) {
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
                                    kravbeskrivelse = mapOf(Locale.forLanguageTag("nb") to Kravbeskrivelse("Test beskrivelse")),
                                    gjenståendeBeløp = 1000.0,
                                ),
                                Krav(
                                    skeKravidentifikator = Kravidentifikator.Skatteetaten("skatte-987654321"),
                                    navKravidentifikator = Kravidentifikator.Nav("987654321"),
                                    navReferanse = "ref2",
                                    kravtype = Kravtype("Kravtype1"),
                                    kravbeskrivelse = mapOf(Locale.forLanguageTag("nb") to Kravbeskrivelse("Test beskrivelse 2")),
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
                                    "kravbeskrivelse": {
                                        "nb": "Test beskrivelse"
                                    },
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
                verify(exactly = 1) { auditLog.info(any()) }
            }
        }
    })
