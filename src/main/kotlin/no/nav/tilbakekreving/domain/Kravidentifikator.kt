package no.nav.tilbakekreving.domain

sealed class Kravidentifikator {
    abstract val id: String

    data class Nav(
        override val id: String,
    ) : Kravidentifikator()

    data class Skatteetaten(
        override val id: String,
    ) : Kravidentifikator()
}
