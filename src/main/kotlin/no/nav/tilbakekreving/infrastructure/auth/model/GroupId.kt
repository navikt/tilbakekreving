package no.nav.tilbakekreving.infrastructure.auth.model

@JvmInline
value class GroupId(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "GroupId kan ikke være tom" }
    }
}
