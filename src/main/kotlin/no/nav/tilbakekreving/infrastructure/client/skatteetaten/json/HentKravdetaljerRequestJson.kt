package no.nav.tilbakekreving.infrastructure.client.skatteetaten.json

import kotlinx.serialization.Serializable
import no.nav.tilbakekreving.domain.Kravidentifikator

@Serializable
data class SkeHentKravdetaljerRequestJson(
    val krav: SkeKravRequestJson,
) {
    companion object {
        fun from(kravidentifikator: Kravidentifikator): SkeHentKravdetaljerRequestJson =
            SkeHentKravdetaljerRequestJson(
                krav = SkeKravRequestJson.from(kravidentifikator),
            )
    }
}

@Serializable
data class SkeKravRequestJson(
    val skatteetatensKravidentifikator: String? = null,
    val oppdragsgiversKravidentifikator: String? = null,
) {
    companion object {
        fun from(kravidentifikator: Kravidentifikator): SkeKravRequestJson =
            when (kravidentifikator) {
                is Kravidentifikator.Nav -> SkeKravRequestJson(oppdragsgiversKravidentifikator = kravidentifikator.id)
                is Kravidentifikator.Skatteetaten -> SkeKravRequestJson(skatteetatensKravidentifikator = kravidentifikator.id)
            }
    }
}
