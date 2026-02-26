package no.nav.tilbakekreving.setup

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.openapi.OpenApiInfo
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.openapi.OpenApiDocSource
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.routing.routingRoot
import no.nav.tilbakekreving.app.HentKravdetaljer
import no.nav.tilbakekreving.app.SøkEtterInnkrevingskrav
import no.nav.tilbakekreving.config.AuthenticationConfigName
import no.nav.tilbakekreving.infrastructure.audit.AuditLog
import no.nav.tilbakekreving.infrastructure.auth.abac.policy.LesKravAccessPolicy
import no.nav.tilbakekreving.infrastructure.route.hentKravdetaljerRoute
import no.nav.tilbakekreving.infrastructure.route.hentKravoversikt

fun Application.configureRouting() {
    val hentKravdetaljer: HentKravdetaljer by dependencies
    val søkEtterInnkrevingskrav: SøkEtterInnkrevingskrav by dependencies
    val kravAccessPolicy: LesKravAccessPolicy by dependencies
    val auditLog: AuditLog by dependencies

    routing {
        route("/internal") {
            authenticate(AuthenticationConfigName.ENTRA_ID.configName) {
                context(kravAccessPolicy, auditLog) {
                    route("/kravdetaljer") {
                        hentKravdetaljerRoute(hentKravdetaljer)
                    }
                    route("/kravoversikt") {
                        hentKravoversikt(søkEtterInnkrevingskrav)
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
