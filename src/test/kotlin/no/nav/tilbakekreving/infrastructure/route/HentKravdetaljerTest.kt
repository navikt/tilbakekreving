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
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.mockk.Called
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.datetime.LocalDate
import no.nav.tilbakekreving.AppEnv
import no.nav.tilbakekreving.app.HentKravdetaljer
import no.nav.tilbakekreving.config.AuthenticationConfigName
import no.nav.tilbakekreving.config.EntraProxyConfig
import no.nav.tilbakekreving.domain.KravDetalj
import no.nav.tilbakekreving.domain.Kravdetaljer
import no.nav.tilbakekreving.domain.KravdetaljerSkyldner
import no.nav.tilbakekreving.domain.Kravgrunnlag
import no.nav.tilbakekreving.domain.Kravidentifikator
import no.nav.tilbakekreving.domain.Kravlinje
import no.nav.tilbakekreving.domain.Kravtype
import no.nav.tilbakekreving.domain.Oppdragsgiver
import no.nav.tilbakekreving.infrastructure.audit.AuditLog
import no.nav.tilbakekreving.infrastructure.auth.AccessTokenValidator
import no.nav.tilbakekreving.infrastructure.auth.EntraOboTokenExchanger
import no.nav.tilbakekreving.infrastructure.auth.abac.policy.lesKravAccessPolicy
import no.nav.tilbakekreving.infrastructure.auth.model.Enhetsnummer
import no.nav.tilbakekreving.infrastructure.auth.model.GroupId
import no.nav.tilbakekreving.infrastructure.auth.model.OboToken
import no.nav.tilbakekreving.infrastructure.auth.model.ValidatedEntraToken
import no.nav.tilbakekreving.infrastructure.client.entra.proxy.EntraProxyClient
import no.nav.tilbakekreving.infrastructure.route.json.HentKravdetaljerJsonRequest
import no.nav.tilbakekreving.infrastructure.route.json.KravidentifikatorType
import no.nav.tilbakekreving.infrastructure.unleash.StubFeatureToggle
import no.nav.tilbakekreving.setup.configureEntraAuthentication
import no.nav.tilbakekreving.setup.configureSerialization
import no.nav.tilbakekreving.util.specWideTestApplication
import org.slf4j.LoggerFactory

class HentKravdetaljerTest :
    WordSpec({
        val authenticationConfigName = AuthenticationConfigName.ENTRA_ID
        val hentKravdetaljer = mockk<HentKravdetaljer>()
        val auditLog = mockk<AuditLog>(relaxed = true)
        val kravAccessPolicy =
            context(StubFeatureToggle(default = true), LoggerFactory.getLogger(this::class.java)) {
                lesKravAccessPolicy(
                    GroupId("les-krav"),
                    mapOf(Enhetsnummer("1111") to setOf(Kravtype.TILBAKEKREVING_BARNETRYGD)),
                )
            }

        val accessTokenValidator = mockk<AccessTokenValidator<ValidatedEntraToken>>()
        coEvery { accessTokenValidator.validateToken(any()) } returns
            AccessTokenValidator.ValidationError.InvalidToken.left()
        coEvery { accessTokenValidator.validateToken("valid-token") } returns
            ValidatedEntraToken(
                navIdent = "Z123456",
                groupIds = setOf(GroupId("les-krav")),
            ).right()

        val entraOboTokenExchanger = mockk<EntraOboTokenExchanger>()
        coEvery { entraOboTokenExchanger.exchange("valid-token", any()) } returns OboToken("obo-token").right()

        val entraProxyClient = mockk<EntraProxyClient>()
        coEvery { entraProxyClient.hentEnheter(OboToken("obo-token")) } returns
            setOf(Enhetsnummer("1111")).right()

        val entraProxyConfig =
            EntraProxyConfig(
                baseUrl = java.net.URI("http://localhost").toURL(),
                apiTarget = "api://test/.default",
            )

        // Reset audit mocks for å kunne telle kall per test
        afterTest { clearMocks(auditLog) }

        val client =
            specWideTestApplication {
                application {
                    dependencies {
                        provide<AppEnv> { AppEnv.LOCAL }
                        provide<AccessTokenValidator<ValidatedEntraToken>> { accessTokenValidator }
                        provide<EntraOboTokenExchanger> { entraOboTokenExchanger }
                        provide<EntraProxyClient> { entraProxyClient }
                        provide<EntraProxyConfig> { entraProxyConfig }
                    }
                    configureSerialization()
                    configureEntraAuthentication()
                    routing {
                        authenticate(authenticationConfigName.configName) {
                            route("/kravdetaljer") {
                                context(auditLog, kravAccessPolicy) {
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
                        kravtype = Kravtype.TILBAKEKREVING_BARNETRYGD,
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
                    .shouldHaveContentType(ContentType.Application.Json)
                    .bodyAsText()
                    .shouldEqualJson(
                        // language=json
                        """
                        {
                            "krav": {
                                "forfallsdato": "2025-02-01",
                                "foreldelsesdato": "2030-01-01",
                                "fastsettelsesdato": "2024-12-01",
                                "kravtype": "TILBAKEKREVING_BARNETRYGD",
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
            "returnere 401 når bruker ikke har tilgang til kravtypen" {
                val dagpengerKravdetaljer =
                    kravdetaljer.copy(
                        krav = kravdetaljer.krav.copy(kravtype = Kravtype.TILBAKEKREVING_DAGPENGER),
                    )
                coEvery { hentKravdetaljer.hentKravdetaljer(kravidentifikator) } returns dagpengerKravdetaljer.right()

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
                    }.shouldHaveStatus(HttpStatusCode.Unauthorized)

                coVerify { auditLog wasNot Called }
            }
        }
    })
