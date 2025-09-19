package no.nav.tilbakekreving.infrastructure.route.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nav.tilbakekreving.domain.Skyldner
import no.nav.tilbakekreving.domain.SkyldnerId

@Serializable
sealed class HentKravoversiktJsonRequest {
    abstract val id: String

    fun toDomain(): Skyldner =
        when (this) {
            is FnrJson -> Skyldner.Fødselnummer(SkyldnerId(id))
            is OrgnummerJson -> Skyldner.Organisasjonsnummer(SkyldnerId(id))
        }

    @Serializable
    @SerialName("fødselsnummer")
    data class FnrJson(
        override val id: String,
    ) : HentKravoversiktJsonRequest()

    @Serializable
    @SerialName("organisasjonsnummer")
    data class OrgnummerJson(
        override val id: String,
    ) : HentKravoversiktJsonRequest()
}
