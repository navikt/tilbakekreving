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
import io.mockk.coEvery
import io.mockk.mockk
import no.nav.tilbakekreving.app.HentKravoversikt
import no.nav.tilbakekreving.domain.Krav
import no.nav.tilbakekreving.domain.Kravidentifikator
import no.nav.tilbakekreving.domain.Kravtype
import no.nav.tilbakekreving.infrastructure.auth.GroupId
import no.nav.tilbakekreving.infrastructure.auth.UserGroupIdsPrincipal
import no.nav.tilbakekreving.setup.configureSerialization
import no.nav.tilbakekreving.util.specWideTestApplication

class HentKravoversiktTest :
    WordSpec({
        val hentKravoversikt = mockk<HentKravoversikt>()
        val kravAccessControl = KravAccessControl(mapOf(Kravtype("Kravtype") to setOf(GroupId("group_all"))))
        val client =
            specWideTestApplication {
                application {
                    configureSerialization()
                    install(Authentication) {
                        bearer("entra-id") {
                            authenticate { _ ->
                                UserGroupIdsPrincipal(listOf(GroupId("group_all")))
                            }
                        }
                    }
                    routing {
                        authenticate("entra-id") {
                            route("/kravoversikt") {
                                context(kravAccessControl) {
                                    hentKravoversikt(hentKravoversikt)
                                }
                            }
                        }
                    }
                }
            }.client

        "hent kravoversikt" should {
            "returnere 200 med kravoversikt" {
                coEvery { hentKravoversikt.hentKravoversikt(any()) } returns
                    listOf(
                        Krav(
                            Kravidentifikator.Nav("123456789"),
                            Kravtype("Kravtype"),
                        ),
                    ).right()

                client
                    .post("/kravoversikt") {
                        header(HttpHeaders.Authorization, "Bearer any-token")
                        setBody(
                            // language=json
                            """
                            {
                              "type": "fødselsnummer",
                              "id": "123456789"
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

            "returnere 200 med utvalgt kravoversikt basert på roller" {
                coEvery { hentKravoversikt.hentKravoversikt(any()) } returns
                    listOf(
                        Krav(
                            Kravidentifikator.Nav("123456789"),
                            Kravtype("Kravtype1"),
                        ),
                        Krav(
                            Kravidentifikator.Nav("987654321"),
                            Kravtype("Kravtype2"),
                        ),
                    ).right()

                KravAccessControl(
                    mapOf(
                        Kravtype("Kravtype1") to setOf(GroupId("group1"), GroupId("group_all")),
                        Kravtype("Kravtype2") to setOf(GroupId("group2"), GroupId("group_all")),
                    ),
                )
            }
        }
    })
