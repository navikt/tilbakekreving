package no.nav.tilbakekreving.infrastructure.route

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.openapi.OpenApiInfo
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.di.DependencyRegistry
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.openapi.OpenApiDocSource
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.routing.routingRoot
import no.nav.tilbakekreving.config.AuthenticationConfigName
import no.nav.tilbakekreving.infrastructure.audit.AuditLog
import no.nav.tilbakekreving.infrastructure.auth.abac.policy.LesKravAccessPolicy
import no.nav.tilbakekreving.infrastructure.client.skatteetaten.SkatteetatenInnkrevingsoppdragHttpClient

context(dependencies: DependencyRegistry)
suspend fun Application.configureRouting() {
    val authConfigName = dependencies.resolve<AuthenticationConfigName>()
    val innkrevingsoppdragHttpClient = dependencies.resolve<SkatteetatenInnkrevingsoppdragHttpClient>()
    val kravAccessPolicy: LesKravAccessPolicy = dependencies.resolve()
    val auditLog: AuditLog = dependencies.resolve()

    this.routing {
        route("/internal") {
            authenticate(authConfigName.name) {
                context(kravAccessPolicy, auditLog) {
                    route("/kravdetaljer") {
                        hentKravdetaljerRoute(innkrevingsoppdragHttpClient)
                    }
                    route("/kravoversikt") {
                        hentKravoversikt(innkrevingsoppdragHttpClient)
                    }
                }
            }
            get("/isAlive") {
                call.respond<HttpStatusCode>(HttpStatusCode.OK)
            }
            get("/isReady") {
                call.respond<HttpStatusCode>(HttpStatusCode.OK)
            }
        }
        swaggerUI("/swagger") {
            info = OpenApiInfo("Tilbakekreving API", "1.0")
            source =
                OpenApiDocSource.Routing(ContentType.Application.Json) {
                    routingRoot.descendants()
                }
        }
    }
}
