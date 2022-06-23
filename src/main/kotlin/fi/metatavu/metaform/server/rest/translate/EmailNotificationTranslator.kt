package fi.metatavu.metaform.server.rest.translate

import com.fasterxml.jackson.databind.ObjectMapper
import fi.metatavu.metaform.api.spec.model.EmailNotification
import fi.metatavu.metaform.api.spec.model.FieldRule
import fi.metatavu.metaform.server.notifications.EmailNotificationController
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import java.io.IOException
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Translator for email notifications
 *
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
@ApplicationScoped
class EmailNotificationTranslator {

  @Inject
  lateinit var logger: Logger

  @Inject
  lateinit var emailNotificationController: EmailNotificationController

  /**
   * Translates JPA email notification object into REST email notification object
   *
   * @param emailNotification JPA emailNotification object
   * @return REST emailNotification
   */
  fun translateEmailNotification(emailNotification: fi.metatavu.metaform.server.persistence.model.notifications.EmailNotification?): EmailNotification? {
    if (emailNotification == null) {
      return null
    }

    return EmailNotification(
      id = emailNotification.id,
      emails = emailNotificationController.getEmailNotificationEmails(emailNotification),
      subjectTemplate = emailNotification.subjectTemplate!!,
      contentTemplate = emailNotification.contentTemplate!!,
      notifyIf = deserializeFieldRule(emailNotification.notifyIf)
    )
  }

  /**
   * Deserializes the field rule JSON
   *
   * @param json field rule JSON
   * @return field rule object
   */
  private fun deserializeFieldRule(json: String?): FieldRule? {
    if (json == null || StringUtils.isBlank(json)) {
      return null
    }

    val objectMapper = ObjectMapper()
    try {
      return objectMapper.readValue(json, FieldRule::class.java)
    } catch (e: IOException) {
      logger.error("Failed to read notify if rule", e)
    }
    return null
  }
}