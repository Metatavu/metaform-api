package fi.metatavu.metaform.server.controllers

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import fi.metatavu.metaform.api.spec.model.FieldRule
import fi.metatavu.metaform.api.spec.model.Reply
import fi.metatavu.metaform.server.email.SendEmailEvent
import fi.metatavu.metaform.server.email.EmailAbstractFreemarkerRenderer
import fi.metatavu.metaform.server.email.EmailTemplateSource
import fi.metatavu.metaform.server.metaform.FieldRuleEvaluator
import fi.metatavu.metaform.server.persistence.dao.EmailNotificationDAO
import fi.metatavu.metaform.server.persistence.dao.EmailNotificationEmailDAO
import fi.metatavu.metaform.server.persistence.model.Metaform
import fi.metatavu.metaform.server.persistence.model.notifications.EmailNotification
import fi.metatavu.metaform.server.persistence.model.notifications.EmailNotificationEmail
import fi.metatavu.metaform.server.rest.translate.EmailNotificationTranslator
import org.slf4j.Logger
import java.io.IOException
import java.util.*
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Event
import jakarta.enterprise.event.TransactionPhase
import jakarta.inject.Inject

/**
 * Controller for Email notifications
 */
@ApplicationScoped
class EmailNotificationController {

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var emailNotificationTranslator: EmailNotificationTranslator

    @Inject
    lateinit var freemarkerRenderer: EmailAbstractFreemarkerRenderer

    @Inject
    lateinit var emailNotificationDAO: EmailNotificationDAO

    @Inject
    lateinit var emailNotificationEmailDAO: EmailNotificationEmailDAO

    @Inject
    lateinit var emailEvent: Event<SendEmailEvent>

    /**
     * Creates email notification
     *
     * @param metaform metaform
     * @param subjectTemplate subject template
     * @param contentTemplate content template
     * @param emails list of email addresses
     * @param notifyIf notify if rule or null if not defined
     * @return created email notification
     * @throws JsonProcessingException thrown when notify if JSON processing fails
     */
    @Throws(JsonProcessingException::class)
    fun createEmailNotification(
            metaform: Metaform,
            subjectTemplate: String,
            contentTemplate: String,
            emails: List<String>,
            notifyIf: FieldRule?
    ): EmailNotification {
        val emailNotification = emailNotificationDAO.create(
                id = UUID.randomUUID(),
                metaform = metaform,
                subjectTemplate = subjectTemplate,
                contentTemplate = contentTemplate,
                notifyIf =  serializeFieldRule(notifyIf)
        )
        emails.forEach { email -> emailNotificationEmailDAO.create(UUID.randomUUID(), emailNotification, email) }
        return emailNotification
    }

    /**
     * Finds an email notification
     *
     * @param id id
     * @return a email notification
     */
    fun findEmailNotificationById(id: UUID): EmailNotification? {
        return emailNotificationDAO.findById(id)
    }

    /**
     * Lists an email notifications from Metaform
     *
     * @param metaform metaform
     * @return a email notification
     */
    fun listEmailNotificationByMetaform(metaform: Metaform): List<EmailNotification> {
        return emailNotificationDAO.listByMetaform(metaform)
    }

    /**
     * Updates email notification
     *
     * @param emailNotification email notification
     * @param subjectTemplate subject template
     * @param contentTemplate content template
     * @param emails list of email addresses
     * @param notifyIf notify if rule or null if not defined
     * @return updated email notification
     * @throws JsonProcessingException thrown when notify if JSON processing fails
     */
    @Throws(JsonProcessingException::class)
    fun updateEmailNotification(
            emailNotification: EmailNotification,
            subjectTemplate: String,
            contentTemplate: String,
            emails: List<String>,
            notifyIf: FieldRule?
    ): EmailNotification {
        emailNotificationDAO.updateSubjectTemplate(emailNotification, subjectTemplate)
        emailNotificationDAO.updateContentTemplate(emailNotification, contentTemplate)
        emailNotificationDAO.updateNotifyIf(emailNotification, serializeFieldRule(notifyIf))
        deleteNotificationEmails(emailNotification)
        emails.forEach { email -> emailNotificationEmailDAO.create(UUID.randomUUID(), emailNotification, email) }
        return emailNotification
    }

