package no.nav.tilbakekreving.infrastructure.client.entra.proxy.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nav.tilbakekreving.infrastructure.auth.model.Enhetsnummer

@Serializable
data class EnhetResponseJson(
    @SerialName("enhetnummer")
    val enhetsnummer: Enhetsnummer,
    val navn: String,
)
