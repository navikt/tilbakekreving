package no.nav.tilbakekreving.infrastructure.route.json

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import no.nav.tilbakekreving.domain.Avvik
import no.nav.tilbakekreving.domain.Avvikstype
import no.nav.tilbakekreving.domain.InnbetalingPlassertMotKrav
import no.nav.tilbakekreving.domain.KravDetalj
import no.nav.tilbakekreving.domain.Kravdetaljer
import no.nav.tilbakekreving.domain.KravdetaljerSkyldner
import no.nav.tilbakekreving.domain.Kravgrunnlag
import no.nav.tilbakekreving.domain.Kravlinje
import no.nav.tilbakekreving.domain.Oppdragsgiver
import no.nav.tilbakekreving.domain.PeriodeMedTvangsmulkt
import no.nav.tilbakekreving.domain.Tilbakekrevingsperiode
import no.nav.tilbakekreving.domain.Tilleggsinformasjon
import no.nav.tilbakekreving.domain.YtelseForAvregningBeløp

@Serializable
data class HentKravdetaljerJsonResponse(
    val krav: KravDetaljResponseJson,
    val oppdragsgiver: OppdragsgiverResponseJson,
    val skyldner: KravdetaljerSkyldnerResponseJson,
    val avvik: AvvikResponseJson? = null,
) {
    companion object {
        fun fromDomain(kravdetaljer: Kravdetaljer): HentKravdetaljerJsonResponse =
            HentKravdetaljerJsonResponse(
                krav = KravDetaljResponseJson.fromDomain(kravdetaljer.krav),
                oppdragsgiver = OppdragsgiverResponseJson.fromDomain(kravdetaljer.oppdragsgiver),
                skyldner = KravdetaljerSkyldnerResponseJson.fromDomain(kravdetaljer.skyldner),
                avvik = kravdetaljer.avvik?.let { AvvikResponseJson.fromDomain(it) },
            )
    }
}

@Serializable
data class KravDetaljResponseJson(
    val forfallsdato: LocalDate?,
    val foreldelsesdato: LocalDate?,
    val fastsettelsesdato: LocalDate?,
    val kravtype: String,
    val `opprinneligBeløp`: Double,
    val `gjenståendeBeløp`: Double,
    val skatteetatensKravidentifikator: String?,
    val kravlinjer: List<KravlinjeResponseJson> = emptyList(),
    val kravgrunnlag: KravgrunnlagResponseJson,
    val innbetalingerPlassertMotKrav: List<InnbetalingPlassertMotKravResponseJson> = emptyList(),
    val tilleggsinformasjon: TilleggsinformasjonResponseJson? = null,
) {
    companion object {
        fun fromDomain(krav: KravDetalj): KravDetaljResponseJson =
            KravDetaljResponseJson(
                forfallsdato = krav.forfallsdato,
                foreldelsesdato = krav.foreldelsesdato,
                fastsettelsesdato = krav.fastsettelsesdato,
                kravtype = krav.kravtype,
                `opprinneligBeløp` = krav.opprinneligBeløp,
                `gjenståendeBeløp` = krav.gjenståendeBeløp,
                skatteetatensKravidentifikator = krav.skatteetatensKravidentifikator,
                kravlinjer = krav.kravlinjer.map { KravlinjeResponseJson.fromDomain(it) },
                kravgrunnlag = KravgrunnlagResponseJson.fromDomain(krav.kravgrunnlag),
                innbetalingerPlassertMotKrav =
                    krav.innbetalingerPlassertMotKrav.map {
                        InnbetalingPlassertMotKravResponseJson.fromDomain(
                            it,
                        )
                    },
                tilleggsinformasjon = krav.tilleggsinformasjon?.let { TilleggsinformasjonResponseJson.fromDomain(it) },
            )
    }
}

@Serializable
data class OppdragsgiverResponseJson(
    val organisasjonsnummer: String,
    val organisasjonsnavn: String?,
) {
    companion object {
        fun fromDomain(oppdragsgiver: Oppdragsgiver): OppdragsgiverResponseJson =
            OppdragsgiverResponseJson(
                organisasjonsnummer = oppdragsgiver.organisasjonsnummer,
                organisasjonsnavn = oppdragsgiver.organisasjonsnavn,
            )
    }
}

@Serializable
data class KravdetaljerSkyldnerResponseJson(
    val identifikator: String,
    val skyldnersNavn: String?,
) {
    companion object {
        fun fromDomain(skyldner: KravdetaljerSkyldner): KravdetaljerSkyldnerResponseJson =
            KravdetaljerSkyldnerResponseJson(
                identifikator = skyldner.identifikator,
                skyldnersNavn = skyldner.skyldnersNavn,
            )
    }
}

@Serializable
data class AvvikResponseJson(
    val avvikstype: String,
    val utdypendeAvviksbeskrivelse: String,
) {
    companion object {
        fun fromDomain(avvik: Avvik): AvvikResponseJson =
            AvvikResponseJson(
                avvikstype =
                    when (avvik.avvikstype) {
                        Avvikstype.TEKNISK_FEIL -> "tekniskfeil"
                        Avvikstype.PLANLAGT_NEDETID -> "planlagtNedetid"
                    },
                utdypendeAvviksbeskrivelse = avvik.utdypendeAvviksbeskrivelse,
            )
    }
}

