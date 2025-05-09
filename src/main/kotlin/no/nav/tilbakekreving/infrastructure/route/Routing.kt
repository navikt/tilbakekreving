package no.nav.tilbakekreving.infrastructure.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureRouting(routing: Routing.() -> Unit) {
    routing {
        route("/internal") {
            get("/isAlive") {
                call.respond(HttpStatusCode.OK)
            }
            get("/isReady") {
                call.respond(HttpStatusCode.OK)
            }
        }
        routing()
    }
}
