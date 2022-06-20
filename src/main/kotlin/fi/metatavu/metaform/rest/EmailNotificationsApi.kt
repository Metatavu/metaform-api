package fi.metatavu.metaform.rest

import fi.metatavu.metaform.api.model.EmailNotification
import java.util.*
import javax.ws.rs.core.Response

class EmailNotificationsApi : fi.metatavu.metaform.api.spec.EmailNotificationsApi {

  override suspend fun listEmailNotifications(metaformId: UUID): Response {
    TODO("Not yet implemented")
  }

  override suspend fun createEmailNotification(
    metaformId: UUID,
    emailNotification: EmailNotification
  ): Response {
    TODO("Not yet implemented")
  }

  override suspend fun deleteEmailNotification(
    metaformId: UUID,
    emailNotificationId: UUID
  ): Response {
    TODO("Not yet implemented")
  }

  override suspend fun findEmailNotification(
    metaformId: UUID,
    emailNotificationId: UUID
  ): Response {
    TODO("Not yet implemented")
  }

  override suspend fun updateEmailNotification(
    metaformId: UUID,
    emailNotificationId: UUID,
    emailNotification: EmailNotification
  ): Response {
    TODO("Not yet implemented")
  }


}