package no.nav.tilbakekreving.infrastructure.route

import arrow.core.getOrElse
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import no.nav.tilbakekreving.app.SøkEtterInnkrevingskrav
import no.nav.tilbakekreving.infrastructure.route.json.HentKravoversiktJsonRequest
import no.nav.tilbakekreving.infrastructure.route.json.HentKravoversiktJsonResponse
import no.nav.tilbakekreving.infrastructure.route.util.groupIdsFromPrincipal
import org.slf4j.LoggerFactory

context(kravAccessControl: KravAccessControl)
fun Route.hentKravoversikt(søkEtterInnkrevingskrav: SøkEtterInnkrevingskrav) {
    val logger = LoggerFactory.getLogger("HentKravoversiktRoute")
    post<HentKravoversiktJsonRequest> { hentKravoversiktJsonRequest ->
        val groupIds = groupIdsFromPrincipal()
        logger.info("Henter kravoversikt for bruker med userGroups=$groupIds")

        val skyldnersøk = hentKravoversiktJsonRequest.toDomain()
        val kravoversikt =
            søkEtterInnkrevingskrav
                .søk(skyldnersøk)
                .getOrElse {
                    when (it) {
                        SøkEtterInnkrevingskrav.Feil.SøkEtterInnkrevingskravFeil ->
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                "Feil ved henting av kravoversikt",
                            )
                    }
                    return@post
                }

        val filteredKrav = kravoversikt.krav.filterByAccess(groupIds)
        val filteredKravoversikt = kravoversikt.copy(krav = filteredKrav)

        call.respond(HttpStatusCode.OK, HentKravoversiktJsonResponse.fromDomain(filteredKravoversikt))
    }
}
