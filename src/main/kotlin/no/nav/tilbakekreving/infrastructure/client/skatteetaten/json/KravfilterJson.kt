package no.nav.tilbakekreving.infrastructure.client.skatteetaten.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nav.tilbakekreving.domain.Kravfilter

@Serializable
enum class KravfilterJson {
    @SerialName("alle")
    ALLE,

    @SerialName("åpne")
    ÅPNE,

    @SerialName("lukkede")
    LUKKEDE,

    @SerialName("ingen")
    INGEN,

    ;

    companion object {
        fun from(kravfilter: Kravfilter): KravfilterJson =
            when (kravfilter) {
                Kravfilter.ALLE -> ALLE
                Kravfilter.ÅPNE -> ÅPNE
                Kravfilter.LUKKEDE -> LUKKEDE
                Kravfilter.INGEN -> INGEN
            }
    }
}
