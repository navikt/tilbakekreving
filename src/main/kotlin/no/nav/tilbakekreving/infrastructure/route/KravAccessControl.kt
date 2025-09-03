package no.nav.tilbakekreving.infrastructure.route

import no.nav.tilbakekreving.domain.Krav
import no.nav.tilbakekreving.domain.Kravtype
import no.nav.tilbakekreving.infrastructure.auth.GroupId
import org.slf4j.LoggerFactory

context(kravAccessControl: KravAccessControl)
fun List<Krav>.filterByAccess(groupIds: Set<GroupId>): List<Krav> = filter(kravAccessControl.isKravAccessibleTo(groupIds))

class KravAccessControl(
    val enhetAccess: Map<Kravtype, Set<GroupId>>,
    val kravAccessGroup: GroupId,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    init {
        logger.info("KravAccessControl initialized with access: $enhetAccess")
    }

    fun isKravAccessibleTo(groupIds: Set<GroupId>): (Krav) -> Boolean =
        { krav ->
            groupIds.contains(kravAccessGroup) &&
                enhetAccess[krav.kravtype]?.any { groupId -> groupId in groupIds } ?: false
        }
}
