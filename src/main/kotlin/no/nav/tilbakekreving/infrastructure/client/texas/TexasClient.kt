package no.nav.tilbakekreving.infrastructure.client.texas

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
import no.nav.tilbakekreving.config.NaisConfig
import no.nav.tilbakekreving.infrastructure.client.texas.json.ExchangeTokenRequestJson
import no.nav.tilbakekreving.infrastructure.client.texas.json.GetTokenRequest
import no.nav.tilbakekreving.infrastructure.client.texas.json.IdentityProviderJson
import no.nav.tilbakekreving.infrastructure.client.texas.json.TexasTokenResponse
import no.nav.tilbakekreving.infrastructure.client.texas.json.ValidateTokenRequest
import no.nav.tilbakekreving.infrastructure.client.texas.json.ValidateTokenResponse
import org.slf4j.LoggerFactory

class TexasClient(
    private val httpClient: HttpClient,
    private val naisConfig: NaisConfig,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun getToken(
        identityProvider: String,
        target: String,
    ): Either<TexasError, TexasTokenResponse> =
        either {
            val response =
                httpClient.post(naisConfig.naisTokenEndpoint) {
                    contentType(ContentType.Application.Json)
                    setBody(
                        GetTokenRequest(
                            identityProvider = identityProvider,
                            target = target,
                        ),
                    )
                }

            if (!response.status.isSuccess()) {
                logger.error("Failed to get token from Texas: {} - {}", response.status, response.bodyAsText())
                raise(TexasError.RequestFailed)
            }

            response.body<TexasTokenResponse>()
        }

    suspend fun exchangeToken(
        identityProvider: IdentityProviderJson,
        target: String,
        userToken: String,
        skipCache: Boolean = false,
    ): Either<TexasError, TexasTokenResponse> =
        either {
            val response =
                httpClient.post(naisConfig.naisTokenExchangeEndpoint) {
                    contentType(ContentType.Application.Json)
                    setBody(
                        ExchangeTokenRequestJson(
                            identityProvider = identityProvider,
                            skipCache = skipCache,
                            target = target,
                            userToken = userToken,
                        ),
                    )
                }

            if (!response.status.isSuccess()) {
                logger.error("Failed to exchange token: {} - {}", response.status, response.bodyAsText())
                raise(TexasError.RequestFailed)
            }

            response.body<TexasTokenResponse>()
        }

    suspend fun introspectToken(
        identityProvider: String,
        token: String,
    ): Either<TexasError, ValidateTokenResponse> =
        either {
            val response =
                httpClient.post(naisConfig.naisTokenIntrospectionEndpoint) {
                    contentType(ContentType.Application.Json)
                    setBody(
                        ValidateTokenRequest(
                            identityProvider = identityProvider,
                            token = token,
                        ),
                    )
                }

            if (!response.status.isSuccess()) {
                logger.error("Failed to introspect token: {} - {}", response.status, response.bodyAsText())
                raise(TexasError.RequestFailed)
            }

            response.body<ValidateTokenResponse>()
        }
}

sealed class TexasError {
    data object RequestFailed : TexasError()
}
