package no.nav.tilbakekreving.infrastructure.client.skatteetaten.json

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import no.nav.tilbakekreving.domain.Kravtype

class KravtypeSerializerTest :
    WordSpec({
        val json = Json { ignoreUnknownKeys = true }

        "KravtypeSerializer" should {
            "deserialisere kjent kravtype" {
                val result = json.decodeFromString(KravtypeSerializer, "\"TILBAKEKREVING_BARNETRYGD\"")
                result shouldBe Kravtype.TILBAKEKREVING_BARNETRYGD
            }

            "deserialisere ukjent kravtype til UKJENT" {
                val result = json.decodeFromString(KravtypeSerializer, "\"HELT_NY_KRAVTYPE_FRA_SKATTEETATEN\"")
                result shouldBe Kravtype.UKJENT
            }

            "serialisere kravtype til streng" {
                val result = json.encodeToString(KravtypeSerializer, Kravtype.TILBAKEKREVING_BARNETRYGD)
                result shouldBe "\"TILBAKEKREVING_BARNETRYGD\""
            }

            "serialisere UKJENT til streng" {
                val result = json.encodeToString(KravtypeSerializer, Kravtype.UKJENT)
                result shouldBe "\"UKJENT\""
            }
        }
    })
