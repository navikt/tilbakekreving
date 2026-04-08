package no.nav.tilbakekreving.infrastructure.client.entra.proxy

import arrow.core.Either
import arrow.core.raise.either
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import no.nav.tilbakekreving.infrastructure.auth.model.Enhetsnummer
import no.nav.tilbakekreving.infrastructure.auth.model.OboToken
import no.nav.tilbakekreving.infrastructure.client.entra.proxy.json.EnhetResponseJson
import org.slf4j.LoggerFactory
import java.net.URL

/**
 * Klient for å kommunisere med [Entra-proxy](https://github.com/navikt/entra-proxy).
 *
 * Entra-proxy forenkler kommunikasjon med Entra-APIet og tilbyr å hente ut diverse data om Entra-brukere.
 *
 * Kan brukes for å hente:
 * - Ansatts tematilganger
 * - Ansatts enhetstilhørighet
 * - Medlemmer i en bestemt enhet
 * - Medlemmer i en bestemt arkivtema-gruppe
 * - Hente utvidet informasjon om ansatt basert på navIdent eller T-ident
 * - Hente Ansattes grupper (bare SecEnabled)
 */
class EntraProxyClient(
    private val httpClient: HttpClient,
    private val baseUrl: URL,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun hentEnheter(oboToken: OboToken): Either<HentEnheterError, Set<Enhetsnummer>> =
        either {
            val result =
                httpClient.get("$baseUrl/api/v1/enhet") {
                    bearerAuth(oboToken.token)
                }

            when (result.status) {
                HttpStatusCode.OK -> {
                    result.body<List<EnhetResponseJson>>().map { it.enhetsnummer }.toSet()
                }

                else -> {
                    logger.error("Henting av enheter feilet: {} - {}", result.status, result.bodyAsText())
                    raise(HentEnheterError.FailedToFetchEnheter)
                }
            }
        }
}

sealed class HentEnheterError {
    data object FailedToFetchEnheter : HentEnheterError()
}
