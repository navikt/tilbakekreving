package no.nav.tilbakekreving.infrastructure.route

import arrow.core.getOrElse
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import no.nav.tilbakekreving.app.HentKravoversikt
import no.nav.tilbakekreving.infrastructure.auth.UserGroupIdsPrincipal
import no.nav.tilbakekreving.infrastructure.route.json.HentKravoversiktJsonRequest
import no.nav.tilbakekreving.infrastructure.route.json.HentKravoversiktJsonResponse
import org.slf4j.LoggerFactory

fun Route.hentKravoversikt(hentKravoversikt: HentKravoversikt) {
    val logger = LoggerFactory.getLogger("HentKravoversiktRoute")
    post<HentKravoversiktJsonRequest> { hentKravoversiktJsonRequest ->
        val groupIds = call.principal<UserGroupIdsPrincipal>()
        logger.info("Henter kravoversikt for bruker med userGroups=${groupIds?.groupIds}")
        val skyldner = hentKravoversiktJsonRequest.toDomain()
        val kravoversikt =
            hentKravoversikt.hentKravoversikt(skyldner).getOrElse {
                when (it) {
                    HentKravoversikt.HentKravoversiktFeil.FeilVedHentingAvKrav ->
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            "Feil ved henting av kravoversikt",
                        )
                }
                return@post
            }

        call.respond(HttpStatusCode.OK, HentKravoversiktJsonResponse.fromDomain(kravoversikt))
    }
}
