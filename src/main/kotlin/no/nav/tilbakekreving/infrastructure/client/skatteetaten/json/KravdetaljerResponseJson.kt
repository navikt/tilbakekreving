package no.nav.tilbakekreving.infrastructure.client.skatteetaten.json

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nav.tilbakekreving.domain.Avvik
import no.nav.tilbakekreving.domain.Avvikstype
import no.nav.tilbakekreving.domain.InnbetalingPlassertMotKrav
import no.nav.tilbakekreving.domain.KravDetalj
import no.nav.tilbakekreving.domain.Kravdetaljer
import no.nav.tilbakekreving.domain.KravdetaljerSkyldner
import no.nav.tilbakekreving.domain.Oppdragsgiver
import no.nav.tilbakekreving.domain.PeriodeMedTvangsmulkt
import no.nav.tilbakekreving.domain.Tilbakekrevingsperiode
import no.nav.tilbakekreving.domain.Tilleggsinformasjon
import no.nav.tilbakekreving.domain.YtelseForAvregningBeløp

@Serializable
data class HentKravdetaljerResponsJson(
    val krav: KravResponseJson,
    val oppdragsgiver: OppdragsgiverResponseJson,
    val skyldner: SkyldnerResponseJson,
    val avvik: AvvikResponseJson?,
) {
    fun toDomain(): Kravdetaljer =
        Kravdetaljer(
            krav = krav.toDomain(),
            oppdragsgiver = oppdragsgiver.toDomain(),
            skyldner = skyldner.toDomain(),
            avvik = avvik?.toDomain(),
        )
}

@Serializable
data class KravResponseJson(
    val forfallsdato: String?,
    val foreldelsesdato: String?,
    val fastsettelsesdato: String?,
    val kravtype: String,
    @SerialName("opprinneligBeloep") val opprinneligBeløp: Double,
    @SerialName("gjenstaaendeBeloep") val gjenståendeBeløp: Double,
    val skatteetatensKravidentifikator: String,
    val kravlinjer: List<KravlinjeResponseJson> = emptyList(),
    val kravgrunnlag: KravgrunnlagResponseJson,
    val innbetalingerPlassertMotKrav: List<InnbetalingPlassertMotKravResponseJson> = emptyList(),
    val tilleggsinformasjon: TilleggsinformasjonResponseJson?,
) {
    fun toDomain(): KravDetalj =
        KravDetalj(
            forfallsdato = forfallsdato?.let { LocalDate.parse(it) },
            foreldelsesdato = foreldelsesdato?.let { LocalDate.parse(it) },
            fastsettelsesdato = fastsettelsesdato?.let { LocalDate.parse(it) },
            kravtype = kravtype,
            opprinneligBeløp = opprinneligBeløp,
            gjenståendeBeløp = gjenståendeBeløp,
            skatteetatensKravidentifikator = skatteetatensKravidentifikator,
            kravlinjer = kravlinjer.map(KravlinjeResponseJson::toDomain),
            kravgrunnlag = kravgrunnlag.toDomain(),
            innbetalingerPlassertMotKrav = innbetalingerPlassertMotKrav.map { it.toDomain() },
            tilleggsinformasjon = tilleggsinformasjon?.toDomain(),
        )
}

@Serializable
data class OppdragsgiverResponseJson(
    val organisasjonsnummer: String,
    val organisasjonsnavn: String?,
) {
    fun toDomain(): Oppdragsgiver =
        Oppdragsgiver(
            organisasjonsnummer = organisasjonsnummer,
            organisasjonsnavn = organisasjonsnavn,
        )
}

@Serializable
data class SkyldnerResponseJson(
    val identifikator: String,
    val skyldnersNavn: String?,
) {
    fun toDomain(): KravdetaljerSkyldner =
        KravdetaljerSkyldner(
            identifikator = identifikator,
            skyldnersNavn = skyldnersNavn,
        )
}

