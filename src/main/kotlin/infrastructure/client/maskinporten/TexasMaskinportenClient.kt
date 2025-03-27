package no.nav.infrastructure.client.maskinporten

import arrow.core.Either
import arrow.core.raise.either
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import no.nav.infrastructure.client.maskinporten.json.TexasTokenRequestJson
import no.nav.infrastructure.client.maskinporten.json.TexasTokenResponseJson
import org.slf4j.LoggerFactory

/**
 * Klient for å hente access token fra Maskinporten.
 *
 * Klienten henter tokenet fra [Texas](https://docs.nais.io/auth/explanations/#texas) (Token Exchange as a Service) som kjører som en sidecar.
 */
class TexasMaskinportenClient(
    private val httpClient: HttpClient,
    private val baseUrl: String,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun getAccessToken(vararg scopes: String): Either<GetAccessTokenError, TexasTokenResponseJson> =
        either {
            val response =
                httpClient.post(baseUrl) {
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                    setBody(
                        TexasTokenRequestJson(
                            identityProvider = "maskinporten",
                            target = scopes.joinToString(" "),
                        ),
                    )
                }

            if (response.status.value !in 200..299) {
                logger.error("Failed to get access token from Texas: ${response.status} - ${response.bodyAsText()}")
                raise(GetAccessTokenError.FailedToGetAccessToken)
            } else {
                response.body<TexasTokenResponseJson>()
            }
        }
}

sealed class GetAccessTokenError {
    data object FailedToGetAccessToken : GetAccessTokenError()
}
