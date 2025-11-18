package no.nav.tilbakekreving.infrastructure.route

import arrow.core.left
import arrow.core.right
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.assertions.ktor.client.shouldBeBadRequest
import io.kotest.assertions.ktor.client.shouldBeOK
import io.kotest.assertions.ktor.client.shouldHaveContentType
import io.kotest.assertions.ktor.client.shouldHaveStatus
import io.kotest.core.spec.style.WordSpec
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.withCharset
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.mockk.Called
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.datetime.LocalDate
import no.nav.tilbakekreving.app.HentKravdetaljer
import no.nav.tilbakekreving.domain.KravDetalj
import no.nav.tilbakekreving.domain.Kravdetaljer
import no.nav.tilbakekreving.domain.KravdetaljerSkyldner
import no.nav.tilbakekreving.domain.Kravgrunnlag
import no.nav.tilbakekreving.domain.Kravidentifikator
import no.nav.tilbakekreving.domain.Kravlinje
import no.nav.tilbakekreving.domain.Oppdragsgiver
import no.nav.tilbakekreving.infrastructure.audit.AuditLog
import no.nav.tilbakekreving.infrastructure.auth.GroupId
import no.nav.tilbakekreving.infrastructure.client.AccessTokenVerifier
import no.nav.tilbakekreving.infrastructure.route.json.HentKravdetaljerJsonRequest
import no.nav.tilbakekreving.infrastructure.route.json.KravidentifikatorType
import no.nav.tilbakekreving.setup.AuthenticationConfigName
import no.nav.tilbakekreving.setup.configureAuthentication
import no.nav.tilbakekreving.setup.configureSerialization
import no.nav.tilbakekreving.util.specWideTestApplication

