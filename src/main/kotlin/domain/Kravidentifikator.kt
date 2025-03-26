package no.nav.domain

sealed class Kravidentifikator {
    abstract val id: String

    data class NavsKravidentifikator(
        override val id: String,
    ) : Kravidentifikator()

    data class SkatteetatensKravidentifikator(
        override val id: String,
    ) : Kravidentifikator()
}