package fi.metatavu.metaform.server.email

import fi.metatavu.metaform.server.email.mailgun.MailFormat

/**
 * Interface that describes a single email provider
 *
 * @author Heikki Kurhinen
 * @author Antti Leppä
 */
interface EmailProvider {
    /**
     * Sends an email
     *
     * @param toEmail recipient's email address
     * @param subject email's subject
     * @param content email's content
     * @param format email format
     */
    fun sendMail(toEmail: String?, subject: String?, content: String?, format: MailFormat?)
}