package no.nav.tilbakekreving.infrastructure.route.json

import kotlinx.serialization.Serializable
import no.nav.tilbakekreving.domain.Kravfilter
import no.nav.tilbakekreving.domain.Skyldner
import no.nav.tilbakekreving.domain.SkyldnerId
import no.nav.tilbakekreving.domain.Skyldnersøk

@Serializable
data class HentKravoversiktJsonRequest(
    val skyldner: String,
    val kravfilter: KravfilterJson,
) {
    fun toDomain(): Skyldnersøk =
        Skyldnersøk(
            skyldner = Skyldner(SkyldnerId(skyldner)),
            kravfilter = kravfilter.toDomain(),
        )
}

@Serializable
enum class KravfilterJson {
    ALLE,
    ÅPNE,
    LUKKEDE,
    INGEN, ;

    fun toDomain(): Kravfilter =
        when (this) {
            ALLE -> Kravfilter.ALLE
            ÅPNE -> Kravfilter.ÅPNE
            LUKKEDE -> Kravfilter.LUKKEDE
            INGEN -> Kravfilter.INGEN
        }
}
