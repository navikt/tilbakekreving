package no.nav.tilbakekreving.domain

import kotlinx.datetime.LocalDate
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory store for tilleggsfrist values for Kravdetaljer.
 * This is a temporary solution until the field is available from the Skatteetaten API.
 */
class TilleggsfristStore {
    private val store = ConcurrentHashMap<Kravidentifikator, LocalDate>()

    fun getTilleggsfrist(kravidentifikator: Kravidentifikator): LocalDate? = store[kravidentifikator]

    fun setTilleggsfrist(
        kravidentifikator: Kravidentifikator,
        tilleggsfrist: LocalDate,
    ) {
        store[kravidentifikator] = tilleggsfrist
    }
}
