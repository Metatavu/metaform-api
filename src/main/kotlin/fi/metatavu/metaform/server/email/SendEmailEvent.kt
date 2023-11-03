package fi.metatavu.metaform.server.email

import fi.metatavu.metaform.server.email.mailgun.MailFormat
import javax.enterprise.event.TransactionPhase

/**
 * Event for sending emails
 *
 * @param toEmail email address
 * @param subject subject
 * @param content content
 * @param format format
 * @param transactionPhase transaction phase when the email should be sent
 */
data class SendEmailEvent(val toEmail: String, val subject: String?, val content: String?, val format: MailFormat, val transactionPhase: TransactionPhase, val attachment: ByteArray?)