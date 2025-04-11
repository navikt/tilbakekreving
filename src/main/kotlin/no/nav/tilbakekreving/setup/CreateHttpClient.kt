package no.nav.tilbakekreving.setup

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import no.nav.tilbakekreving.AppEnv

fun createHttpClient(
    engine: HttpClientEngine,
    appEnv: AppEnv,
    httpClientConfig: HttpClientConfig<*>.() -> Unit = {},
): HttpClient =
    HttpClient(engine) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                },
            )
        }
        install(Logging) {
            level =
                when (appEnv) {
                    AppEnv.DEV -> LogLevel.ALL
                    else -> LogLevel.INFO
                }
        }
        httpClientConfig()
    }
