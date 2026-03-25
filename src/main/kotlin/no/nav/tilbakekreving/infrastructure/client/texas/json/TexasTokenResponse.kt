package no.nav.tilbakekreving.infrastructure.client.texas.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TexasTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_in") val expiresIn: Long,
    @SerialName("token_type") val tokenType: String,
)