    /**
     * Returns whether email notification should be sent according to notify if rule
     *
     * @param emailNotification email notification
     * @param replyEntity reply entity
     * @return whether email notification should be sent according to notify if rule
     */
    fun evaluateEmailNotificationNotifyIf(emailNotification: EmailNotification, replyEntity: Reply): Boolean {
        return evaluateEmailNotificationNotifyIf(emailNotificationTranslator.translate(emailNotification), replyEntity)
    }

    /**
     * Returns whether email notification should be sent according to notify if rule
     *
     * @param emailNotificationEntity email notification entity
     * @param replyEntity reply entity
     * @return whether email notification should be sent according to notify if rule
     */
    fun evaluateEmailNotificationNotifyIf(emailNotificationEntity: fi.metatavu.metaform.api.spec.model.EmailNotification, replyEntity: Reply): Boolean {
        val notifyIf = emailNotificationEntity.notifyIf
        return if (notifyIf != null) {
            FieldRuleEvaluator().evaluate(notifyIf, replyEntity)
        } else true
    }

    /**
     * Returns list of email addresses in email notification
     *
     * @param emailNotification email notification
     * @return List of email addresses in email notification
     */
    fun getEmailNotificationEmails(emailNotification: EmailNotification?): List<String> {
        return emailNotificationEmailDAO.listByEmailNotification(emailNotification)
                .mapNotNull(EmailNotificationEmail::email)
    }

    /**
     * Send email notification
     *
     * @param emailNotification email notification
     * @param replyEntity reply posted
     * @param emails notify emails
     */
    fun sendEmailNotification(emailNotification: EmailNotification, replyEntity: Reply?, emails: Set<String>) {
        val id = emailNotification.id!!
        val data = toFreemarkerData(replyEntity)
        val subject = freemarkerRenderer.render(
            configuration = freemarkerRenderer.configuration,
            templateName = EmailTemplateSource.EMAIL_SUBJECT.getName(id),
            dataModel = data,
            locale = DEFAULT_LOCALE
        )
        val content = freemarkerRenderer.render(
            configuration = freemarkerRenderer.configuration,
            templateName = EmailTemplateSource.EMAIL_CONTENT.getName(id),
            dataModel = data,
            locale = DEFAULT_LOCALE
        )

        emails.forEach { email ->
            emailEvent.fire(
                SendEmailEvent(
                    toEmail = email,
                    subject = subject,
                    content = content,
                    transactionPhase = TransactionPhase.AFTER_SUCCESS
                )
            )
        }
    }

    /**
     * Delete email notification
     *
     * @param emailNotification email notification
     */
    fun deleteEmailNotification(emailNotification: EmailNotification) {
        deleteNotificationEmails(emailNotification)
        emailNotificationDAO.delete(emailNotification)
    }

    /**
     * Converts reply to Freemarker data
     *
     * @param reply reply
     * @return freemarker data
     */
    private fun toFreemarkerData(reply: Reply?): Map<String, Any>? {
        if (reply == null) {
            return null
        }
        val objectMapper = ObjectMapper()
        objectMapper.registerModule(JavaTimeModule())
        try {
            return objectMapper.readValue(objectMapper.writeValueAsString(reply), object : TypeReference<Map<String, Any>>() {})
        } catch (e: IOException) {
            logger.error("Failed to convert reply into freemarker data", e)
        }
        return null
    }

    /**
     * Serializes field rule as string
     *
     * @param fieldRule field rule
     * @return serialized field rule
     * @throws JsonProcessingException
     */
    @Throws(JsonProcessingException::class)
    private fun serializeFieldRule(fieldRule: FieldRule?): String? {
        if (fieldRule == null) {
            return null
        }
        val objectMapper = ObjectMapper()
        return objectMapper.writeValueAsString(fieldRule)
    }

    /**
     * Deletes email notification entities
     *
     * @param emailNotification email notification
     */
    private fun deleteNotificationEmails(emailNotification: EmailNotification) {
        val emailNotificationEmails = emailNotificationEmailDAO.listByEmailNotification(emailNotification)
        emailNotificationEmails.forEach(emailNotificationEmailDAO::delete)
    }

    companion object {
        private val DEFAULT_LOCALE = Locale("fi")
    }

}