package no.nav.tilbakekreving.infrastructure.client.skatteetaten.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nav.tilbakekreving.domain.Kravfilter
import no.nav.tilbakekreving.domain.Skyldnersøk

@Serializable
data class HentKravoversiktRequestJson(
    val skyldner: String,
    val kravfilter: KravfilterJson,
) {
    companion object {
        fun from(skyldnersøk: Skyldnersøk): HentKravoversiktRequestJson =
            HentKravoversiktRequestJson(
                skyldner = skyldnersøk.skyldner.skyldnerId.value,
                kravfilter = KravfilterJson.fromDomain(skyldnersøk.kravfilter),
            )
    }
}

@Serializable
enum class KravfilterJson {
    @SerialName("alle")
    ALLE,

    @SerialName("åpne")
    ÅPNE,

    @SerialName("lukkede")
    LUKKEDE,

    @SerialName("ingen")
    INGEN, ;

    companion object {
        fun fromDomain(kravfilter: Kravfilter): KravfilterJson =
            when (kravfilter) {
                Kravfilter.ALLE -> ALLE
                Kravfilter.ÅPNE -> ÅPNE
                Kravfilter.LUKKEDE -> LUKKEDE
                Kravfilter.INGEN -> INGEN
            }
    }
}
