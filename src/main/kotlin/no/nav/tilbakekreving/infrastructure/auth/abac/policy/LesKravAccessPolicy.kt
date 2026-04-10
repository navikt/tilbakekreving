package no.nav.tilbakekreving.infrastructure.auth.abac.policy

import arrow.core.Either
import no.nav.tilbakekreving.app.FeatureToggle
import no.nav.tilbakekreving.app.Toggle
import no.nav.tilbakekreving.domain.Kravtype
import no.nav.tilbakekreving.domain.UkjentKravtype
import no.nav.tilbakekreving.infrastructure.auth.abac.AccessPolicy
import no.nav.tilbakekreving.infrastructure.auth.abac.accessPolicy
import no.nav.tilbakekreving.infrastructure.auth.model.Enhetsnummer
import no.nav.tilbakekreving.infrastructure.auth.model.GroupId
import org.slf4j.Logger

/**
 * Tilgangskontroll for å se krav.
 *
 * Brukeren må være i gruppen 0000-CA-Tilbakekreving_Les_krav for å se krav. IDen til denne gruppen sendes inn
 * med [lesKravAccessGroup].
 *
 * Når feature toggle [Toggle.KRAVTYPE_ENHET_TILGANGSKONTROLL] er aktivert, filtreres kravene
 * basert på brukerens enhetsgrupper. Brukeren ser da kun krav der kravtypen
 * er knyttet til en enhet brukeren har tilgang til. Mappingen over hvilke enheter som har tilgang til hvilke kravtyper
 * finnes i [no.nav.tilbakekreving.infrastructure.auth.model.enhetKravtypeMapping] og sendes inn med [enhetAccess].
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
                resource.fold(
                    ifLeft = {
                        // Ukjent kravtype kan kun sees av saksbehandlere med rett til å se ALLE krav
                        subject.enheter.any { Kravtype.ALLE in enhetAccess[it].orEmpty() }
                    },
                    ifRight = { kravtype ->
                        subject.enheter.any {
                            val kravtyper = enhetAccess[it].orEmpty()
                            Kravtype.ALLE in kravtyper || kravtype in kravtyper
                        }
                    },
                )
            } else {
                true
            }
        }
    }
}

typealias LesKravAccessPolicy = AccessPolicy<NavSaksbehandler, Either<UkjentKravtype, Kravtype>>

data class NavSaksbehandler(
    val groupIds: Set<GroupId>,
    val enheter: Set<Enhetsnummer>,
)
