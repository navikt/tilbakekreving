package no.nav.tilbakekreving.setup

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import no.nav.tilbakekreving.AppEnv

/**
 * Oppretter en HTTP-klient med standard konfigurasjoner.
 *
 * Klienten kan videre konfigureres med [HttpClient.config] ved behov for utvidet funksjonalitet.
 */
context(appEnv: AppEnv)
fun createHttpClient(engine: HttpClientEngine): HttpClient =
    HttpClient(engine) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                },
            )
        }
        install(Logging) {
            level =
                when (appEnv) {
                    AppEnv.LOCAL, AppEnv.DEV -> LogLevel.ALL
                    else -> LogLevel.INFO
                }
            sanitizeHeader { header -> header == HttpHeaders.Authorization }
        }
    }
