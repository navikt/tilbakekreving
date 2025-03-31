package no.nav

enum class AppEnv {
    LOCAL,
    DEV,
    PROD,
    ;

    companion object {
        fun getFromEnvVariable(name: String): AppEnv =
            when (System.getenv(name)) {
                "dev" -> DEV
                "prod" -> PROD
                else -> LOCAL
            }
    }
}