@Serializable
data class KravgrunnlagResponseJson(
    val oppdragsgiversKravidentifikator: String?,
    val oppdragsgiversReferanse: String?,
) {
    companion object {
        fun fromDomain(kravgrunnlag: Kravgrunnlag): KravgrunnlagResponseJson =
            KravgrunnlagResponseJson(
                oppdragsgiversKravidentifikator = kravgrunnlag.oppdragsgiversKravidentifikator,
                oppdragsgiversReferanse = kravgrunnlag.oppdragsgiversReferanse,
            )
    }
}

@Serializable
data class KravlinjeResponseJson(
    val kravlinjetype: String,
    val opprinneligBeløp: Double,
    val gjenståendeBeløp: Double?,
    val kravlinjeBeskrivelse: Map<String, String> = emptyMap(),
) {
    companion object {
        fun fromDomain(kravlinje: Kravlinje): KravlinjeResponseJson =
            KravlinjeResponseJson(
                kravlinjetype = kravlinje.kravlinjetype,
                opprinneligBeløp = kravlinje.opprinneligBeløp,
                gjenståendeBeløp = kravlinje.gjenståendeBeløp,
                kravlinjeBeskrivelse =
                    kravlinje.kravlinjeBeskrivelse
                        .map { (locale, beskrivelse) -> locale.toLanguageTag() to beskrivelse.value }
                        .toMap(),
            )
    }
}

@Serializable
data class InnbetalingPlassertMotKravResponseJson(
    val innbetalingsIdentifikator: String,
    val innbetalingstype: String,
    val innbetalingsdato: LocalDate,
    val innbetaltBeløp: Double,
) {
    companion object {
        fun fromDomain(innbetaling: InnbetalingPlassertMotKrav): InnbetalingPlassertMotKravResponseJson =
            InnbetalingPlassertMotKravResponseJson(
                innbetalingsIdentifikator = innbetaling.innbetalingsIdentifikator,
                innbetalingstype = innbetaling.innbetalingstype,
                innbetalingsdato = innbetaling.innbetalingsdato,
                innbetaltBeløp = innbetaling.innbetaltBeløp,
            )
    }
}

@Serializable
data class TilleggsinformasjonResponseJson(
    val type: String,
    val periode: PeriodeMedTvangsmulktResponseJson? = null,
    val stoppdatoForLøpendeMulkt: LocalDate? = null,
    val ytelserForAvregning: YtelseForAvregningBeløpResponseJson? = null,
    val tilbakekrevingsperiode: TilbakekrevingsperiodeResponseJson? = null,
) {
    companion object {
        fun fromDomain(tilleggsinformasjon: Tilleggsinformasjon): TilleggsinformasjonResponseJson =
            when (tilleggsinformasjon) {
                is Tilleggsinformasjon.BrønnøysundRegistrene -> {
                    TilleggsinformasjonResponseJson(
                        type = "BrønnøysundRegistrene",
                        periode = PeriodeMedTvangsmulktResponseJson.fromDomain(tilleggsinformasjon.periode),
                        stoppdatoForLøpendeMulkt = tilleggsinformasjon.stoppdatoForLøpendeMulkt,
                    )
                }

                is Tilleggsinformasjon.Nav -> {
                    TilleggsinformasjonResponseJson(
                        type = "Nav",
                        ytelserForAvregning =
                            tilleggsinformasjon.ytelserForAvregning?.let {
                                YtelseForAvregningBeløpResponseJson.fromDomain(
                                    it,
                                )
                            },
                        tilbakekrevingsperiode =
                            TilbakekrevingsperiodeResponseJson.fromDomain(
                                tilleggsinformasjon.tilbakekrevingsperiode,
                            ),
                    )
                }
            }
    }
}

@Serializable
data class PeriodeMedTvangsmulktResponseJson(
    val fom: LocalDate,
    val tom: LocalDate,
) {
    companion object {
        fun fromDomain(periode: PeriodeMedTvangsmulkt): PeriodeMedTvangsmulktResponseJson =
            PeriodeMedTvangsmulktResponseJson(
                fom = periode.fom,
                tom = periode.tom,
            )
    }
}

@Serializable
data class YtelseForAvregningBeløpResponseJson(
    val valuta: String,
    val beløp: Long,
) {
    companion object {
        fun fromDomain(ytelse: YtelseForAvregningBeløp): YtelseForAvregningBeløpResponseJson =
            YtelseForAvregningBeløpResponseJson(
                valuta = ytelse.valuta,
                beløp = ytelse.beløp,
            )
    }
}

@Serializable
data class TilbakekrevingsperiodeResponseJson(
    val fom: LocalDate,
    val tom: LocalDate,
) {
    companion object {
        fun fromDomain(periode: Tilbakekrevingsperiode): TilbakekrevingsperiodeResponseJson =
            TilbakekrevingsperiodeResponseJson(
                fom = periode.fom,
                tom = periode.tom,
            )
    }
}
