package no.nav.tilbakekreving.infrastructure.route

import arrow.core.right
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.assertions.ktor.client.shouldBeOK
import io.kotest.assertions.ktor.client.shouldHaveContentType
import io.kotest.core.spec.style.WordSpec
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.withCharset
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.tilbakekreving.app.HentKravoversikt
import no.nav.tilbakekreving.domain.Krav
import no.nav.tilbakekreving.domain.Kravidentifikator
import no.nav.tilbakekreving.setup.configureSerialization
import no.nav.tilbakekreving.util.specWideTestApplication

class HentKravoversiktTest :
    WordSpec({
        val hentKravoversikt = mockk<HentKravoversikt>()
        val client =
            specWideTestApplication {
                application {
                    configureSerialization()
                    routing {
                        route("/kravdetaljer") {
                            hentKravoversikt(hentKravoversikt)
                        }
                    }
                }
            }.client

        "hent kravoversikt" should {
            "returnere 200 med kravoversikt" {
                coEvery { hentKravoversikt.hentKravoversikt(any(), any()) } returns
                    listOf(
                        Krav(
                            Kravidentifikator.Nav("123456789"),
                            "Kravtype",
                        ),
                    ).right()

                client
                    .post("/kravdetaljer") {
                        setBody(
                            // language=json
                            """
                            {
                              "type": "fødselsnummer",
                              "id": "123456789",
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
                            "krav": [
                                {
                                    "kravidentifikator": {
                                        "id": "123456789",
                                        "type": "nav"
                                    },
                                    "kravtype": "Kravtype"
                                }
                            ]
                        }
                        """.trimIndent(),
                    )
            }
        }
    })
