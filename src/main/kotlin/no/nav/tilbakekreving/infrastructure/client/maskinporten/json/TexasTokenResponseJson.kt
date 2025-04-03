package no.nav.tilbakekreving.infrastructure.client.maskinporten.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TexasTokenResponseJson(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("expires_in")
    val expiresIn: Int,
    @SerialName("token_type")
    val tokenType: String,
)
