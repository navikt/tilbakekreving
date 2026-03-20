package no.nav.tilbakekreving.infrastructure.auth

data class NavUserPrincipal(
    val navIdent: String,
    val groupIds: Set<GroupId>,
    val enheter: Set<Enhetsnummer>,
)

@JvmInline
value class GroupId(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "GroupId kan ikke være tom" }
    }
}

@JvmInline
value class Enhetsnummer(
    val value: String,
) {
    init {
        require(value.matches(Regex("[0-9]{4}"))) { "Enhetsnummer må være på 4 siffer" }
    }
}
