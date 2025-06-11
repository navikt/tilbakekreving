package no.nav.tilbakekreving.domain

import kotlinx.datetime.LocalDate

data class Kravdetaljer(
    val kravgrunnlag: Kravgrunnlag,
    val kravlinjer: List<Kravlinje>,
    val tilleggsfrist: LocalDate? = null,
)
