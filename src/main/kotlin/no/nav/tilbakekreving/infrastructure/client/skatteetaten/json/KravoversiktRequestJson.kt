package no.nav.tilbakekreving.infrastructure.client.skatteetaten.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nav.tilbakekreving.domain.Skyldner

@Serializable
data class HentKravoversiktRequestJson(
    val skyldner: String,
    val kravfilter: KravfilterJson,
) {
    companion object {
        fun from(skyldner: Skyldner): HentKravoversiktRequestJson =
            HentKravoversiktRequestJson(
                skyldner = skyldner.id.value,
                // TODO: Sett kravfilter når det er lagt til i APIet og sendt gjennom applikasjonslaget
                kravfilter = KravfilterJson.ALLE,
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
    INGEN,
}
