package fi.metatavu.metaform.server.email

import jakarta.enterprise.event.TransactionPhase

/**
 * Event for sending emails
 *
 * @param toEmail email address
 * @param subject subject
 * @param content content
 * @param transactionPhase transaction phase when the email should be sent
 */
data class SendEmailEvent(val toEmail: String, val subject: String?, val content: String?, val transactionPhase: TransactionPhase)