package fi.metatavu.metaform.server.email

/**
 * Interface that describes a single email provider
 *
 * @author Heikki Kurhinen
 * @author Antti Leppä
 */
interface EmailProvider {
    /**
     * Sends an email in HTML format
     *
     * @param toEmail recipient's email address
     * @param subject email's subject
     * @param content email's content
     */
    fun sendMail(toEmail: String?, subject: String?, content: String?)
}