package no.nav.tilbakekreving.infrastructure.client.maskinporten

import arrow.core.Either
import arrow.core.raise.either
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import no.nav.tilbakekreving.infrastructure.client.AccessTokenProvider
import no.nav.tilbakekreving.infrastructure.client.maskinporten.json.TexasTokenRequestJson
import no.nav.tilbakekreving.infrastructure.client.maskinporten.json.TexasTokenResponseJson
import org.slf4j.LoggerFactory

/**
 * Klient for å hente access token fra Maskinporten.
 *
 * Klienten henter tokenet fra [Texas](https://docs.nais.io/auth/explanations/#texas) (Token Exchange as a Service) som kjører som en sidecar.
 */
class TexasMaskinportenClient(
    private val httpClient: HttpClient,
    private val baseUrl: String,
) : AccessTokenProvider {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun getAccessToken(vararg scopes: String): Either<AccessTokenProvider.GetAccessTokenError, String> =
        either {
            val response =
                httpClient.post(baseUrl) {
                    contentType(ContentType.Application.Json)
                    setBody(
                        TexasTokenRequestJson(
                            identityProvider = "maskinporten",
                            target = scopes.joinToString(" "),
                        ),
                    )
                }

            if (!response.status.isSuccess()) {
                logger.error("Failed to get access token from Texas: {} - {}", response.status, response.bodyAsText())
                raise(AccessTokenProvider.GetAccessTokenError.FailedToGetAccessToken)
            } else {
                response.body<TexasTokenResponseJson>().accessToken
            }
        }
}
