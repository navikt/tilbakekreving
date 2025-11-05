package no.nav.tilbakekreving.infrastructure.route

import arrow.core.getOrElse
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import no.nav.tilbakekreving.app.SøkEtterInnkrevingskrav
import no.nav.tilbakekreving.infrastructure.audit.AuditLog
import no.nav.tilbakekreving.infrastructure.route.json.HentKravoversiktJsonRequest
import no.nav.tilbakekreving.infrastructure.route.json.HentKravoversiktJsonResponse
import no.nav.tilbakekreving.infrastructure.route.util.navUserPrincipal
import org.slf4j.LoggerFactory

context(kravAccessControl: KravAccessControl, auditLog: AuditLog)
fun Route.hentKravoversikt(søkEtterInnkrevingskrav: SøkEtterInnkrevingskrav) {
    val logger = LoggerFactory.getLogger("HentKravoversiktRoute")
    post<HentKravoversiktJsonRequest> { hentKravoversiktJsonRequest ->
        val principal =
            navUserPrincipal() ?: run {
                logger.warn("Fant ikke navUserPrincipal ved henting av kravoversikt")
                call.respond(HttpStatusCode.Unauthorized, "Ugyldig bruker")
                return@post
            }

        val groupIds = principal.groupIds.toSet()
        logger.info("Henter kravoversikt for ${principal.navIdent} med userGroups=$groupIds")

        val skyldnersøk = hentKravoversiktJsonRequest.toDomain()
        val kravoversikt =
            søkEtterInnkrevingskrav.søk(skyldnersøk).getOrElse {
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

        auditLog.info(
            AuditLog.Message(
                sourceUserId = principal.navIdent,
                destinationUserId = filteredKravoversikt.skyldner.identifikator,
                event = AuditLog.EventType.ACCESS,
                message = "Hentet kravoversikt for skyldner",
            ),
        )

        call.respond(HttpStatusCode.OK, HentKravoversiktJsonResponse.fromDomain(filteredKravoversikt))
    }
}
