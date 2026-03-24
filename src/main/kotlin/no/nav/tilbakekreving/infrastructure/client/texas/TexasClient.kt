package no.nav.tilbakekreving.infrastructure.client.texas

import arrow.core.Either
import arrow.core.raise.either
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import no.nav.tilbakekreving.config.NaisConfig
import no.nav.tilbakekreving.infrastructure.auth.AccessTokenValidator
import no.nav.tilbakekreving.infrastructure.auth.model.OboToken
import no.nav.tilbakekreving.infrastructure.auth.model.ValidatedEntraToken
import no.nav.tilbakekreving.infrastructure.client.texas.json.ExchangeTokenRequestJson
import no.nav.tilbakekreving.infrastructure.client.texas.json.ExchangeTokenResponseJson
import no.nav.tilbakekreving.infrastructure.client.texas.json.IdentityProviderJson
import no.nav.tilbakekreving.infrastructure.client.texas.json.ValidateTokenRequest
import no.nav.tilbakekreving.infrastructure.client.texas.json.ValidateTokenResponse
import org.slf4j.LoggerFactory

class TexasClient(
    private val httpClient: HttpClient,
    private val naisConfig: NaisConfig,
) : AccessTokenValidator<ValidatedEntraToken> {
    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun exchangeToken(
        userToken: String,
        target: String,
    ): Either<ExchangeTokenError, OboToken> =
        either {
            val response =
                httpClient.post(naisConfig.naisTokenExchangeEndpoint) {
                    contentType(ContentType.Application.Json)
                    setBody(
                        ExchangeTokenRequestJson(
                            identityProvider = IdentityProviderJson.ENTRA_ID,
                            skipCache = false,
                            target = target,
                            userToken = userToken,
                        ),
                    )
                }

            when (response.status) {
                HttpStatusCode.OK -> {
                    OboToken(response.body<ExchangeTokenResponseJson>().accessToken)
                }

                else -> {
                    logger.error("Failed to exchange token: {} - {}", response.status, response.bodyAsText())
                    raise(ExchangeTokenError.FailedToExchangeToken)
                }
            }
        }

    override suspend fun validateToken(token: String) =
        either {
            val response =
                httpClient.post(naisConfig.naisTokenIntrospectionEndpoint) {
                    contentType(ContentType.Application.Json)
                    setBody(
                        ValidateTokenRequest(
                            identityProvider = "azuread",
                            token = token,
                        ),
                    )
                }

            if (!response.status.isSuccess()) {
                logger.error("Failed to validate token: {} - {}", response.status, response.bodyAsText())
                raise(AccessTokenValidator.ValidationError.FailedToValidateToken)
            } else {
                when (val validateTokenResponse = response.body<ValidateTokenResponse>()) {
                    is ValidateTokenResponse.ValidTokenResponse -> {
                        validateTokenResponse.toDomain()
                    }

                    is ValidateTokenResponse.InvalidTokenResponse -> {
                        logger.info("Token is invalid: ${validateTokenResponse.error}")
                        raise(AccessTokenValidator.ValidationError.InvalidToken)
                    }
                }
            }
        }
}

sealed class ExchangeTokenError {
    data object FailedToExchangeToken : ExchangeTokenError()
}
