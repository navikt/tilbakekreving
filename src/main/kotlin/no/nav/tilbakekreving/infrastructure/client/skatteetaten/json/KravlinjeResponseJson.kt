package no.nav.tilbakekreving.infrastructure.client.skatteetaten.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nav.tilbakekreving.domain.Kravlinje
import no.nav.tilbakekreving.domain.Kravlinjebeskrivelse
import java.util.Locale

@Serializable
data class SkeKravlinjeResponseJson(
    val kravlinjetype: String,
    @SerialName("opprinneligBeloep")
    val opprinneligBeløp: Double,
    @SerialName("gjenstaaendeBeloep")
    val gjenståendeBeløp: Double,
    val kravlinjeBeskrivelse: SkeMultiSpråkTekstResponseJson? = null,
) {
    fun toDomain(): Kravlinje =
        Kravlinje(
            kravlinjetype = kravlinjetype,
            opprinneligBeløp = opprinneligBeløp,
            gjenståendeBeløp = gjenståendeBeløp,
            kravlinjeBeskrivelse =
                kravlinjeBeskrivelse?.språkTekst?.associate {
                    Locale.forLanguageTag(it.språk) to
                        Kravlinjebeskrivelse(
                            it.tekst,
                        )
                } ?: emptyMap(),
        )
}

@Serializable
data class SkeMultiSpråkTekstResponseJson(
    @SerialName("spraakTekst")
    val språkTekst: List<SkeSpråkTekstResponseJson>,
)

@Serializable
data class SkeSpråkTekstResponseJson(
    val tekst: String,
    @SerialName("spraak")
    val språk: String,
)
