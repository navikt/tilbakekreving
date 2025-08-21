package no.nav.tilbakekreving.infrastructure.route

import no.nav.tilbakekreving.domain.Krav
import no.nav.tilbakekreving.domain.Kravtype
import no.nav.tilbakekreving.infrastructure.auth.GroupId

context(kravAccessControl: KravAccessControl)
fun List<Krav>.filterByAccess(groupIds: Set<GroupId>): List<Krav> = filter(kravAccessControl.isKravAccessibleTo(groupIds))

class KravAccessControl(
    val access: Map<Kravtype, Set<GroupId>> = defaultAccess,
) {
    companion object {
        // TODO: Flytt til application.conf
        val defaultAccess: Map<Kravtype, Set<GroupId>> = emptyMap()
    }

    fun isKravAccessibleTo(groupIds: Set<GroupId>): (Krav) -> Boolean =
        { krav -> access[krav.kravtype]?.any { groupId -> groupId in groupIds } ?: false }
}
