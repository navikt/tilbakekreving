package no.nav.tilbakekreving.infrastructure.client.entra.proxy.json

import kotlinx.serialization.Serializable
import no.nav.tilbakekreving.infrastructure.auth.model.Enhetsnummer

@Serializable
data class EnhetResponseJson(
    val enhetsnummer: Enhetsnummer,
    val navn: String,
)
