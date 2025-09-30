package no.nav.tilbakekreving.infrastructure.client.skatteetaten.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nav.tilbakekreving.domain.Kravlinje
import no.nav.tilbakekreving.domain.MultiSpråkTekst
import no.nav.tilbakekreving.domain.SpråkTekst

@Serializable
data class KravlinjeResponseJson(
    val kravlinjetype: String,
    @SerialName("opprinneligBeloep")
    val opprinneligBeløp: Double,
    @SerialName("gjenstaaendeBeloep")
    val gjenståendeBeløp: Double,
    val kravlinjeBeskrivelse: MultiSpråkTekstResponseJson? = null,
) {
    fun toDomain(): Kravlinje =
        Kravlinje(
            kravlinjetype = kravlinjetype,
            opprinneligBeløp = opprinneligBeløp,
            gjenståendeBeløp = gjenståendeBeløp,
            kravlinjeBeskrivelse = kravlinjeBeskrivelse?.toDomain(),
        )
}

@Serializable
data class MultiSpråkTekstResponseJson(
    @SerialName("spraakTekst")
    val språkTekst: List<SpråkTekstResponseJson>,
) {
    fun toDomain(): MultiSpråkTekst =
        MultiSpråkTekst(
            språkTekst = språkTekst.map { it.toDomain() },
        )
}

@Serializable
data class SpråkTekstResponseJson(
    val tekst: String,
    @SerialName("spraak")
    val språk: String,
) {
    fun toDomain(): SpråkTekst =
        SpråkTekst(
            tekst = tekst,
            språk = språk,
        )
}
