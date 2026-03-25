package no.nav.tilbakekreving.infrastructure.client.texas.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetTokenRequest(
    @SerialName("identity_provider") val identityProvider: String,
    val target: String,
)
