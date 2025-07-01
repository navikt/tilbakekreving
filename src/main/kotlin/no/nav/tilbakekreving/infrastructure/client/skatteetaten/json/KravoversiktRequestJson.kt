package no.nav.tilbakekreving.infrastructure.client.skatteetaten.json

import kotlinx.serialization.Serializable
import no.nav.tilbakekreving.domain.Kravfilter
import no.nav.tilbakekreving.domain.Skyldner

@Serializable
data class HentKravoversiktRequestJson(
    val skyldner: String,
    val kravfilter: String,
) {
    companion object {
        fun from(
            skyldner: Skyldner,
            kravfilter: Kravfilter,
        ): HentKravoversiktRequestJson =
            HentKravoversiktRequestJson(
                skyldner = skyldner.id,
                kravfilter = kravfilter.name.lowercase(),
            )
    }
}
