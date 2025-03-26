package no.nav.domain

data class Kravdetaljer(
    val kravgrunnlag: Kravgrunnlag,
    val kravlinjer: List<Kravlinje>,
)