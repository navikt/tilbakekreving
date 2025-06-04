package no.nav.tilbakekreving.infrastructure.auth

data class UserGroupIdsPrincipal(
    val groupIds: List<GroupId>,
)

@JvmInline
value class GroupId(
    val value: String,
)
