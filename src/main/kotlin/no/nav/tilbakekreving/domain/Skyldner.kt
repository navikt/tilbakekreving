package no.nav.tilbakekreving.domain

@JvmInline
value class SkyldnerId(
    val value: String,
)

sealed class Skyldner {
    abstract val id: SkyldnerId

    data class Fødselnummer(
        override val id: SkyldnerId,
    ) : Skyldner()

    data class Organisasjonsnummer(
        override val id: SkyldnerId,
    ) : Skyldner()
}
