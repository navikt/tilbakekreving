package no.nav

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        route("/internal") {
            get("/isAlive") {
                call.respond(HttpStatusCode.OK)
            }
            get("/isReady") {
                call.respond(HttpStatusCode.OK)
            }
        }
        get("/") {
            call.respondText("Hello World!")
        }
    }
}
