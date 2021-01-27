package fi.metatavu.metaform.server.persistence.dao;

import fi.metatavu.metaform.client.model.AuditLogEntryType;
import fi.metatavu.metaform.server.persistence.model.*;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * DAO class for AuditLogEntry entity
 *
 * @author Katja Danilova
 */
@ApplicationScoped
public class AuditLogEntryDAO extends AbstractDAO<AuditLogEntry> {

  /**
   * Creates AuditLogEntry
   *
   * @param id uuid of log entry
   * @param metaform metaform
   * @param userId userId
   * @param replyId replyId
   * @param attachmentId attachmentId
   * @param message message
   * @return created auditlogentry
   */
  public AuditLogEntry create(UUID id, Metaform metaform, UUID userId, AuditLogEntryType auditLogEntryType, UUID replyId, UUID attachmentId, String message) {
    AuditLogEntry auditLogEntry = new AuditLogEntry();
    auditLogEntry.setId(id);
    auditLogEntry.setMetaform(metaform);
    auditLogEntry.setUserId(userId);
    auditLogEntry.setLogEntryType(auditLogEntryType);
    auditLogEntry.setReplyId(replyId);
    auditLogEntry.setAttachmentId(attachmentId);
    auditLogEntry.setMessage(message);

    return persist(auditLogEntry);
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
  public List<AuditLogEntry> list(Metaform metaform, UUID replyId, UUID userId, OffsetDateTime createdBefore, OffsetDateTime createdAfter) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<AuditLogEntry> criteria = criteriaBuilder.createQuery(AuditLogEntry.class);
    Root<AuditLogEntry> root = criteria.from(AuditLogEntry.class);

    List<Predicate> restrictions = new ArrayList<>();

    restrictions.add(criteriaBuilder.equal(root.get(AuditLogEntry_.metaform), metaform));

    if (replyId != null) {
      restrictions.add(criteriaBuilder.equal(root.get(AuditLogEntry_.replyId), replyId));
    }

    if (userId != null) {
      restrictions.add(criteriaBuilder.equal(root.get(AuditLogEntry_.userId), userId));
    }

    if (createdBefore != null) {
      restrictions.add(criteriaBuilder.lessThanOrEqualTo(root.get(AuditLogEntry_.time), createdBefore));
    }

    if (createdAfter != null) {
      restrictions.add(criteriaBuilder.greaterThanOrEqualTo(root.get(AuditLogEntry_.time), createdAfter));
    }

    criteria.select(root);
    criteria.where(criteriaBuilder.and(restrictions.toArray(new Predicate[0])));
    criteria.orderBy(criteriaBuilder.asc(root.get(AuditLogEntry_.time)));
    TypedQuery<AuditLogEntry> query = entityManager.createQuery(criteria);

    return query.getResultList();
  }

  /**
   * Lists audit log entries by metaform
   *
   * @param metaform metaform
   * @return list of audit log entries
   */
  public List<AuditLogEntry> listByMetaform(Metaform metaform) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<AuditLogEntry> criteria = criteriaBuilder.createQuery(AuditLogEntry.class);
    Root<AuditLogEntry> root = criteria.from(AuditLogEntry.class);

    criteria.select(root);
    criteria.where(criteriaBuilder.equal(root.get(AuditLogEntry_.metaform), metaform));
    TypedQuery<AuditLogEntry> query = entityManager.createQuery(criteria);

    return query.getResultList();
  }
}
