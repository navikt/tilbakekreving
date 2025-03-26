package no.nav.infrastructure.client.skatteetaten

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
import io.ktor.http.appendPathSegments
import no.nav.app.HentKravdetaljerFeil
import no.nav.app.InnkrevingsoppdragClient
import no.nav.domain.Kravdetaljer
import no.nav.domain.Kravidentifikator
import no.nav.infrastructure.client.skatteetaten.json.KravdetaljerResponseJson
import no.nav.infrastructure.client.skatteetaten.json.KravidentifikatortypeQuery
import org.slf4j.LoggerFactory

class SkatteetatenInnkrevingsoppdragHttpClient(
    private val baseUrl: String,
    private val client: HttpClient,
) : InnkrevingsoppdragClient {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun hentKravdetaljer(kravidentifikator: Kravidentifikator): Either<HentKravdetaljerFeil, Kravdetaljer> =
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

            if (httpResponse.status.value !in 200..299) {
                logger.error(
                    "Feil ved henting av kravdetaljer: {} - {}",
                    httpResponse.status.toString(),
                    httpResponse.bodyAsText(),
                )
                raise(HentKravdetaljerFeil.FeilVedHentingAvKravdetaljer)
            }

            httpResponse.body<KravdetaljerResponseJson>().toDomain()
        }
}
