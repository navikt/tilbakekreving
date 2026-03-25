package no.nav.tilbakekreving.infrastructure.client.texas.json

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonPrimitive

@Serializable(with = ValidateTokenResponseSerializer::class)
sealed class ValidateTokenResponse {
    abstract val active: Boolean

    @Serializable
    @SerialName("valid")
    data class ValidTokenResponse(
        override val active: Boolean,
        @Suppress("PropertyName") val NAVident: String,
        val exp: Long,
        val iat: Long,
        val groups: List<String>,
    ) : ValidateTokenResponse()

    @Serializable
    @SerialName("invalid")
    data class InvalidTokenResponse(
        override val active: Boolean,
        val error: String,
    ) : ValidateTokenResponse()
}

class ValidateTokenResponseSerializer : KSerializer<ValidateTokenResponse> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ValidateTokenResponse")

    override fun serialize(
        encoder: Encoder,
        value: ValidateTokenResponse,
    ) = throw NotImplementedError("Serialization is not implemented for ValidateTokenResponse")

    override fun deserialize(decoder: Decoder): ValidateTokenResponse {
        val jsonDecoder = decoder as? JsonDecoder ?: throw IllegalArgumentException("Expected JsonDecoder")
        val jsonObject =
            jsonDecoder.decodeJsonElement() as? JsonObject ?: throw IllegalArgumentException("Expected JsonObject")

        val active = jsonObject["active"]?.jsonPrimitive?.boolean ?: false

        return if (active) {
            jsonDecoder.json.decodeFromJsonElement(ValidateTokenResponse.ValidTokenResponse.serializer(), jsonObject)
        } else {
            jsonDecoder.json.decodeFromJsonElement(ValidateTokenResponse.InvalidTokenResponse.serializer(), jsonObject)
        }
    }
}
