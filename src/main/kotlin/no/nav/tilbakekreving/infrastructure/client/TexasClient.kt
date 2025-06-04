package no.nav.tilbakekreving.infrastructure.client

import arrow.core.raise.either
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nav.tilbakekreving.infrastructure.auth.GroupId
import org.slf4j.LoggerFactory

class TexasClient(
    private val httpClient: HttpClient,
    private val baseUrl: String,
) : AccessTokenVerifier {
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

            if (response.status.value !in 200..299) {
                logger.error("Failed to verify token: ${response.status} - ${response.bodyAsText()}")
                raise(AccessTokenVerifier.VerificationError.FailedToVerifyToken)
            } else {
                when (val verifyTokenResponse = response.body<VerifyTokenResponse>()) {
                    is VerifyTokenResponse.ValidTokenResponse -> verifyTokenResponse.toDomain()
                    is VerifyTokenResponse.InvalidTokenResponse -> {
                        logger.info("Token is invalid: ${verifyTokenResponse.error}")
                        raise(AccessTokenVerifier.VerificationError.InvalidToken)
                    }
                }
            }
        }
}

@Serializable
data class VerifyTokenRequest(
    @SerialName("identity_provider") val identityProvider: String,
    val token: String,
)

@Serializable
sealed class VerifyTokenResponse {
    abstract val active: Boolean

    @Serializable
    @SerialName("valid")
    data class ValidTokenResponse(
        override val active: Boolean,
        val exp: Long,
        val iat: Long,
        val groups: List<String>,
    ) : VerifyTokenResponse() {
        fun toDomain(): AccessTokenVerifier.ValidatedToken =
            AccessTokenVerifier.ValidatedToken(
                groups.map(::GroupId),
            )
    }

    @Serializable
    @SerialName("invalid")
    data class InvalidTokenResponse(
        override val active: Boolean,
        val error: String,
    ) : VerifyTokenResponse()
}
