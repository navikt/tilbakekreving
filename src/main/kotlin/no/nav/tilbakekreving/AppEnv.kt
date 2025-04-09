package no.nav.tilbakekreving

import io.ktor.util.logging.Logger

enum class AppEnv {
    LOCAL,
    DEV,
    PROD,
    ;

    companion object {
        fun getFromEnvVariable(
            name: String,
            log: Logger,
        ): AppEnv {
            val envVar: String? = System.getenv(name)
            log.info("Environment variable $name is set to $envVar")
            return when (envVar) {
                "dev-gcp" -> DEV
                "prod-gcp" -> PROD
                else -> LOCAL
            }
        }
    }
}
