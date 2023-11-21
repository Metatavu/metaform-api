package fi.metatavu.metaform.server.rest

import com.fasterxml.jackson.core.JsonProcessingException
import fi.metatavu.metaform.api.spec.model.EmailNotification
import fi.metatavu.metaform.server.controllers.EmailNotificationController
import fi.metatavu.metaform.server.controllers.MetaformController
import fi.metatavu.metaform.server.persistence.model.Metaform
import fi.metatavu.metaform.server.rest.translate.EmailNotificationTranslator
import java.util.*
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.core.Response

@RequestScoped
@Transactional
class EmailNotificationsApi : fi.metatavu.metaform.api.spec.EmailNotificationsApi, AbstractApi() {

  @Inject
  lateinit var metaformController: MetaformController

  @Inject
  lateinit var emailNotificationController: EmailNotificationController

  @Inject
  lateinit var emailNotificationTranslator: EmailNotificationTranslator

  /**
   * Lists email notifications
   * 
   * @param metaformId Metaform id
   * @return List of email notifications
   */
  override fun listEmailNotifications(metaformId: UUID): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isMetaformAdmin(metaformId)) {
      return createForbidden(createNotAllowedMessage(LIST, EMAIL_NOTIFICATION))
    }

    val metaform: Metaform = metaformController.findMetaformById(metaformId)
            ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    return createOk(emailNotificationController.listEmailNotificationByMetaform(metaform)
            .map(emailNotificationTranslator::translate))

  }

  /**
   * Creates email notification
   * 
   * @param metaformId Metaform id
   * @param emailNotification email notification
   * @return created email notification
   */
  override fun createEmailNotification(
    metaformId: UUID,
    emailNotification: EmailNotification
  ): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isMetaformAdmin(metaformId)) {
      return createForbidden(createNotAllowedMessage(LIST, EMAIL_NOTIFICATION))
    }

    val metaform: Metaform = metaformController.findMetaformById(metaformId)
            ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    val createdEmailNotification = try {
      emailNotificationController.createEmailNotification(
              metaform = metaform,
              subjectTemplate = emailNotification.subjectTemplate,
              contentTemplate = emailNotification.contentTemplate,
              emails = emailNotification.emails,
              notifyIf = emailNotification.notifyIf
      )
    } catch (e: JsonProcessingException) {
      return createBadRequest(e.message)
    }

    return createOk(emailNotificationTranslator.translate(createdEmailNotification))

  }

  /**
   * Deletes email notification
   * 
   * @param metaformId Metaform id
   * @param emailNotificationId email notification id
   * @return deleted email notification
   */
  override fun deleteEmailNotification(
    metaformId: UUID,
    emailNotificationId: UUID
  ): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isMetaformAdmin(metaformId)) {
      return createForbidden(createNotAllowedMessage(UPDATE, EMAIL_NOTIFICATION))
    }

    val metaform = metaformController.findMetaformById(metaformId)
            ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    val emailNotification = emailNotificationController.findEmailNotificationById(emailNotificationId)
            ?: return createNotFound(createNotFoundMessage(EMAIL_NOTIFICATION, emailNotificationId))

    if (emailNotification.metaform?.id != metaform.id) {
      return createNotFound(createNotBelongMessage(EMAIL_NOTIFICATION))
    }

    emailNotificationController.deleteEmailNotification(emailNotification)

    return createNoContent()
  }

  /**
   * Finds email notification by id
   * 
   * @param metaformId Metaform id
   * @param emailNotificationId email notification id
   * @return email notification
   */
  override fun findEmailNotification(
    metaformId: UUID,
    emailNotificationId: UUID
  ): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isMetaformAdmin(metaformId)) {
      return createForbidden(createNotAllowedMessage(FIND, EMAIL_NOTIFICATION))
    }

    val metaform = metaformController.findMetaformById(metaformId)
            ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    val emailNotification = emailNotificationController.findEmailNotificationById(emailNotificationId)
            ?: return createNotFound(createNotFoundMessage(EMAIL_NOTIFICATION, emailNotificationId))

    return if (emailNotification.metaform?.id != metaform.id) {
      return createNotFound(createNotBelongMessage(EMAIL_NOTIFICATION))
    } else createOk(emailNotificationTranslator.translate(emailNotification))

  }

  /**
   * Updates email notification
   * 
   * @param metaformId Metaform id
   * @param emailNotificationId email notification id
   * @param emailNotification email data
   * @return updated email notification
   */
  override fun updateEmailNotification(
    metaformId: UUID,
    emailNotificationId: UUID,
    emailNotification: EmailNotification
  ): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isMetaformAdmin(metaformId)) {
      return createForbidden(createNotAllowedMessage(FIND, EMAIL_NOTIFICATION))
    }

    val metaform = metaformController.findMetaformById(metaformId)
            ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    val foundEmailNotification = emailNotificationController.findEmailNotificationById(emailNotificationId)
            ?: return createNotFound(createNotFoundMessage(EMAIL_NOTIFICATION, emailNotificationId))

    if (foundEmailNotification.metaform?.id != metaform.id) {
      return createNotFound(createNotBelongMessage(EMAIL_NOTIFICATION))
    }

    val updatedEmailNotification =  try {
      emailNotificationController.updateEmailNotification(
              foundEmailNotification,
              emailNotification.subjectTemplate,
              emailNotification.contentTemplate,
              emailNotification.emails,
              emailNotification.notifyIf
      )
    } catch (e: JsonProcessingException) {
      return createBadRequest(e.message)
    }

    return createOk(emailNotificationTranslator.translate(updatedEmailNotification))
  }


}