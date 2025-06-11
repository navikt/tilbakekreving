package no.nav.tilbakekreving.infrastructure.route.json

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import no.nav.tilbakekreving.domain.Kravidentifikator

@Serializable
data class OppdaterTilleggsfristJsonRequest(
    val id: String,
    val type: KravidentifikatorType,
    val tilleggsfrist: String,
) {
    fun toDomain(): Pair<Kravidentifikator, LocalDate> {
        val kravidentifikator =
            when (type) {
                KravidentifikatorType.NAV -> Kravidentifikator.Nav(id)
                KravidentifikatorType.SKATTEETATEN -> Kravidentifikator.Skatteetaten(id)
            }
        val tilleggsfristDate = LocalDate.parse(tilleggsfrist)
        return Pair(kravidentifikator, tilleggsfristDate)
    }
}
