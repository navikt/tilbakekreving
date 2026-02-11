package no.nav.tilbakekreving.infrastructure.client.json

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
import no.nav.tilbakekreving.infrastructure.auth.GroupId
import no.nav.tilbakekreving.infrastructure.client.AccessTokenVerifier

@Serializable(with = VerifyTokenResponseSerializer::class)
sealed class VerifyTokenResponse {
    abstract val active: Boolean

    @Serializable
    @SerialName("valid")
    data class ValidTokenResponse(
        override val active: Boolean,
        @Suppress("PropertyName") val NAVident: String,
        val exp: Long,
        val iat: Long,
        val groups: List<String>,
    ) : VerifyTokenResponse() {
        fun toDomain(): AccessTokenVerifier.ValidatedToken =
            AccessTokenVerifier.ValidatedToken(
                navIdent = NAVident,
                groupIds = groups.map(::GroupId),
            )
    }

    @Serializable
    @SerialName("invalid")
    data class InvalidTokenResponse(
        override val active: Boolean,
        val error: String,
    ) : VerifyTokenResponse()
}

class VerifyTokenResponseSerializer : KSerializer<VerifyTokenResponse> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("VerifyTokenResponse")

    override fun serialize(
        encoder: Encoder,
        value: VerifyTokenResponse,
    ) = throw NotImplementedError("Serialization is not implemented for VerifyTokenResponse")

    override fun deserialize(decoder: Decoder): VerifyTokenResponse {
        val jsonDecoder = decoder as? JsonDecoder ?: throw IllegalArgumentException("Expected JsonDecoder")
        val jsonObject =
            jsonDecoder.decodeJsonElement() as? JsonObject ?: throw IllegalArgumentException("Expected JsonObject")

        val active = jsonObject["active"]?.jsonPrimitive?.boolean ?: false

        return if (active) {
            jsonDecoder.json.decodeFromJsonElement(VerifyTokenResponse.ValidTokenResponse.serializer(), jsonObject)
        } else {
            jsonDecoder.json.decodeFromJsonElement(VerifyTokenResponse.InvalidTokenResponse.serializer(), jsonObject)
        }
    }
}
