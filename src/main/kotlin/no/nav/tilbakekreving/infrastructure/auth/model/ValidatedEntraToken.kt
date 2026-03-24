package no.nav.tilbakekreving.infrastructure.auth.model

data class ValidatedEntraToken(
    val navIdent: String,
    val groupIds: Set<GroupId>,
)
