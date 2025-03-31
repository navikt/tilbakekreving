package no.nav.route

import arrow.core.getOrElse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import no.nav.app.HentKravdetaljer
import no.nav.route.json.HentKravdetaljerJsonRequest
import no.nav.route.json.HentKravdetaljerJsonResponse

fun Application.configureRouting(hentKravdetaljer: HentKravdetaljer) {
    routing {
        route("/internal") {
            get("/isAlive") {
                call.respond(HttpStatusCode.OK)
            }
            get("/isReady") {
                call.respond(HttpStatusCode.OK)
            }
            get("/maskinporten/token") {
                call.respondText("Maskinporten token")
            }
            post<HentKravdetaljerJsonRequest>("/kravdetaljer") { jsonRequest ->
                val kravdetaljer =
                    hentKravdetaljer.hentKravdetaljer(jsonRequest.toDomain()).getOrElse {
                        call.respond(HttpStatusCode.InternalServerError, "Feil ved henting av kravdetaljer")
                        return@post
                    }

                call.respond(HttpStatusCode.OK, HentKravdetaljerJsonResponse.fromDomain(kravdetaljer))
            }
        }
        get("/") {
            call.respondText("Hello World!")
        }
    }
}

