package no.nav.tilbakekreving.infrastructure.route.json

import kotlinx.serialization.Serializable
import no.nav.tilbakekreving.domain.Kravidentifikator

@Serializable
data class HentKravdetaljerJsonRequest(
    val id: String,
    val type: KravidentifikatorType,
) {
    fun toDomain(): Kravidentifikator =
        when (type) {
            KravidentifikatorType.NAV -> Kravidentifikator.Nav(id)
            KravidentifikatorType.SKATTEETATEN -> Kravidentifikator.Skatteetaten(id)
        }
}

@Serializable
enum class KravidentifikatorType {
    NAV,
    SKATTEETATEN,
}
