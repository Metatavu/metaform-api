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
	 * Create AuditLogEntry
	 * @param id uuid of log entry
	 * @param userId userId
	 * @param time time
	 * @param replyId replyId
	 * @param attachmentId attachmentId
	 * @param message message
	 * @return created AuditLogEntry
	 */
	public AuditLogEntry create(UUID id, UUID userId, OffsetDateTime time, AuditLogEntryType auditLogEntryType, UUID replyId,
															UUID attachmentId, String message){
		AuditLogEntry auditLogEntry = new AuditLogEntry();
		auditLogEntry.setId(id);
		auditLogEntry.setUserId(userId);
    auditLogEntry.setTime(time);
    auditLogEntry.setLogEntryType(auditLogEntryType);
    auditLogEntry.setReplyId(replyId);
    auditLogEntry.setAttachmentId(attachmentId);
    auditLogEntry.setMessage(message);
    return persist(auditLogEntry);
	}

    /**
     * get audit log entries by replies, user id, created before and after parameters
     * @param replies list of replies
     * @param userId userId
     * @param createdBefore created before
     * @param createdAfter created after
     * @return list of AuditLogEntry
     */
    public List<AuditLogEntry> listAuditLogEntries(List<UUID> replies, UUID userId, OffsetDateTime createdBefore, OffsetDateTime createdAfter) {
			EntityManager entityManager = getEntityManager();

			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
			CriteriaQuery<AuditLogEntry> criteria = criteriaBuilder.createQuery(AuditLogEntry.class);
			Root<AuditLogEntry> root = criteria.from(AuditLogEntry.class);

			List<Predicate> restrictions = new ArrayList<>();

			Predicate predicateReplyId = root.get(AuditLogEntry_.replyId).in(replies);
			restrictions.add(predicateReplyId);
			if (userId != null)
				restrictions.add(criteriaBuilder.equal(root.get(AuditLogEntry_.userId), userId));
			if (createdBefore != null)
				restrictions.add(criteriaBuilder.lessThanOrEqualTo(root.get(AuditLogEntry_.time), createdBefore));
			if (createdAfter != null)
				restrictions.add(criteriaBuilder.greaterThanOrEqualTo(root.get(AuditLogEntry_.time), createdAfter));

			criteria.select(root);
			criteria.where(criteriaBuilder.and(restrictions.toArray(new Predicate[0])));
			criteria.orderBy(criteriaBuilder.asc(root.get(AuditLogEntry_.time)));
			TypedQuery<AuditLogEntry> query = entityManager.createQuery(criteria);
			return query.getResultList();
    }
}
