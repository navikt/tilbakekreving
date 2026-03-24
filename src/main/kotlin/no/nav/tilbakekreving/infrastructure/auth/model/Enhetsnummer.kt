package no.nav.tilbakekreving.infrastructure.auth.model

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class Enhetsnummer(
    val value: String,
) {
    init {
        require(value.matches(Regex("""\d{4}"""))) { "Enhetsnummer må være på 4 siffer" }
    }
}
