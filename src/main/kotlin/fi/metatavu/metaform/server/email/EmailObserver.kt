package fi.metatavu.metaform.server.email

import org.slf4j.Logger
import javax.annotation.PostConstruct
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.event.Observes
import javax.enterprise.event.TransactionPhase
import javax.inject.Inject

/**
 * Observer for sending emails with CDI events
 *
 * @author Antti Leppä
 */
@ApplicationScoped
@Suppress ("unused")
class EmailObserver {

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var emailProvider: EmailProvider

    /**
     * Initializes observer. Method assures eager initialization of observer
     */
    @PostConstruct
    fun init() {
        // this method is left empty on purpose
    }

    /**
     * Event handler for sending emails after successful transaction
     *
     * @param event event
     */
    fun onSendEmailEventAfterSuccess(@Observes(during = TransactionPhase.AFTER_SUCCESS) event: SendEmailEvent) {
        if (event.transactionPhase == TransactionPhase.AFTER_SUCCESS) {
            try {
                emailProvider.sendMail(
                    toEmail = event.toEmail,
                    subject = event.subject,
                    content = event.content,
                    format = event.format
                )
            } catch (e: Exception) {
                logger.error("Failed to send email", e)
            }
        }
    }

    /**
     * Event handler for sending emails immediately
     *
     * @param event event
     */
    fun onSendEmailEventInProgress(@Observes(during = TransactionPhase.IN_PROGRESS) event: SendEmailEvent) {
        if (event.transactionPhase == TransactionPhase.IN_PROGRESS) {
            try {
                emailProvider.sendMail(
                    toEmail = event.toEmail,
                    subject = event.subject,
                    content = event.content,
                    format = event.format
                )
            } catch (e: Exception) {
                logger.error("Failed to send email", e)
            }
        }
    }

}