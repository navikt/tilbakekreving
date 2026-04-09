package no.nav.tilbakekreving.infrastructure.client.skatteetaten.json

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import no.nav.tilbakekreving.domain.Kravtype
import org.slf4j.LoggerFactory

object KravtypeSerializer : KSerializer<Kravtype> {
    private val logger = LoggerFactory.getLogger(KravtypeSerializer::class.java)

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Kravtype", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Kravtype {
        val value = decoder.decodeString()
        return try {
            Kravtype.valueOf(value)
        } catch (_: IllegalArgumentException) {
            logger.error("Mottok ukjent kravtype fra Skatteetaten: {}", value)
            Kravtype.UKJENT
        }
    }

    override fun serialize(
        encoder: Encoder,
        value: Kravtype,
    ) {
        encoder.encodeString(value.name)
    }
}
