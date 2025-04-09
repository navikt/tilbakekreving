package no.nav.tilbakekreving.domain

sealed class Skyldner {
    abstract val id: String

    data class Fødselnummer(
        override val id: String,
    ) : Skyldner()

    data class Organisasjonsnummer(
        override val id: String,
    ) : Skyldner()
}