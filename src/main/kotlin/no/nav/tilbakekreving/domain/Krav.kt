package no.nav.tilbakekreving.domain

data class Krav(
    val kravidentifikator: Kravidentifikator,
    val kravtype: Kravtype,
)

@JvmInline
value class Kravtype(
    val value: String,
)
