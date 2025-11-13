package no.nav.tilbakekreving.infrastructure.client.skatteetaten

import arrow.core.Either
import arrow.core.raise.either
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import no.nav.tilbakekreving.app.HentKravdetaljer
import no.nav.tilbakekreving.domain.Kravdetaljer
import no.nav.tilbakekreving.domain.Kravidentifikator
import no.nav.tilbakekreving.infrastructure.client.skatteetaten.json.HentKravdetaljerResponsJson
import org.slf4j.LoggerFactory

class SkatteetatenInnkrevingsoppdrackMockHttpClient(
    private val baseUrl: String,
    private val client: HttpClient,
) : HentKravdetaljer {
    private val logger = LoggerFactory.getLogger(this::class.java)

    init {
        logger.info("Starter innkrevingsoppdrackMockHttpClient")
    }

    override suspend fun hentKravdetaljer(
        kravidentifikator: Kravidentifikator,
    ): Either<HentKravdetaljer.HentKravdetaljerFeil, Kravdetaljer> =
        either {
            val httpResponse =
                client.get("$baseUrl/api/innkreving/innkrevingsoppdrag/v1/innkrevingsoppdrag/${kravidentifikator.id}/mock") {
                    headers {
                        append("Klientid", "NAV/2.0")
                    }
                    accept(ContentType.Application.Json)
                    val kravtype =
                        when (kravidentifikator) {
                            is Kravidentifikator.Nav -> "OPPDRAGSGIVERS_KRAVIDENTIFIKATOR"
                            is Kravidentifikator.Skatteetaten -> "SKATTEETATENS_KRAVIDENTIFIKATOR"
                        }
                    parameter(
                        "kravidentifikatortype",
                        kravtype,
                    )
                }

            when (httpResponse.status) {
                HttpStatusCode.OK -> {
                    httpResponse.body<HentKravdetaljerResponsJson>().toDomain()
                }

                HttpStatusCode.NotFound -> {
                    logger.info(
                        "Kravdetaljer ikke funnet for kravidentifikator: {}",
                        when (kravidentifikator) {
                            is Kravidentifikator.Nav -> "navId=${kravidentifikator.id}"
                            is Kravidentifikator.Skatteetaten -> "skatteetatenId=${kravidentifikator.id}"
                        },
                    )
                    raise(HentKravdetaljer.HentKravdetaljerFeil.FantIkkeKravdetaljer)
                }

                else -> {
                    logger.error(
                        "Feil ved henting av kravdetaljer: {} - {}",
                        httpResponse.status.toString(),
                        httpResponse.bodyAsText(),
                    )
                    raise(HentKravdetaljer.HentKravdetaljerFeil.FeilVedHentingAvKravdetaljer)
                }
            }
        }
}
