package no.nav.tilbakekreving.infrastructure.auth.abac.policy

import no.nav.tilbakekreving.app.FeatureToggle
import no.nav.tilbakekreving.app.Toggle
import no.nav.tilbakekreving.domain.Krav
import no.nav.tilbakekreving.domain.Kravtype
import no.nav.tilbakekreving.infrastructure.auth.abac.AccessPolicy
import no.nav.tilbakekreving.infrastructure.auth.abac.accessPolicy
import no.nav.tilbakekreving.infrastructure.auth.model.Enhetsnummer
import no.nav.tilbakekreving.infrastructure.auth.model.GroupId
import org.slf4j.Logger

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
context(featureToggle: FeatureToggle, logger: Logger?)
fun lesKravAccessPolicy(
    lesKravAccessGroup: GroupId,
    enhetAccess: Map<Enhetsnummer, Set<Kravtype>>,
): LesKravAccessPolicy {
    logger?.info("KravAccessPolicy initialized with enhetAccess: $enhetAccess")

    return accessPolicy {
        require { lesKravAccessGroup in subject.groupIds }

        require {
            if (featureToggle.isEnabled(Toggle.KRAVTYPE_ENHET_TILGANGSKONTROLL)) {
                subject.enheter.any { enhetAccess[it]?.contains(resource.kravtype) == true }
            } else {
                true
            }
        }
    }
}

typealias LesKravAccessPolicy = AccessPolicy<NavSaksbehandler, Krav>

data class NavSaksbehandler(
    val groupIds: Set<GroupId>,
    val enheter: Set<Enhetsnummer>,
)
