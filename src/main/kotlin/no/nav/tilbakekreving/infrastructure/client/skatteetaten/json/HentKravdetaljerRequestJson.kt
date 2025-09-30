package no.nav.tilbakekreving.infrastructure.client.skatteetaten.json

import kotlinx.serialization.Serializable
import no.nav.tilbakekreving.domain.Kravidentifikator

@Serializable
data class HentKravdetaljerRequestJson(
    val krav: KravRequestJson,
) {
    companion object {
        fun from(kravidentifikator: Kravidentifikator): HentKravdetaljerRequestJson =
            HentKravdetaljerRequestJson(
                krav = KravRequestJson.from(kravidentifikator),
            )
    }
}

@Serializable
data class KravRequestJson(
    val skatteetatensKravidentifikator: String? = null,
    val oppdragsgiversKravidentifikator: String? = null,
) {
    companion object {
        fun from(kravidentifikator: Kravidentifikator): KravRequestJson =
            when (kravidentifikator) {
                is Kravidentifikator.Nav -> KravRequestJson(oppdragsgiversKravidentifikator = kravidentifikator.id)
                is Kravidentifikator.Skatteetaten -> KravRequestJson(skatteetatensKravidentifikator = kravidentifikator.id)
            }
    }
}
