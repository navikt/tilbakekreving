package no.nav.tilbakekreving.domain

@JvmInline
value class SkyldnerId(
    val value: String,
)

data class Skyldner(
    val skyldnerId: SkyldnerId,
)
