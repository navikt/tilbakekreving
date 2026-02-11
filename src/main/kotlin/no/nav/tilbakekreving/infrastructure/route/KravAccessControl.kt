package no.nav.tilbakekreving.infrastructure.route

import no.nav.tilbakekreving.domain.Krav
import no.nav.tilbakekreving.domain.Kravtype
import no.nav.tilbakekreving.infrastructure.auth.GroupId
import org.slf4j.LoggerFactory

class KravAccessControl(
    private val enhetAccess: Map<Kravtype, Set<GroupId>>,
    private val kravAccessGroup: GroupId,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    init {
        logger.info("KravAccessControl initialized with access: $enhetAccess")
    }

    fun filterByAccess(
        krav: List<Krav>,
        groupIds: Set<GroupId>,
    ): List<Krav> = krav.filter(isKravAccessibleTo(groupIds))

    private fun isKravAccessibleTo(groupIds: Set<GroupId>): (Krav) -> Boolean =
        { krav ->
            groupIds.contains(kravAccessGroup)
            // TODO: Skru på tilgangskontroll når mapping fra kravtype til enhet er på plass
            // && enhetAccess[krav.kravtype]?.any { groupId -> groupId in groupIds } ?: false
        }
}
