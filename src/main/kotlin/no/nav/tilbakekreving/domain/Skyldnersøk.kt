package no.nav.tilbakekreving.domain

data class Skyldnersøk(
    val skyldner: Skyldner,
    val kravfilter: Kravfilter,
)
