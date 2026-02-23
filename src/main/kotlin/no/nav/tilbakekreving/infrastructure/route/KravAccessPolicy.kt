package no.nav.tilbakekreving.infrastructure.route

import no.nav.tilbakekreving.app.FeatureToggles
import no.nav.tilbakekreving.app.Toggle
import no.nav.tilbakekreving.domain.Krav
import no.nav.tilbakekreving.domain.Kravtype
import no.nav.tilbakekreving.infrastructure.auth.GroupId
import no.nav.tilbakekreving.infrastructure.auth.abac.AccessPolicy
import no.nav.tilbakekreving.infrastructure.auth.abac.accessPolicy
import org.slf4j.LoggerFactory

/**
 * Tilgangskontroll for krav.
 *
 * Brukeren må alltid ha [lesKravAccessGroup] for å se krav.
 *
 * Når feature toggle [Toggle.KRAVTYPE_ENHET_TILGANGSKONTROLL] er aktivert, filtreres kravene
 * i tillegg basert på brukerens enhetsgrupper. Brukeren ser da kun krav der kravtypen
 * er knyttet til en enhet brukeren har tilgang til via [enhetAccess].
 *
 * Når toggle er deaktivert, ser brukeren alle krav så lenge de har [lesKravAccessGroup].
 */
context(featureToggles: FeatureToggles)
fun kravAccessPolicy(
    lesKravAccessGroup: GroupId,
    enhetAccess: Map<Kravtype, Set<GroupId>> = emptyMap(),
): AccessPolicy<KravAccessSubject, Krav> {
    val logger = LoggerFactory.getLogger("KravAccessPolicy")
    logger.info("KravAccessPolicy initialized with enhetAccess: $enhetAccess")

    return accessPolicy {
        require { lesKravAccessGroup in subject.groupIds }

        require {
            if (featureToggles.isEnabled(Toggle.KRAVTYPE_ENHET_TILGANGSKONTROLL)) {
                enhetAccess[resource.kravtype]?.any { it in subject.groupIds } ?: false
            } else {
                true
            }
        }
    }
}
