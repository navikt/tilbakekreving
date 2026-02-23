package no.nav.tilbakekreving.infrastructure.route

import no.nav.tilbakekreving.infrastructure.auth.GroupId

data class KravAccessSubject(
    val groupIds: Set<GroupId>,
)
