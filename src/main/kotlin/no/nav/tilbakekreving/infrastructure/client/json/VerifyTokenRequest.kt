package no.nav.tilbakekreving.infrastructure.client.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerifyTokenRequest(
    @SerialName("identity_provider") val identityProvider: String,
    val token: String,
)
