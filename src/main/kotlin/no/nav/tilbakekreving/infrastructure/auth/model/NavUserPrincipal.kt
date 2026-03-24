package no.nav.tilbakekreving.infrastructure.auth.model

data class NavUserPrincipal(
    val navIdent: String,
    val groupIds: Set<GroupId>,
    val enheter: Set<Enhetsnummer>,
)
