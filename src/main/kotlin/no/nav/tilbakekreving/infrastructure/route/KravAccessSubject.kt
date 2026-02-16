package no.nav.tilbakekreving.infrastructure.route

import no.nav.tilbakekreving.domain.Krav
import no.nav.tilbakekreving.domain.Kravtype
import no.nav.tilbakekreving.infrastructure.auth.GroupId
import no.nav.tilbakekreving.infrastructure.auth.abac.AccessPolicy
import no.nav.tilbakekreving.infrastructure.auth.abac.accessPolicy
import org.slf4j.LoggerFactory

data class KravAccessSubject(
    val groupIds: Set<GroupId>,
)

fun kravAccessPolicy(
    kravAccessGroup: GroupId,
    enhetAccess: Map<Kravtype, Set<GroupId>> = emptyMap(),
): AccessPolicy<KravAccessSubject, Krav> {
    val logger = LoggerFactory.getLogger("KravAccessPolicy")
    logger.info("KravAccessPolicy initialized with enhetAccess: $enhetAccess")

    return accessPolicy {
        require { kravAccessGroup in subject.groupIds }

        // TODO: Skru på tilgangskontroll når mapping fra kravtype til enhet er på plass
        // require { enhetAccess[resource.kravtype]?.any { it in subject.groupIds } ?: false }
    }
}
