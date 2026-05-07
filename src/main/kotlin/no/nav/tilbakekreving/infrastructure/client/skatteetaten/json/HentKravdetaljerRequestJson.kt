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
    val skatteetatensKravidentifikator: String,
    val oppdragsgiversKravidentifikator: String? = null,
) {
    companion object {
        fun from(kravidentifikator: Kravidentifikator): SkeKravRequestJson =
            SkeKravRequestJson(skatteetatensKravidentifikator = kravidentifikator.id)
    }
}
