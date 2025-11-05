package no.nav.tilbakekreving.infrastructure.audit

interface AuditLog {
    fun info(message: Message)

    /**
     * Representerer en melding som skal logges i auditloggen.
     *
     * @param sourceUserId Hvem som utførte handlingen. F.eks. NAV-ID på ansatt.
     * @param destinationUserId Hvem handlingen gjelder. F.eks. fødselsnummeret til en bruker eller orgnummer.
     * @param event Type hendelse som skal logges. F.eks. CREATE, ACCESS, UPDATE, DELETE.
     * @param message Innholdet i meldingen som skal logges.
     */
    data class Message(
        val sourceUserId: String,
        val destinationUserId: String,
        val event: EventType,
        val message: String,
    )

    /**
     * Konfigurasjon for auditloggen.
     *
     * @param applicationName Navnet på applikasjonen som logger.
     * @param loggerName Navnet på loggeren som brukes.
     * @param loggType Typen logg som skal brukes.
     */
    data class Config(
        val applicationName: String,
        val loggerName: String,
        val loggType: String,
    )

    enum class EventType {
        CREATE,
        ACCESS,
        UPDATE,
        DELETE,
    }
}
