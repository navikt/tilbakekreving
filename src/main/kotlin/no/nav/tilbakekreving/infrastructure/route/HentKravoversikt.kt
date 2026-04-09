package no.nav.tilbakekreving.infrastructure.route

import arrow.core.getOrElse
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import no.nav.tilbakekreving.app.SøkEtterInnkrevingskrav
import no.nav.tilbakekreving.infrastructure.auth.abac.policy.LesKravAccessPolicy
import no.nav.tilbakekreving.infrastructure.auth.abac.policy.NavSaksbehandler
import no.nav.tilbakekreving.infrastructure.route.json.HentKravoversiktJsonRequest
import no.nav.tilbakekreving.infrastructure.route.json.HentKravoversiktJsonResponse
import no.nav.tilbakekreving.infrastructure.route.util.authenticatedPost

context(kravAccessPolicy: LesKravAccessPolicy)
fun Route.hentKravoversikt(søkEtterInnkrevingskrav: SøkEtterInnkrevingskrav) {
    authenticatedPost { principal ->
        val skyldnersøk = call.receive<HentKravoversiktJsonRequest>().toDomain()
        val kravoversikt =
            søkEtterInnkrevingskrav.søk(skyldnersøk).getOrElse {
                when (it) {
                    SøkEtterInnkrevingskrav.Feil.SøkEtterInnkrevingskravFeil -> {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            "Feil ved henting av kravoversikt",
                        )
                    }
                }
                return@authenticatedPost
            }

        val subject = NavSaksbehandler(principal.groupIds, principal.enheter)
        val filteredKrav = kravoversikt.krav.filter { kravAccessPolicy.isAllowed(subject, it.kravtype) }
        val filteredKravoversikt = kravoversikt.copy(krav = filteredKrav)

        call.respond(HttpStatusCode.OK, HentKravoversiktJsonResponse.fromDomain(filteredKravoversikt))
    }
}
