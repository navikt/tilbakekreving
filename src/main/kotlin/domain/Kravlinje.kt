package no.nav.domain

data class Kravlinje(
    val kravlinjetype: String,
    val opprinneligBeloep: Double,
    val gjenstaaendeBeloep: Double?,
)