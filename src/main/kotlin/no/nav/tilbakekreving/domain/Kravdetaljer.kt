package no.nav.tilbakekreving.domain

import kotlinx.datetime.LocalDate

data class Kravdetaljer(
    val krav: KravDetalj,
    val oppdragsgiver: Oppdragsgiver,
    val skyldner: KravdetaljerSkyldner,
    val avvik: Avvik? = null,
)

data class KravDetalj(
    val forfallsdato: LocalDate,
    val foreldelsesdato: LocalDate,
    val fastsettelsesdato: LocalDate,
    val kravtype: String,
    val opprinneligBeløp: Double,
    val gjenståendeBeløp: Double,
    val skatteetatensKravidentifikator: String,
    val kravlinjer: List<Kravlinje>,
    val kravgrunnlag: Kravgrunnlag,
    val innbetalingerPlassertMotKrav: List<InnbetalingPlassertMotKrav> = emptyList(),
    val tilleggsinformasjon: Tilleggsinformasjon? = null,
)

data class KravdetaljerSkyldner(
    val identifikator: String,
    val skyldnersNavn: String? = null,
)

data class Avvik(
    val avvikstype: Avvikstype,
    val utdypendeAvviksbeskrivelse: String,
)

enum class Avvikstype {
    TEKNISK_FEIL,
    PLANLAGT_NEDETID,
}

data class InnbetalingPlassertMotKrav(
    val innbetalingsIdentifikator: String,
    val innbetalingstype: String,
    val innbetalingsdato: LocalDate,
    val innbetaltBeløp: Double,
)

sealed class Tilleggsinformasjon {
    data class BrønnøysundRegistrene(
        val periode: PeriodeMedTvangsmulkt,
        val stoppdatoForLøpendeMulkt: LocalDate? = null,
    ) : Tilleggsinformasjon()

    data class Nav(
        val ytelserForAvregning: YtelseForAvregningBeløp? = null,
        val tilbakekrevingsperiode: Tilbakekrevingsperiode,
    ) : Tilleggsinformasjon()
}

data class PeriodeMedTvangsmulkt(
    val fom: LocalDate,
    val tom: LocalDate,
)

data class YtelseForAvregningBeløp(
    val valuta: String,
    val beløp: Long,
)

data class Tilbakekrevingsperiode(
    val fom: LocalDate,
    val tom: LocalDate,
)
