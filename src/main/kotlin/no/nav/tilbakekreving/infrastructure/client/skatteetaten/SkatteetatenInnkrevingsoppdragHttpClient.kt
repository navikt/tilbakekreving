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
import io.ktor.http.appendPathSegments
import no.nav.tilbakekreving.app.HentKravdetaljer
import no.nav.tilbakekreving.domain.Kravdetaljer
import no.nav.tilbakekreving.domain.Kravidentifikator
import no.nav.tilbakekreving.infrastructure.client.skatteetaten.json.KravdetaljerResponseJson
import no.nav.tilbakekreving.infrastructure.client.skatteetaten.json.KravidentifikatortypeQuery
import org.slf4j.LoggerFactory

class SkatteetatenInnkrevingsoppdragHttpClient(
    private val baseUrl: String,
    private val client: HttpClient,
) : HentKravdetaljer {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun hentKravdetaljer(
        kravidentifikator: Kravidentifikator,
    ): Either<HentKravdetaljer.HentKravdetaljerFeil, Kravdetaljer> =
        either {
            val httpResponse =
                client.get("$baseUrl/api/innkreving/innkrevingsoppdrag/v1/innkrevingsoppdrag") {
                    url {
                        appendPathSegments(kravidentifikator.id)
                    }
                    accept(ContentType.Application.Json)

                    headers {
                        append("Klientid", "NAV/2.0")
                    }
                    parameter("kravidentifikatortype", KravidentifikatortypeQuery.from(kravidentifikator))
                }

            when (httpResponse.status) {
                HttpStatusCode.OK -> {
                    httpResponse.body<KravdetaljerResponseJson>().toDomain()
                }

                HttpStatusCode.NotFound -> {
                    logger.info(
                        "Kravdetaljer ikke funnet for kravidentifikator: {}",
                        kravidentifikator.id,
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
