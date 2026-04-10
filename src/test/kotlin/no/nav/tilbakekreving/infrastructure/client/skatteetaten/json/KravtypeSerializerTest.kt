package no.nav.tilbakekreving.infrastructure.client.skatteetaten.json

import arrow.core.left
import arrow.core.right
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import no.nav.tilbakekreving.domain.Kravtype
import no.nav.tilbakekreving.domain.UkjentKravtype

class KravtypeSerializerTest :
    WordSpec({
        val json = Json { ignoreUnknownKeys = true }

        "KravtypeSerializer" should {
            "deserialisere kjent kravtype" {
                val result = json.decodeFromString(KravtypeSerializer, "\"TILBAKEKREVING_BARNETRYGD\"")
                result shouldBe Kravtype.TILBAKEKREVING_BARNETRYGD.right()
            }

            "deserialisere ukjent kravtype til UkjentKravtype" {
                val result = json.decodeFromString(KravtypeSerializer, "\"HELT_NY_KRAVTYPE_FRA_SKATTEETATEN\"")
                result shouldBe UkjentKravtype("HELT_NY_KRAVTYPE_FRA_SKATTEETATEN").left()
            }

            "serialisere kravtype til streng" {
                val result = json.encodeToString(KravtypeSerializer, Kravtype.TILBAKEKREVING_BARNETRYGD.right())
                result shouldBe "\"TILBAKEKREVING_BARNETRYGD\""
            }

            "serialisere ukjent kravtype til opprinnelig streng" {
                val result = json.encodeToString(KravtypeSerializer, UkjentKravtype("EN_NY_TYPE").left())
                result shouldBe "\"EN_NY_TYPE\""
            }
        }
    })
