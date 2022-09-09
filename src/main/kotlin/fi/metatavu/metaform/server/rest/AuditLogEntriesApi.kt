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

  /**
   * Delete audit log entry
   * 
   * @param metaformId metaform id
   * @param auditLogEntryId audit log entry id
   * 
   * @return deleted audit log entry
   */
  override fun deleteAuditLogEntry(metaformId: UUID, auditLogEntryId: UUID): Response {
    if (!systemSettingController.inTestMode()) {
      return createForbidden(createNotAllowedMessage(DELETE, AUDIT_LOG_ENTRY))
    }

    metaformController.findMetaformById(metaformId)
            ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    val auditLogEntry: AuditLogEntry = auditLogEntryController.findAuditLogEntryById(auditLogEntryId)
            ?: return createNotFound(createNotFoundMessage(AUDIT_LOG_ENTRY, auditLogEntryId))
    auditLogEntryController.deleteAuditLogEntry(auditLogEntry)
    return createNoContent()
  }

  /**
   * List audit log entries
   * 
   * @param metaformId metaform id
   * @param userId user id
   * @param replyId reply id
   * @param createdBefore filter results created before this date
   * @param createdAfter filter results created after this date
   * @return list of audit log entries
   */
  override fun listAuditLogEntries(
    metaformId: UUID,
    userId: UUID?,
    replyId: UUID?,
    createdBefore: String?,
    createdAfter: String?
  ): Response {
    if (!hasRealmRole(VIEW_AUDIT_LOGS_ROLE) && !isMetaformAdmin(metaformId)) {
      return createForbidden(createNotAllowedMessage(LIST, AUDIT_LOG_ENTRY))
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