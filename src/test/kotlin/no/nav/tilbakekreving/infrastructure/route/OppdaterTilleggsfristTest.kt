package no.nav.tilbakekreving.infrastructure.route

import io.kotest.assertions.ktor.client.shouldBeOK
import io.kotest.assertions.ktor.client.shouldHaveStatus
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.datetime.LocalDate
import no.nav.tilbakekreving.domain.Kravidentifikator
import no.nav.tilbakekreving.domain.TilleggsfristStore
import no.nav.tilbakekreving.infrastructure.route.json.KravidentifikatorType
import no.nav.tilbakekreving.setup.configureSerialization
import no.nav.tilbakekreving.util.specWideTestApplication

class OppdaterTilleggsfristTest :
    WordSpec({
        val tilleggsfristStore = TilleggsfristStore()
        val client =
            specWideTestApplication {
                application {
                    configureSerialization()
                    routing {
                        route("/tilleggsfrist") {
                            oppdaterTilleggsfristRoute(tilleggsfristStore)
                        }
                    }
                }
            }.client

        val kravidentifikator = Kravidentifikator.Nav("123456789")
        val tilleggsfrist = LocalDate(2025, 2, 1)

        "oppdater tilleggsfrist" should {
            "returnere 200 når tilleggsfrist er oppdatert" {
                client
                    .post("/tilleggsfrist") {
                        contentType(ContentType.Application.Json)
                        setBody(
                            // language=json
                            """
                            {
                                "id": "${kravidentifikator.id}",
                                "type": "${KravidentifikatorType.NAV}",
                                "tilleggsfrist": "$tilleggsfrist"
                            }
                            """.trimIndent(),
                        )
                    }.shouldBeOK()

                // Verify that the tilleggsfrist was stored
                tilleggsfristStore.getTilleggsfrist(kravidentifikator) shouldBe tilleggsfrist
            }

            "returnere 400 når tilleggsfrist har ugyldig format" {
                val response =
                    client
                        .post("/tilleggsfrist") {
                            contentType(ContentType.Application.Json)
                            setBody(
                                // language=json
                                """
                                {
                                    "id": "${kravidentifikator.id}",
                                    "type": "${KravidentifikatorType.SKATTEETATEN}",
                                    "tilleggsfrist": "invalid-date-format"
                                }
                                """.trimIndent(),
                            )
                        }

                response.shouldHaveStatus(HttpStatusCode.BadRequest)
            }
        }
    })
