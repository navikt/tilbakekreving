package no.nav.tilbakekreving

import io.ktor.util.logging.Logger

enum class AppEnv {
    LOCAL,
    DEV,
    PROD,
    ;

    companion object {
        context(logger: Logger?)
        fun getFromEnvVariable(name: String): AppEnv {
            val envVar: String? = System.getenv(name)
            logger?.info("Environment variable $name is set to $envVar")
            return when (envVar) {
                "dev-gcp" -> DEV
                "prod-gcp" -> PROD
                else -> LOCAL
            }
        }
    }
}
