package no.nav.tilbakekreving.infrastructure.client

import arrow.core.raise.either
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import no.nav.tilbakekreving.infrastructure.auth.NavUserPrincipal
import no.nav.tilbakekreving.infrastructure.client.json.VerifyTokenRequest
import no.nav.tilbakekreving.infrastructure.client.json.VerifyTokenResponse
import org.slf4j.LoggerFactory

class TexasClient(
    private val httpClient: HttpClient,
    private val baseUrl: String,
) : AccessTokenVerifier<NavUserPrincipal> {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun verifyToken(token: String) =
        either {
            val response =
                httpClient.post(baseUrl) {
                    contentType(ContentType.Application.Json)
                    setBody(
                        VerifyTokenRequest(
                            identityProvider = "azuread",
                            token = token,
                        ),
                    )
                }

            if (!response.status.isSuccess()) {
                logger.error("Failed to verify token: {} - {}", response.status, response.bodyAsText())
                raise(AccessTokenVerifier.VerificationError.FailedToVerifyToken)
            } else {
                when (val verifyTokenResponse = response.body<VerifyTokenResponse>()) {
                    is VerifyTokenResponse.ValidTokenResponse -> {
                        verifyTokenResponse.toDomain()
                    }

                    is VerifyTokenResponse.InvalidTokenResponse -> {
                        logger.info("Token is invalid: ${verifyTokenResponse.error}")
                        raise(AccessTokenVerifier.VerificationError.InvalidToken)
                    }
                }
            }
        }
}
