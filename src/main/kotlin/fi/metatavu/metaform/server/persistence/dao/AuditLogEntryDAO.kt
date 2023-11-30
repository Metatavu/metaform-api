package fi.metatavu.metaform.server.persistence.dao

import fi.metatavu.metaform.api.spec.model.AuditLogEntryType
import fi.metatavu.metaform.server.persistence.model.*
import java.time.OffsetDateTime
import java.util.*
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.Predicate
import kotlin.collections.ArrayList


/**
 * DAO class for AuditLogEntry entity
 *
 * @author Katja Danilova
 */
@ApplicationScoped
class AuditLogEntryDAO : AbstractDAO<AuditLogEntry>() {

  /**
   * Creates AuditLogEntry
   *
   * @param id uuid of log entry
   * @param metaform metaform
   * @param userId userId
   * @param auditLogEntryType log entry type
   * @param replyId reply Id
   * @param attachmentId attachment Id
   * @param message message
   * @return created auditlogentry
   */
  fun create(
    id: UUID,
    metaform: Metaform,
    userId: UUID,
    auditLogEntryType: AuditLogEntryType,
    replyId: UUID?,
    attachmentId: UUID?,
    message: String?
  ): AuditLogEntry {
    val auditLogEntry = AuditLogEntry()
    auditLogEntry.id = id
    auditLogEntry.metaform = metaform
    auditLogEntry.userId = userId
    auditLogEntry.logEntryType = auditLogEntryType
    auditLogEntry.replyId = replyId
    auditLogEntry.attachmentId = attachmentId
    auditLogEntry.message = message
    return persist(auditLogEntry)
  }

  /**
   * Gets audit log entries by replies, user id, created before and after parameters
   *
   * @param metaform replyId
   * @param replyId replyId
   * @param userId userId
   * @param createdBefore created before
   * @param createdAfter created after
   * @return list of AuditLogEntry
   */
  fun list(
    metaform: Metaform,
    replyId: UUID?,
    userId: UUID?,
    createdBefore: OffsetDateTime?,
    createdAfter: OffsetDateTime?
  ): List<AuditLogEntry> {
    val criteriaBuilder = entityManager.criteriaBuilder

    val criteria = criteriaBuilder.createQuery(
      AuditLogEntry::class.java
    )
    val root = criteria.from(
      AuditLogEntry::class.java
    )

    val restrictions: MutableList<Predicate> = ArrayList()

    restrictions.add(criteriaBuilder.equal(root.get(AuditLogEntry_.metaform), metaform))
    if (replyId != null) {
      restrictions.add(criteriaBuilder.equal(root.get(AuditLogEntry_.replyId), replyId))
    }
    if (userId != null) {
      restrictions.add(criteriaBuilder.equal(root.get(AuditLogEntry_.userId), userId))
    }
    if (createdBefore != null) {
      restrictions.add(
        criteriaBuilder.lessThanOrEqualTo(
          root.get(AuditLogEntry_.createdAt),
          createdBefore
        )
      )
    }
    if (createdAfter != null) {
      restrictions.add(
        criteriaBuilder.greaterThanOrEqualTo(
          root.get(AuditLogEntry_.createdAt),
          createdAfter
        )
      )
    }
    criteria.select(root)
    criteria.where(criteriaBuilder.and(*restrictions.toTypedArray()))
    criteria.orderBy(criteriaBuilder.asc(root.get(AuditLogEntry_.createdAt)))
    val query = entityManager.createQuery(criteria)
    return query.resultList
  }

  /**
   * Lists audit log entries by metaform
   *
   * @param metaform metaform
   * @return list of audit log entries
   */
  fun listByMetaform(metaform: Metaform): List<AuditLogEntry> {
    val criteriaBuilder = entityManager.criteriaBuilder
    val criteria = criteriaBuilder.createQuery(
      AuditLogEntry::class.java
    )
    val root = criteria.from(
      AuditLogEntry::class.java
    )
    criteria.select(root)
    criteria.where(criteriaBuilder.equal(root.get(AuditLogEntry_.metaform), metaform))
    val query = entityManager.createQuery(criteria)
    return query.resultList
  }

  /**
   * Gets average process delay for given Metaforms replies
   *
   * @param metaform metaform
   * @returns average reply process delay
   */
  fun getAverageProcessDelayByMetaform(metaform: Metaform): Double? {
    val criteriaBuilder = entityManager.criteriaBuilder
    val criteria = criteriaBuilder.createQuery(Double::class.java)
    val root = criteria.from(Metaform::class.java)
    val viewedJoin = root.join(Metaform_.metaformReplyViewed)
    val createdJoin = root.join(Metaform_.metaformReplyCreated)
    val replyCreated = createdJoin.get(MetaformReplyCreated_.replyCreated)
    val replyViewed = viewedJoin.get(MetaformReplyViewed_.replyViewed)

    criteria.select(
      criteriaBuilder.avg(
        criteriaBuilder.diff(
          createToSecondsSqlFunction(criteriaBuilder, replyViewed),
          createToSecondsSqlFunction(criteriaBuilder, replyCreated)
        )
      )
    )

    criteria.where(criteriaBuilder.equal(root.get(Metaform_.id), metaform.id!!))

    return getSingleResult(entityManager.createQuery(criteria))
  }

  /**
   * Creates SQL TO_SECONDS function for Criteria Query
   *
   * @param criteriaBuilder criteria builder
   * @param value value to convert to seconds
   * @returns
   */
  private fun createToSecondsSqlFunction(criteriaBuilder: CriteriaBuilder, value: Expression<*>): Expression<Int>? {
    return criteriaBuilder.function(
      "TO_SECONDS",
      Int::class.java,
      value
    )
  }
}