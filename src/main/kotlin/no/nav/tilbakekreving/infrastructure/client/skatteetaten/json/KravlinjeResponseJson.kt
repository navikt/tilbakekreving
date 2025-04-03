package no.nav.tilbakekreving.infrastructure.client.skatteetaten.json

import kotlinx.serialization.Serializable
import no.nav.tilbakekreving.domain.Kravlinje

@Serializable
data class KravlinjeResponseJson(
    val kravlinjetype: String,
    val opprinneligBeloep: Double,
    val gjenstaaendeBeloep: Double?,
) {
    fun toDomain(): Kravlinje =
        Kravlinje(
            kravlinjetype = kravlinjetype,
            opprinneligBeloep = opprinneligBeloep,
            gjenstaaendeBeloep = gjenstaaendeBeloep,
        )
}
