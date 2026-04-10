package no.nav.tilbakekreving.infrastructure.client.skatteetaten.json

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import no.nav.tilbakekreving.domain.Kravtype
import no.nav.tilbakekreving.domain.UkjentKravtype
import org.slf4j.LoggerFactory

object KravtypeSerializer : KSerializer<Either<UkjentKravtype, Kravtype>> {
    private val logger = LoggerFactory.getLogger(KravtypeSerializer::class.java)

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Kravtype", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Either<UkjentKravtype, Kravtype> {
        val value = decoder.decodeString()
        return try {
            Kravtype.valueOf(value).right()
        } catch (_: IllegalArgumentException) {
            logger.error("Mottok ukjent kravtype fra Skatteetaten: {}", value)
            UkjentKravtype(value).left()
        }
    }

    override fun serialize(
        encoder: Encoder,
        value: Either<UkjentKravtype, Kravtype>,
    ) {
        encoder.encodeString(
            value.fold(
                ifLeft = { it.value },
                ifRight = { it.name },
            ),
        )
    }
}
