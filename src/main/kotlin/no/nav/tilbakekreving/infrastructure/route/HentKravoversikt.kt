package no.nav.tilbakekreving.infrastructure.route

import arrow.core.getOrElse
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import no.nav.tilbakekreving.app.SøkEtterInnkrevingskrav
import no.nav.tilbakekreving.infrastructure.auth.abac.policy.KravAccessSubject
import no.nav.tilbakekreving.infrastructure.auth.abac.policy.LesKravAccessPolicy
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

        val filteredKrav = kravAccessPolicy.filter(KravAccessSubject(principal.groupIds), kravoversikt.krav)
        val filteredKravoversikt = kravoversikt.copy(krav = filteredKrav)

        call.respond(HttpStatusCode.OK, HentKravoversiktJsonResponse.fromDomain(filteredKravoversikt))
    }
}
