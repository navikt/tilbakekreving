package no.nav.tilbakekreving.infrastructure.route

import arrow.core.left
import arrow.core.right
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.assertions.ktor.client.shouldBeBadRequest
import io.kotest.assertions.ktor.client.shouldBeOK
import io.kotest.assertions.ktor.client.shouldHaveContentType
import io.kotest.assertions.ktor.client.shouldHaveStatus
import io.kotest.core.spec.style.WordSpec
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.withCharset
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.datetime.LocalDate
import no.nav.tilbakekreving.app.HentKravdetaljer
import no.nav.tilbakekreving.domain.Kravdetaljer
import no.nav.tilbakekreving.domain.Kravgrunnlag
import no.nav.tilbakekreving.domain.Kravidentifikator
import no.nav.tilbakekreving.domain.Kravlinje
import no.nav.tilbakekreving.infrastructure.route.json.HentKravdetaljerJsonRequest
import no.nav.tilbakekreving.infrastructure.route.json.KravidentifikatorType
import no.nav.tilbakekreving.setup.configureSerialization
import no.nav.tilbakekreving.util.specWideTestApplication

class HentKravdetaljerTest :
    WordSpec({
        val hentKravdetaljer = mockk<HentKravdetaljer>()
        val client =
            specWideTestApplication {
                application {
                    configureSerialization()
                    routing {
                        route("/kravdetaljer") {
                            hentKravdetaljerRoute(hentKravdetaljer)
                        }
                    }
                }
            }.client

        val kravdetaljer =
            Kravdetaljer(
                kravgrunnlag = Kravgrunnlag(LocalDate(2025, 1, 1)),
                kravlinjer =
                    listOf(
                        Kravlinje(
                            kravlinjetype = "Kravlinjetype",
                            opprinneligBeloep = 1000.0,
                            gjenstaaendeBeloep = 500.0,
                        ),
                    ),
                tilleggsfrist = LocalDate(2025, 2, 1),
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
                    }.shouldBeOK()
                    .shouldHaveContentType(ContentType.Application.Json.withCharset(Charsets.UTF_8))
                    .bodyAsText()
                    .shouldEqualJson(
                        // language=json
                        """
                        {
                            "kravgrunnlag": {
                                "datoNårKravVarBesluttetHosOppdragsgiver": "2025-01-01"
                            },
                            "kravlinjer": [
                                {
                                    "kravlinjetype": "Kravlinjetype",
                                    "opprinneligBeløp": 1000.0,
                                    "gjenståendeBeløp": 500.0
                                }
                            ],
                            "tilleggsfrist": "2025-02-01"
                        }
                        """.trimIndent(),
                    )
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
                    }.shouldHaveStatus(HttpStatusCode.NoContent)
            }
            "returnere 401 når json ikke er riktig" {
                client
                    .post("/kravdetaljer") {
                        contentType(ContentType.Application.Json)
                        setBody(
                            // language=json
                            """
                            {}
                            """.trimIndent(),
                        )
                    }.shouldBeBadRequest()
            }
        }
    })
