package no.nav.tilbakekreving.infrastructure.auth

data class NavUserPrincipal(
    val navIdent: String,
    val groupIds: List<GroupId>,
)

@JvmInline
value class GroupId(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "GroupId cannot be blank" }
    }
}
