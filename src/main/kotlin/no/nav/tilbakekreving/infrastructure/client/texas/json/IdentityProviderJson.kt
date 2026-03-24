package no.nav.tilbakekreving.infrastructure.client.texas.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class IdentityProviderJson {
    @SerialName("entra_id")
    ENTRA_ID,

    @SerialName("tokenx")
    TOKENX,

    @SerialName("maskinporten")
    MASKINPORTEN,

    @SerialName("idporten")
    IDPORTEN,
}
