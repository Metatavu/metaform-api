package fi.metatavu.metaform.server.rest

import com.fasterxml.jackson.core.JsonProcessingException
import fi.metatavu.metaform.api.spec.model.EmailNotification
import fi.metatavu.metaform.server.controllers.EmailNotificationController
import fi.metatavu.metaform.server.controllers.MetaformController
import fi.metatavu.metaform.server.persistence.model.Metaform
import fi.metatavu.metaform.server.rest.translate.EmailNotificationTranslator
import java.util.*
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.core.Response

@RequestScoped
@Transactional
class EmailNotificationsApi : fi.metatavu.metaform.api.spec.EmailNotificationsApi, AbstractApi() {

  @Inject
  lateinit var metaformController: MetaformController

  @Inject
  lateinit var emailNotificationController: EmailNotificationController

  @Inject
  lateinit var emailNotificationTranslator: EmailNotificationTranslator

  override suspend fun listEmailNotifications(metaformId: UUID): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isRealmMetaformAdmin) {
      return createForbidden(createNotAllowedMessage(LIST, EMAIL_NOTIFICATION))
    }

    val metaform: Metaform = metaformController.findMetaformById(metaformId)
            ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    return createOk(emailNotificationController.listEmailNotificationByMetaform(metaform)
            .map(emailNotificationTranslator::translate))

  }

  override suspend fun createEmailNotification(
    metaformId: UUID,
    emailNotification: EmailNotification
  ): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isRealmMetaformAdmin) {
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

  override suspend fun deleteEmailNotification(
    metaformId: UUID,
    emailNotificationId: UUID
  ): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isRealmMetaformAdmin) {
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

  override suspend fun findEmailNotification(
    metaformId: UUID,
    emailNotificationId: UUID
  ): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isRealmMetaformAdmin) {
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

  override suspend fun updateEmailNotification(
    metaformId: UUID,
    emailNotificationId: UUID,
    emailNotification: EmailNotification
  ): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isRealmMetaformAdmin) {
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