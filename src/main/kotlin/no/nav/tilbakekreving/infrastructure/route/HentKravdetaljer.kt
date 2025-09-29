package no.nav.tilbakekreving.infrastructure.route

import arrow.core.getOrElse
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import no.nav.tilbakekreving.app.HentKravdetaljer
import no.nav.tilbakekreving.infrastructure.route.json.HentKravdetaljerJsonRequest
import no.nav.tilbakekreving.infrastructure.route.json.HentKravdetaljerJsonResponse
import no.nav.tilbakekreving.infrastructure.route.util.groupIdsFromPrincipal
import org.slf4j.LoggerFactory

fun Route.hentKravdetaljerRoute(hentKravdetaljer: HentKravdetaljer) {
    val logger = LoggerFactory.getLogger("HentKravdetaljerRoute")

    post<HentKravdetaljerJsonRequest> { hentKravdetaljerJson ->
        val groupIds = groupIdsFromPrincipal()
        logger.info("Henter kravoversikt for bruker med userGroups=$groupIds")
        val kravidentifikator = hentKravdetaljerJson.toDomain()
        val kravdetaljer =
            hentKravdetaljer.hentKravdetaljer(kravidentifikator).getOrElse {
                when (it) {
                    HentKravdetaljer.HentKravdetaljerFeil.FantIkkeKravdetaljer ->
                        call.respond(
                            HttpStatusCode.NoContent,
                        )

                    HentKravdetaljer.HentKravdetaljerFeil.FeilVedHentingAvKravdetaljer ->
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            "Feil ved henting av kravdetaljer",
                        )
                }
                return@post
            }

        call.respond(HttpStatusCode.OK, HentKravdetaljerJsonResponse.fromDomain(kravdetaljer))
    }
}
