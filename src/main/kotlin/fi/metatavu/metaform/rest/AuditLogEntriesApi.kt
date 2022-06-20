package fi.metatavu.metaform.rest

import java.util.*
import javax.ws.rs.core.Response

class AuditLogEntriesApi: fi.metatavu.metaform.api.spec.AuditLogEntriesApi {
  override suspend fun deleteAuditLogEntry(metaformId: UUID, auditLogEntryId: UUID): Response {
    TODO("Not yet implemented")
  }

  override suspend fun listAuditLogEntries(
    metaformId: UUID,
    userId: UUID?,
    replyId: UUID?,
    createdBefore: String?,
    createdAfter: String?
  ): Response {
    TODO("Not yet implemented")
  }
}