@Serializable
data class AvvikResponseJson(
    val avvikstype: String,
    val utdypendeAvviksbeskrivelse: String,
) {
    fun toDomain(): Avvik =
        Avvik(
            avvikstype =
                when (avvikstype) {
                    "tekniskfeil" -> Avvikstype.TEKNISK_FEIL
                    "planlagtNedetid" -> Avvikstype.PLANLAGT_NEDETID
                    else -> throw IllegalArgumentException("Unknown avvikstype: $avvikstype")
                },
            utdypendeAvviksbeskrivelse = utdypendeAvviksbeskrivelse,
        )
}

@Serializable
data class InnbetalingPlassertMotKravResponseJson(
    val innbetalingsIdentifikator: String,
    val innbetalingstype: String,
    val innbetalingsdato: String,
    @SerialName("innbetaltBeloep") val innbetaltBeløp: Double,
) {
    fun toDomain(): InnbetalingPlassertMotKrav =
        InnbetalingPlassertMotKrav(
            innbetalingsIdentifikator = innbetalingsIdentifikator,
            innbetalingstype = innbetalingstype,
            innbetalingsdato = LocalDate.parse(innbetalingsdato),
            innbetaltBeløp = innbetaltBeløp,
        )
}

@Serializable
data class TilleggsinformasjonResponseJson(
    @SerialName("tilleggsinformasjonBroennoeysundRegistrene") val tilleggsinformasjonBrønnøysundRegistrene:
        TilleggsinformasjonBrønnøysundRegistreneResponseJson? = null,
    val tilleggsinformasjonNav: TilleggsinformasjonNavResponseJson? = null,
) {
    fun toDomain(): Tilleggsinformasjon? =
        when {
            tilleggsinformasjonBrønnøysundRegistrene != null -> tilleggsinformasjonBrønnøysundRegistrene.toDomain()
            tilleggsinformasjonNav != null -> tilleggsinformasjonNav.toDomain()
            else -> null
        }
}

@Serializable
data class TilleggsinformasjonBrønnøysundRegistreneResponseJson(
    val periode: PeriodeMedTvangsmulktResponseJson,
    @SerialName("stoppdatoForLoependeMulkt") val stoppdatoForLøpendeMulkt: String? = null,
) {
    fun toDomain(): Tilleggsinformasjon.BrønnøysundRegistrene =
        Tilleggsinformasjon.BrønnøysundRegistrene(
            periode = periode.toDomain(),
            stoppdatoForLøpendeMulkt = stoppdatoForLøpendeMulkt?.let { LocalDate.parse(it) },
        )
}

@Serializable
data class TilleggsinformasjonNavResponseJson(
    val ytelserForAvregning: YtelseForAvregningBeløpResponseJson? = null,
    val tilbakekrevingsperiode: TilbakekrevingsperiodeResponseJson,
) {
    fun toDomain(): Tilleggsinformasjon.Nav =
        Tilleggsinformasjon.Nav(
            ytelserForAvregning = ytelserForAvregning?.toDomain(),
            tilbakekrevingsperiode = tilbakekrevingsperiode.toDomain(),
        )
}

@Serializable
data class PeriodeMedTvangsmulktResponseJson(
    val fom: String,
    val tom: String,
) {
    fun toDomain(): PeriodeMedTvangsmulkt =
        PeriodeMedTvangsmulkt(
            fom = LocalDate.parse(fom),
            tom = LocalDate.parse(tom),
        )
}

@Serializable
data class YtelseForAvregningBeløpResponseJson(
    val valuta: String,
    @SerialName("beloep") val beløp: Long,
) {
    fun toDomain(): YtelseForAvregningBeløp =
        YtelseForAvregningBeløp(
            valuta = valuta,
            beløp = beløp,
        )
}

@Serializable
data class TilbakekrevingsperiodeResponseJson(
    val fom: String,
    val tom: String,
) {
    fun toDomain(): Tilbakekrevingsperiode =
        Tilbakekrevingsperiode(
            fom = LocalDate.parse(fom),
            tom = LocalDate.parse(tom),
        )
}
