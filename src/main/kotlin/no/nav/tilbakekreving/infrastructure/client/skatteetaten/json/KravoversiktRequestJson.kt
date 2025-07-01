package no.nav.tilbakekreving.infrastructure.client.skatteetaten.json

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.jsonObject
import no.nav.tilbakekreving.domain.Kravfilter
import no.nav.tilbakekreving.domain.Skyldner

@Serializable
data class HentKravoversiktRequestJson(
    val skyldner: SkyldnerJson,
    val kravfilter: KravfilterJson,
) {
    companion object {
        fun from(
            skyldner: Skyldner,
            kravfilter: Kravfilter,
        ): HentKravoversiktRequestJson =
            when (skyldner) {
                is Skyldner.Fødselnummer ->
                    HentKravoversiktRequestJson(
                        skyldner = SkyldnerJson.Fødselsnummer(skyldner.id),
                        kravfilter = KravfilterJson.from(kravfilter),
                    )

                is Skyldner.Organisasjonsnummer ->
                    HentKravoversiktRequestJson(
                        skyldner = SkyldnerJson.Organisasjonsnummer(skyldner.id),
                        kravfilter = KravfilterJson.from(kravfilter),
                    )
            }
    }
}

@Serializable(with = SkyldnerJsonSerializer::class)
sealed class SkyldnerJson {
    @Serializable
    data class Fødselsnummer(
        @SerialName("foedselsnummer") val fødselsnummer: String,
    ) : SkyldnerJson()

    @Serializable
    data class Organisasjonsnummer(
        val organisasjonsnummer: String,
    ) : SkyldnerJson()
}

class SkyldnerJsonSerializer : KSerializer<SkyldnerJson> {
    // For serializing, we need structs for each subtype
    private val fødselsnummerSerializer = SkyldnerJson.Fødselsnummer.serializer()
    private val organisasjonsnummerSerializer = SkyldnerJson.Organisasjonsnummer.serializer()

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("SkyldnerJson", kotlinx.serialization.descriptors.PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: SkyldnerJson,
    ) {
        when (value) {
            is SkyldnerJson.Fødselsnummer -> encoder.encodeSerializableValue(fødselsnummerSerializer, value)
            is SkyldnerJson.Organisasjonsnummer -> encoder.encodeSerializableValue(organisasjonsnummerSerializer, value)
        }
    }

    override fun deserialize(decoder: Decoder): SkyldnerJson {
        // This is a simplified approach. In a real implementation, you'd need to
        // check which fields are present to determine the correct type
        val jsonDecoder =
            decoder as? kotlinx.serialization.json.JsonDecoder
                ?: throw IllegalStateException("Can be deserialized only by JSON")

        val json = jsonDecoder.decodeJsonElement().jsonObject

        return when {
            "foedselnummer" in json -> jsonDecoder.json.decodeFromJsonElement(fødselsnummerSerializer, json)
            "organisasjonsnummer" in json -> jsonDecoder.json.decodeFromJsonElement(organisasjonsnummerSerializer, json)
            else -> throw IllegalStateException("Unknown SkyldnerJson type")
        }
    }
}
