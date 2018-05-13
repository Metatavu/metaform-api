package fi.metatavu.metaform.server.persistence.dao;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import fi.metatavu.metaform.server.persistence.model.Metaform;
import fi.metatavu.metaform.server.persistence.model.Reply;
import fi.metatavu.metaform.server.persistence.model.Reply_;

/**
 * DAO class for Reply entity
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class ReplyDAO extends AbstractDAO<Reply> {
  
  /**
   * Creates new reply
   * 
   * @param id id
   * @param userId user id
   * @param metaform Metaform
   * @return created Metaform
   */
  public Reply create(UUID id, UUID userId, Metaform metaform) {
    Reply reply = new Reply(); 
    reply.setId(id);
    reply.setMetaform(metaform);
    reply.setUserId(userId);
    return persist(reply);
  }
  
  /**
   * Finds reply by Metaform, user id and null revision.
   * 
   * @param metaform Metaform
   * @param userId userId
   * @return reply
   */
  public Reply findByMetaformAndUserIdAndRevisionNull(Metaform metaform, UUID userId) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Reply> criteria = criteriaBuilder.createQuery(Reply.class);
    Root<Reply> root = criteria.from(Reply.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.and(
        criteriaBuilder.equal(root.get(Reply_.metaform), metaform),
        criteriaBuilder.equal(root.get(Reply_.userId), userId),
        criteriaBuilder.isNull(root.get(Reply_.revision))
      ) 
    );
    
    return getSingleResult(entityManager.createQuery(criteria));
  }

  /**
   * Lists replies by Metaform
   * 
   * @param metaform Metaform
   * @return list of replies
   */
  public List<Reply> listByMetaform(Metaform metaform) {
    EntityManager entityManager = getEntityManager();
    
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Reply> criteria = criteriaBuilder.createQuery(Reply.class);
    Root<Reply> root = criteria.from(Reply.class);
    criteria.select(root);
    criteria.where(criteriaBuilder.equal(root.get(Reply_.metaform), metaform));
    
    TypedQuery<Reply> query = entityManager.createQuery(criteria);
    
    return query.getResultList();
  }

  /**
   * List replies by multiple filters.
   * 
   * All parameters can be nulled. Nulled parameters will be ignored.
   * 
   * @param metaform Metaform
   * @param userId userId
   * @param revisionNull true to include only null replies with null revision, false to only non null revisions.
   * @param createdBefore filter results by created before specified time.
   * @param createdAfter filter results by created after specified time.
   * @param modifiedBefore filter results by modified before specified time.
   * @param modifiedAfter filter results by modified after specified time.
   * @return replies list of replies
   */
  public List<Reply> list(Metaform metaform, UUID userId, boolean includeRevisions, OffsetDateTime createdBefore, OffsetDateTime createdAfter, OffsetDateTime modifiedBefore, OffsetDateTime modifiedAfter) {
    EntityManager entityManager = getEntityManager();
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Reply> criteria = criteriaBuilder.createQuery(Reply.class);
    Root<Reply> root = criteria.from(Reply.class);

    List<Predicate> restrictions = new ArrayList<>();
    
    if (metaform != null) {
      restrictions.add(criteriaBuilder.equal(root.get(Reply_.metaform), metaform));
    }
    
    if (userId != null) {
      restrictions.add(criteriaBuilder.equal(root.get(Reply_.userId), userId));
    }
    
    if (!includeRevisions) {
      restrictions.add(criteriaBuilder.isNull(root.get(Reply_.revision)));
    }
    
    if (createdBefore != null) {
      restrictions.add(criteriaBuilder.lessThanOrEqualTo(root.get(Reply_.createdAt), createdBefore));
    }
  
    if (createdAfter != null) {
      restrictions.add(criteriaBuilder.greaterThanOrEqualTo(root.get(Reply_.createdAt), createdAfter));
    }
    
    if (modifiedBefore != null) {
      restrictions.add(criteriaBuilder.lessThanOrEqualTo(root.get(Reply_.modifiedAt), modifiedBefore));
    }
  
    if (modifiedAfter != null) {
      restrictions.add(criteriaBuilder.greaterThanOrEqualTo(root.get(Reply_.modifiedAt), modifiedAfter));
    }

    criteria.select(root);    
    criteria.where(criteriaBuilder.and(restrictions.toArray(new Predicate[0])));
    
    return entityManager.createQuery(criteria).getResultList();
  }

  /**
   * Updates reply revision field
   * 
   * @param reply reply
   * @param revision revision time
   * @return updated reply
   */
  public Reply updateRevision(Reply reply, OffsetDateTime revision) {
    reply.setRevision(revision);
    return persist(reply);
  }
  
}
