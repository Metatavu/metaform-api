package fi.metatavu.metaform.server.rest

import fi.metatavu.metaform.server.controllers.AuditLogEntryController
import fi.metatavu.metaform.server.controllers.MetaformController
import fi.metatavu.metaform.server.controllers.SystemSettingController
import fi.metatavu.metaform.server.persistence.model.AuditLogEntry
import fi.metatavu.metaform.server.persistence.model.Metaform
import fi.metatavu.metaform.server.rest.translate.AuditLogEntryTranslator
import java.util.*
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.core.Response

@RequestScoped
@Transactional
class AuditLogEntriesApi: fi.metatavu.metaform.api.spec.AuditLogEntriesApi, AbstractApi() {
  @Inject
  lateinit var systemSettingController: SystemSettingController

  @Inject
  lateinit var metaformController: MetaformController

  @Inject
  lateinit var auditLogEntryController: AuditLogEntryController

  @Inject
  lateinit var auditLogEntryTranslator: AuditLogEntryTranslator
  override suspend fun deleteAuditLogEntry(metaformId: UUID, auditLogEntryId: UUID): Response {
    if (!systemSettingController.inTestMode()) {
      return createForbidden(createNotAllowedMessage(DELETE, LOGS))
    }

    metaformController.findMetaformById(metaformId)
            ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    val auditLogEntry: AuditLogEntry = auditLogEntryController.findAuditLogEntryById(auditLogEntryId)
            ?: return createNotFound(createNotFoundMessage(LOGS, auditLogEntryId))
    auditLogEntryController.deleteAuditLogEntry(auditLogEntry)
    return createNoContent()
  }

  override suspend fun listAuditLogEntries(
    metaformId: UUID,
    userId: UUID?,
    replyId: UUID?,
    createdBefore: String?,
    createdAfter: String?
  ): Response {
    if (!hasRealmRole(VIEW_AUDIT_LOGS_ROLE)) {
      return createForbidden(String.format("Only users with %s can access this view", VIEW_AUDIT_LOGS_ROLE))
    }

    val createdBeforeTime = parseTime(createdBefore)
    val createdAfterTime = parseTime(createdAfter)

    val metaform: Metaform = metaformController.findMetaformById(metaformId)
            ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    val auditLogEntries: List<AuditLogEntry> = auditLogEntryController.listAuditLogEntries(metaform, replyId, userId, createdBeforeTime, createdAfterTime)

    val result = auditLogEntries
            .map { auditLogEntryTranslator.translate(it) }

    return createOk(result)
  }
}