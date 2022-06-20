package fi.metatavu.metaform.rest

import fi.metatavu.metaform.api.model.Reply
import java.util.*
import javax.ws.rs.core.Response

class RepliesApi: fi.metatavu.metaform.api.spec.RepliesApi {
  override suspend fun createReply(
    metaformId: UUID,
    reply: Reply,
    updateExisting: Boolean?,
    replyMode: String?
  ): Response {
    TODO("Not yet implemented")
  }

  override suspend fun deleteReply(metaformId: UUID, replyId: UUID, ownerKey: String?): Response {
    TODO("Not yet implemented")
  }

  override suspend fun export(metaformId: UUID, format: String): Response {
    TODO("Not yet implemented")
  }

  override suspend fun findReply(metaformId: UUID, replyId: UUID, ownerKey: String?): Response {
    TODO("Not yet implemented")
  }

  override suspend fun listReplies(
    metaformId: UUID,
    userId: UUID?,
    createdBefore: String?,
    createdAfter: String?,
    modifiedBefore: String?,
    modifiedAfter: String?,
    includeRevisions: Boolean?,
    fields: List<String>?,
    firstResult: Int?,
    maxResults: Int?
  ): Response {
    TODO("Not yet implemented")
  }

  override suspend fun replyExport(metaformId: UUID, replyId: UUID, format: String): Response {
    TODO("Not yet implemented")
  }

  override suspend fun updateReply(
    metaformId: UUID,
    replyId: UUID,
    reply: Reply,
    ownerKey: String?
  ): Response {
    TODO("Not yet implemented")
  }
}