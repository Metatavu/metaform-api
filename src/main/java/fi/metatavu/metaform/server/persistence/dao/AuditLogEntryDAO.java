package fi.metatavu.metaform.server.persistence.dao;

import fi.metatavu.metaform.client.model.AuditLogEntryType;
import fi.metatavu.metaform.server.persistence.model.*;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.time.OffsetDateTime;
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
     * @param id
     * @param userId
     * @param dateTime
     * @param replyId
     * @param attachmentId
     * @param message
     * @return
     */
    public AuditLogEntry create(UUID id, UUID userId, OffsetDateTime dateTime, AuditLogEntryType type, UUID replyId,
                                UUID attachmentId, String message){
        AuditLogEntry auditLogEntry = new AuditLogEntry();
        auditLogEntry.setId(id);
        auditLogEntry.setUserId(userId);
        auditLogEntry.setTime(dateTime);
        auditLogEntry.setLogEntryType(type);
        auditLogEntry.setReplyId(replyId);
        auditLogEntry.setAttachmentId(attachmentId);
        auditLogEntry.setMessage(message);
        return persist(auditLogEntry);
    }

    /**
     * Select all audit log entries by replies
     * @param replies
     * @return
     */
    public List<AuditLogEntry> listAuditLogEntriesByReplyId(List<Reply> replies){
        if (replies.isEmpty())
            return Collections.emptyList();
        EntityManager entityManager = getEntityManager();

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<AuditLogEntry> criteria = criteriaBuilder.createQuery(AuditLogEntry.class);
        Root<AuditLogEntry> root = criteria.from(AuditLogEntry.class);
        criteria.select(root);
        for (Reply reply : replies)
            criteria.where(criteriaBuilder.equal(root.get(AuditLogEntry_.replyId), reply.getId()));
        criteria.orderBy(criteriaBuilder.asc(root.get(AuditLogEntry_.time)));
        TypedQuery<AuditLogEntry> query = entityManager.createQuery(criteria);

        return query.getResultList();
    }


}
