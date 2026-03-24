package no.nav.tilbakekreving.infrastructure.client.texas.json

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExchangeTokenRequestJson
    @OptIn(ExperimentalSerializationApi::class)
    constructor(
        @SerialName("identity_provider") val identityProvider: IdentityProviderJson,
        @EncodeDefault @SerialName("skip_cache") val skipCache: Boolean? = false,
        val target: String,
        @SerialName("user_token") val userToken: String,
)
