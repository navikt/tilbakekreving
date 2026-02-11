package no.nav.tilbakekreving.infrastructure.client.skatteetaten.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nav.tilbakekreving.domain.Kravfilter
import no.nav.tilbakekreving.domain.Skyldnersøk

@Serializable
data class SkeHentKravoversiktRequestJson(
    val skyldner: String,
    val kravfilter: SkeKravfilterJson,
) {
    companion object {
        fun from(skyldnersøk: Skyldnersøk): SkeHentKravoversiktRequestJson =
            SkeHentKravoversiktRequestJson(
                skyldner = skyldnersøk.skyldner.skyldnerId.value,
                kravfilter = SkeKravfilterJson.fromDomain(skyldnersøk.kravfilter),
            )
    }
}

@Serializable
enum class SkeKravfilterJson {
    @SerialName("alle")
    ALLE,

    @SerialName("åpne")
    ÅPNE,

    @SerialName("lukkede")
    LUKKEDE,

    @SerialName("ingen")
    INGEN, ;

    companion object {
        fun fromDomain(kravfilter: Kravfilter): SkeKravfilterJson =
            when (kravfilter) {
                Kravfilter.ALLE -> ALLE
                Kravfilter.ÅPNE -> ÅPNE
                Kravfilter.LUKKEDE -> LUKKEDE
                Kravfilter.INGEN -> INGEN
            }
    }
}