class HentKravdetaljerTest :
    WordSpec({
        val authenticationConfigName = AuthenticationConfigName("test")
        val hentKravdetaljer = mockk<HentKravdetaljer>()
        val auditLog = mockk<AuditLog>(relaxed = true)

        val accessTokenVerifier = mockk<AccessTokenVerifier>()
        coEvery { accessTokenVerifier.verifyToken(any()) } returns
            AccessTokenVerifier.VerificationError.InvalidToken.left()
        coEvery { accessTokenVerifier.verifyToken("valid-token") } returns
            AccessTokenVerifier
                .ValidatedToken(
                    navIdent = "Z123456",
                    groupIds = listOf("gruppe1").map { GroupId(it) },
                ).right()

        // Reset audit mocks for å kunne telle kall per test
        afterTest { clearMocks(auditLog) }

        val client =
            specWideTestApplication {
                application {
                    configureSerialization()
                    configureAuthentication(authenticationConfigName, accessTokenVerifier)
                    routing {
                        authenticate(authenticationConfigName.name) {
                            route("/kravdetaljer") {
                                context(auditLog) {
                                    hentKravdetaljerRoute(hentKravdetaljer)
                                }
                            }
                        }
                    }
                }
            }.client

        val kravdetaljer =
            Kravdetaljer(
                krav =
                    KravDetalj(
                        forfallsdato = LocalDate(2025, 2, 1),
                        foreldelsesdato = LocalDate(2030, 1, 1),
                        fastsettelsesdato = LocalDate(2024, 12, 1),
                        kravtype = "OB04",
                        opprinneligBeløp = 1000.0,
                        gjenståendeBeløp = 500.0,
                        skatteetatensKravidentifikator = "skatte-123",
                        kravlinjer =
                            listOf(
                                Kravlinje(
                                    kravlinjetype = "Kravlinjetype",
                                    opprinneligBeløp = 1000.0,
                                    gjenståendeBeløp = 500.0,
                                    emptyMap(),
                                ),
                            ),
                        kravgrunnlag = Kravgrunnlag("123456789", "ref-123"),
                    ),
                oppdragsgiver = Oppdragsgiver("987654321", "Test Oppdragsgiver"),
                skyldner = KravdetaljerSkyldner("12345678901", "Test Person"),
            )
        val kravidentifikator = Kravidentifikator.Nav("123456789")

        "hent kravdetaljer" should {
            "returnere 200 med kravdetaljer" {
                coEvery { hentKravdetaljer.hentKravdetaljer(kravidentifikator) } returns kravdetaljer.right()

                HentKravdetaljerJsonRequest(
                    id = kravidentifikator.id,
                    type = KravidentifikatorType.NAV,
                )

                client
                    .post("/kravdetaljer") {
                        contentType(ContentType.Application.Json)
                        setBody(
                            // language=json
                            """
                            {
                                "id": "${kravidentifikator.id}",
                                "type": "${KravidentifikatorType.NAV}"
                            }
                            """.trimIndent(),
                        )
                        bearerAuth("valid-token")
                    }.shouldBeOK()
                    .shouldHaveContentType(ContentType.Application.Json.withCharset(Charsets.UTF_8))
                    .bodyAsText()
                    .shouldEqualJson(
                        // language=json
                        """
                        {
                            "krav": {
                                "forfallsdato": "2025-02-01",
                                "foreldelsesdato": "2030-01-01",
                                "fastsettelsesdato": "2024-12-01",
                                "kravtype": "OB04",
                                "opprinneligBeløp": 1000.0,
                                "gjenståendeBeløp": 500.0,
                                "skatteetatensKravidentifikator": "skatte-123",
                                "kravlinjer": [
                                    {
                                        "kravlinjetype": "Kravlinjetype",
                                        "opprinneligBeløp": 1000.0,
                                        "gjenståendeBeløp": 500.0,
                                        "kravlinjeBeskrivelse": {}
                                    }
                                ],
                                "kravgrunnlag": {
                                    "oppdragsgiversKravidentifikator": "123456789",
                                    "oppdragsgiversReferanse": "ref-123"
                                },
                                "innbetalingerPlassertMotKrav": [],
                                "tilleggsinformasjon": null
                            },
                            "oppdragsgiver": {
                                "organisasjonsnummer": "987654321",
                                "organisasjonsnavn": "Test Oppdragsgiver"
                            },
                            "skyldner": {
                                "identifikator": "12345678901",
                                "skyldnersNavn": "Test Person"
                            },
                            "avvik": null
                        }
                        """.trimIndent(),
                    )

                coVerify(exactly = 1) { auditLog.info(any()) }
            }
            "returnere 201 når kravdetaljer ikke finnes" {
                coEvery { hentKravdetaljer.hentKravdetaljer(kravidentifikator) } returns
                    HentKravdetaljer.HentKravdetaljerFeil.FantIkkeKravdetaljer.left()

                client
                    .post("/kravdetaljer") {
                        contentType(ContentType.Application.Json)
                        setBody(
                            // language=json
                            """
                            {
                                "id": "${kravidentifikator.id}",
                                "type": "${KravidentifikatorType.NAV}"
                            }
                            """.trimIndent(),
                        )
                        bearerAuth("valid-token")
                    }.shouldHaveStatus(HttpStatusCode.NoContent)

                coVerify { auditLog wasNot Called }
            }
            "returnere 400 når json ikke er riktig" {
                client
                    .post("/kravdetaljer") {
                        contentType(ContentType.Application.Json)
                        setBody(
                            // language=json
                            """
                            {}
                            """.trimIndent(),
                        )
                        bearerAuth("valid-token")
                    }.shouldBeBadRequest()

                verify { auditLog wasNot Called }
            }
            "returnere 401 når token er ugyldig" {
                client
                    .post("/kravdetaljer") {
                        contentType(ContentType.Application.Json)
                        setBody(
                            // language=json
                            """
                            {
                                "id": "${kravidentifikator.id}",
                                "type": "${KravidentifikatorType.NAV}"
                            }
                            """.trimIndent(),
                        )
                        bearerAuth("invalid-token")
                    }.shouldHaveStatus(HttpStatusCode.Unauthorized)

                verify { auditLog wasNot Called }
            }
            "returnere 500 ved feil i tjenesten" {
                coEvery { hentKravdetaljer.hentKravdetaljer(kravidentifikator) } returns
                    HentKravdetaljer.HentKravdetaljerFeil.FeilVedHentingAvKravdetaljer.left()
                client
                    .post("/kravdetaljer") {
                        contentType(ContentType.Application.Json)
                        setBody(
                            // language=json
                            """
                            {
                                "id": "${kravidentifikator.id}",
                                "type": "${KravidentifikatorType.NAV}"
                            }
                            """.trimIndent(),
                        )
                        bearerAuth("valid-token")
                    }.shouldHaveStatus(HttpStatusCode.InternalServerError)

                coVerify { auditLog wasNot Called }
            }
        }
    })
