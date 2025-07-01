package no.nav.tilbakekreving.infrastructure.route.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nav.tilbakekreving.domain.Kravfilter
import no.nav.tilbakekreving.domain.Skyldner
import no.nav.tilbakekreving.domain.Skyldner.Fødselnummer
import no.nav.tilbakekreving.domain.Skyldner.Organisasjonsnummer

@Serializable
sealed class HentKravoversiktJsonRequest {
    abstract val id: String
    abstract val kravfilter: KravfilterJson

    fun toDomain(): Pair<Skyldner, Kravfilter> = skyldner() to kravfilter.toDomain()

    private fun skyldner(): Skyldner =
        when (this) {
            is FnrJson -> Fødselnummer(id)
            is OrgnummerJson -> Organisasjonsnummer(id)
        }

    @Serializable
    enum class KravfilterJson {
        ALLE,
        ÅPNE,
        LUKKEDE,
        INGEN, ;

        fun toDomain(): Kravfilter =
            when (this) {
                ALLE -> Kravfilter.ALLE
                ÅPNE -> Kravfilter.ÅPNE
                LUKKEDE -> Kravfilter.LUKKEDE
                INGEN -> Kravfilter.INGEN
            }
    }

    @Serializable
    @SerialName("fødselsnummer")
    data class FnrJson(
        override val id: String,
        override val kravfilter: KravfilterJson,
    ) : HentKravoversiktJsonRequest()

    @Serializable
    @SerialName("organisasjonsnummer")
    data class OrgnummerJson(
        override val id: String,
        override val kravfilter: KravfilterJson,
    ) : HentKravoversiktJsonRequest()
}
