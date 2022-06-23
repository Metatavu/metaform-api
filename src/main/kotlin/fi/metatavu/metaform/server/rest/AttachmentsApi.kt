package fi.metatavu.metaform.server.rest

import java.util.*
import javax.ws.rs.core.Response

class AttachmentsApi: fi.metatavu.metaform.api.spec.AttachmentsApi {
  override suspend fun findAttachment(attachmentId: UUID, ownerKey: String?): Response {
    TODO("Not yet implemented")
  }

  override suspend fun findAttachmentData(attachmentId: UUID, ownerKey: String?): Response {
    TODO("Not yet implemented")
  }
}