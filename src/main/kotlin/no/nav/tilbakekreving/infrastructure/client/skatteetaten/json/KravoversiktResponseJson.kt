package no.nav.tilbakekreving.infrastructure.client.skatteetaten.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nav.tilbakekreving.domain.Krav
import no.nav.tilbakekreving.domain.Kravidentifikator

@Serializable
data class HentKravoversiktResponseJson(
    val krav: List<KravJson>,
) {
    fun toDomain(): List<Krav> = krav.map(KravJson::toDomain)
}

@Serializable
data class KravJson(
    val kravidentifikator: String,
    val oppdragsgiverKravidentifikator: String,
    val kravtype: String,
    val kravbeskrivelse: KravbeskrivelseJson,
) {
    fun toDomain(): Krav =
        Krav(
            kravidentifikator = Kravidentifikator.Nav(kravidentifikator),
            kravtype = kravtype,
        )
}

@Serializable
data class KravbeskrivelseJson(
    @SerialName("spraakTekst")
    val språkTekst: List<SpråkTekstJson>,
)

@Serializable
data class SpråkTekstJson(
    val tekst: String,
    @SerialName("spraak")
    val språk: String,
)
