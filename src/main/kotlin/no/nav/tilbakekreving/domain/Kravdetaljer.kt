package no.nav.tilbakekreving.domain

data class Kravdetaljer(
    val kravgrunnlag: Kravgrunnlag,
    val kravlinjer: List<Kravlinje>,
)
