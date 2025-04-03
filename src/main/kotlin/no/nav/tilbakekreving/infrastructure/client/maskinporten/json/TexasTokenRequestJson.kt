package no.nav.tilbakekreving.infrastructure.client.maskinporten.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TexasTokenRequestJson(
    @SerialName("identity_provider")
    val identityProvider: String,
    val target: String,
)
