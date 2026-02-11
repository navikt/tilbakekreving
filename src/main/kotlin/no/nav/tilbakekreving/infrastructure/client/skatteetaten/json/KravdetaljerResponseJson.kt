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
data class SkeHentKravdetaljerResponsJson(
    val oppdragsgiver: SkeOppdragsgiverResponseJson,
    val skyldner: SkeSkyldnerResponseJson,
    val krav: SkeKravResponseJson,
    val avvik: SkeAvvikResponseJson?,
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
data class SkeKravResponseJson(
    val forfallsdato: LocalDate?,
    val foreldelsesdato: LocalDate?,
    val fastsettelsesdato: LocalDate?,
    val kravtype: String,
    @SerialName("opprinneligBeloep") val `opprinneligBeløp`: Double,
    @SerialName("gjenstaaendeBeloep") val `gjenståendeBeløp`: Double,
    val skatteetatensKravidentifikator: String?,
    val kravlinjer: List<SkeKravlinjeResponseJson> = emptyList(),
    val kravgrunnlag: SkeKravgrunnlagResponseJson,
    val innbetalingerPlassertMotKrav: List<SkeInnbetalingPlassertMotKravResponseJson> = emptyList(),
    val tilleggsinformasjon: SkeTilleggsinformasjonResponseJson?,
) {
    fun toDomain(): KravDetalj =
        KravDetalj(
            forfallsdato = forfallsdato,
            foreldelsesdato = foreldelsesdato,
            fastsettelsesdato = fastsettelsesdato,
            kravtype = kravtype,
            `opprinneligBeløp` = opprinneligBeløp,
            `gjenståendeBeløp` = gjenståendeBeløp,
            skatteetatensKravidentifikator = skatteetatensKravidentifikator,
            kravlinjer = kravlinjer.map(SkeKravlinjeResponseJson::toDomain),
            kravgrunnlag = kravgrunnlag.toDomain(),
            innbetalingerPlassertMotKrav = innbetalingerPlassertMotKrav.map { it.toDomain() },
            tilleggsinformasjon = tilleggsinformasjon?.toDomain(),
        )
}

@Serializable
data class SkeOppdragsgiverResponseJson(
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
data class SkeSkyldnerResponseJson(
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
data class SkeAvvikResponseJson(
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
data class SkeInnbetalingPlassertMotKravResponseJson(
    val innbetalingsIdentifikator: String,
    val innbetalingstype: String,
    val innbetalingsdato: LocalDate,
    @SerialName("innbetaltBeloep") val innbetaltBeløp: Double,
) {
    fun toDomain(): InnbetalingPlassertMotKrav =
        InnbetalingPlassertMotKrav(
            innbetalingsIdentifikator = innbetalingsIdentifikator,
            innbetalingstype = innbetalingstype,
            innbetalingsdato = innbetalingsdato,
            innbetaltBeløp = innbetaltBeløp,
        )
}

@Serializable
data class SkeTilleggsinformasjonResponseJson(
    @SerialName("tilleggsinformasjonBroennoeysundRegistrene") val tilleggsinformasjonBrønnøysundRegistrene:
        SkeTilleggsinformasjonBrønnøysundRegistreneResponseJson? = null,
    val tilleggsinformasjonNav: SkeTilleggsinformasjonNavResponseJson? = null,
) {
    fun toDomain(): Tilleggsinformasjon? =
        when {
            tilleggsinformasjonBrønnøysundRegistrene != null -> tilleggsinformasjonBrønnøysundRegistrene.toDomain()
            tilleggsinformasjonNav != null -> tilleggsinformasjonNav.toDomain()
            else -> null
        }
}

@Serializable
data class SkeTilleggsinformasjonBrønnøysundRegistreneResponseJson(
    val periode: SkePeriodeMedTvangsmulktResponseJson,
    @SerialName("stoppdatoForLoependeMulkt") val stoppdatoForLøpendeMulkt: LocalDate? = null,
) {
    fun toDomain(): Tilleggsinformasjon.BrønnøysundRegistrene =
        Tilleggsinformasjon.BrønnøysundRegistrene(
            periode = periode.toDomain(),
            stoppdatoForLøpendeMulkt = stoppdatoForLøpendeMulkt,
        )
}

@Serializable
data class SkeTilleggsinformasjonNavResponseJson(
    val ytelserForAvregning: SkeYtelseForAvregningBeløpResponseJson? = null,
    val tilbakekrevingsperiode: SkeTilbakekrevingsperiodeResponseJson,
) {
    fun toDomain(): Tilleggsinformasjon.Nav =
        Tilleggsinformasjon.Nav(
            ytelserForAvregning = ytelserForAvregning?.toDomain(),
            tilbakekrevingsperiode = tilbakekrevingsperiode.toDomain(),
        )
}

@Serializable
data class SkePeriodeMedTvangsmulktResponseJson(
    val fom: LocalDate,
    val tom: LocalDate,
) {
    fun toDomain(): PeriodeMedTvangsmulkt =
        PeriodeMedTvangsmulkt(
            fom = fom,
            tom = tom,
        )
}

@Serializable
data class SkeYtelseForAvregningBeløpResponseJson(
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
data class SkeTilbakekrevingsperiodeResponseJson(
    val fom: LocalDate,
    val tom: LocalDate,
) {
    fun toDomain(): Tilbakekrevingsperiode =
        Tilbakekrevingsperiode(
            fom = fom,
            tom = tom,
        )
}
