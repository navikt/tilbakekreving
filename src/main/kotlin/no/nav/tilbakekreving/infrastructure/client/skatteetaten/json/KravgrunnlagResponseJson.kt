package no.nav.tilbakekreving.infrastructure.client.skatteetaten.json

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import no.nav.tilbakekreving.domain.Kravgrunnlag

@Serializable
data class KravgrunnlagResponseJson(
    val datoNaarKravVarBesluttetHosOppdragsgiver: String,
) {
    fun toDomain(): Kravgrunnlag =
        Kravgrunnlag(
            datoNaarKravVarBesluttetHosOppdragsgiver = LocalDate.parse(datoNaarKravVarBesluttetHosOppdragsgiver),
        )
}
