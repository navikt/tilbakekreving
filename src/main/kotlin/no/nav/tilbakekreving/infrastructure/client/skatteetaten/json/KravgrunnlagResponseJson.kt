package no.nav.tilbakekreving.infrastructure.client.skatteetaten.json

import kotlinx.serialization.Serializable
import no.nav.tilbakekreving.domain.Kravgrunnlag

@Serializable
data class KravgrunnlagResponseJson(
    val oppdragsgiversKravidentifikator: String,
    val oppdragsgiversReferanse: String?,
) {
    fun toDomain(): Kravgrunnlag =
        Kravgrunnlag(
            oppdragsgiversKravidentifikator = oppdragsgiversKravidentifikator,
            oppdragsgiversReferanse = oppdragsgiversReferanse,
        )
}
