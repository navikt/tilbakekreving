package no.nav.tilbakekreving

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import org.slf4j.LoggerFactory

class AppEnvTest :
    WordSpec({
        val logger = LoggerFactory.getLogger("test")
        val envVarName = "NAIS_CLUSTER_NAME_TEST"

        afterEach { removeEnvVar(envVarName) }

        "getFromEnvVariable" should {
            "returnere DEV for dev-gcp" {
                setEnvVar(envVarName, "dev-gcp")

                val result = with(logger) { AppEnv.getFromEnvVariable(envVarName) }

                result shouldBe AppEnv.DEV
            }

            "returnere PROD for prod-gcp" {
                setEnvVar(envVarName, "prod-gcp")

                val result = with(logger) { AppEnv.getFromEnvVariable(envVarName) }

                result shouldBe AppEnv.PROD
            }

            "returnere LOCAL for ukjent verdi" {
                setEnvVar(envVarName, "something-else")

                val result = with(logger) { AppEnv.getFromEnvVariable(envVarName) }

                result shouldBe AppEnv.LOCAL
            }

            "returnere LOCAL når miljøvariabel ikke er satt" {
                val result = with(logger) { AppEnv.getFromEnvVariable(envVarName) }

                result shouldBe AppEnv.LOCAL
            }
        }
    })

@Suppress("UNCHECKED_CAST")
private fun getEditableEnvMap(): MutableMap<String, String> {
    val env = System.getenv()
    val field = env.javaClass.getDeclaredField("m")
    field.isAccessible = true
    return field.get(env) as MutableMap<String, String>
}

private fun setEnvVar(
    key: String,
    value: String,
) {
    getEditableEnvMap()[key] = value
}

private fun removeEnvVar(key: String) {
    getEditableEnvMap().remove(key)
}
