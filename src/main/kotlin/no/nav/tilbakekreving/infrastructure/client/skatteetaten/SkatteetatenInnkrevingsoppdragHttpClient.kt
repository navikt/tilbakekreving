package no.nav.tilbakekreving.infrastructure.client.skatteetaten

import arrow.core.Either
import arrow.core.raise.either
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import no.nav.tilbakekreving.app.HentKravdetaljer
import no.nav.tilbakekreving.app.SøkEtterInnkrevingskrav
import no.nav.tilbakekreving.domain.Kravdetaljer
import no.nav.tilbakekreving.domain.Kravidentifikator
import no.nav.tilbakekreving.domain.Kravoversikt
import no.nav.tilbakekreving.domain.Skyldnersøk
import no.nav.tilbakekreving.infrastructure.client.skatteetaten.json.HentKravdetaljerRequestJson
import no.nav.tilbakekreving.infrastructure.client.skatteetaten.json.HentKravdetaljerResponsJson
import no.nav.tilbakekreving.infrastructure.client.skatteetaten.json.HentKravoversiktRequestJson
import no.nav.tilbakekreving.infrastructure.client.skatteetaten.json.HentKravoversiktResponseJson
import org.slf4j.LoggerFactory

class SkatteetatenInnkrevingsoppdragHttpClient(
    private val baseUrl: String,
    private val client: HttpClient,
) : HentKravdetaljer,
    SøkEtterInnkrevingskrav {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun hentKravdetaljer(
        kravidentifikator: Kravidentifikator,
    ): Either<HentKravdetaljer.HentKravdetaljerFeil, Kravdetaljer> =
        either {
            val httpResponse =
                client.post("$baseUrl/api/innkreving/innkrevingsoppdrag/v1/innkrevingsoppdrag/kravdetaljer") {
                    headers {
                        append("Klientid", "NAV/2.0")
                    }
                    setBody(HentKravdetaljerRequestJson.from(kravidentifikator))
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
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

    override suspend fun søk(skyldnersøk: Skyldnersøk): Either<SøkEtterInnkrevingskrav.Feil, Kravoversikt> =
        either {
            val httpResponse =
                client.post("$baseUrl/api/innkreving/innkrevingsoppdrag/v1/innkrevingsoppdrag/kravoversikt") {
                    headers {
                        append("Klientid", "NAV/2.0")
                    }
                    setBody(HentKravoversiktRequestJson.from(skyldnersøk))
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                }

            when (httpResponse.status) {
                HttpStatusCode.OK -> {
                    httpResponse.body<HentKravoversiktResponseJson>().toDomain()
                }

                else -> {
                    logger.error(
                        "Feil ved søk etter innkrevingskrav: {} - {}",
                        httpResponse.status.toString(),
                        httpResponse.bodyAsText(),
                    )
                    raise(SøkEtterInnkrevingskrav.Feil.SøkEtterInnkrevingskravFeil)
                }
            }
        }
}
