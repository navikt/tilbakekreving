package no.nav.tilbakekreving.infrastructure.route

import arrow.core.getOrElse
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import no.nav.tilbakekreving.app.HentKravoversikt
import no.nav.tilbakekreving.infrastructure.route.json.HentKravoversiktJsonRequest
import no.nav.tilbakekreving.infrastructure.route.json.HentKravoversiktJsonResponse

fun Route.hentKravoversikt(hentKravoversikt: HentKravoversikt) {
    post<HentKravoversiktJsonRequest> { jsonRequest ->
        val kravoversikt =
            hentKravoversikt.hentKravoversikt(jsonRequest.toDomain()).getOrElse {
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
