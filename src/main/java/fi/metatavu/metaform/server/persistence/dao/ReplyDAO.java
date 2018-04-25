package fi.metatavu.metaform.server.persistence.dao;

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
   * Finds reply by Metaform and user id
   * 
   * @param metaform Metaform
   * @param userId userId
   * @return reply
   */
  public Reply findByMetaformAndUserId(Metaform metaform, UUID userId) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Reply> criteria = criteriaBuilder.createQuery(Reply.class);
    Root<Reply> root = criteria.from(Reply.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.and(
        criteriaBuilder.equal(root.get(Reply_.metaform), metaform),
        criteriaBuilder.equal(root.get(Reply_.userId), userId)          
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
   * @param metaform Metaform
   * @param userId userId
   * @return
   */
  public List<Reply> list(Metaform metaform, UUID userId) {
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

    criteria.select(root);    
    criteria.where(criteriaBuilder.and(restrictions.toArray(new Predicate[0])));
    
    return entityManager.createQuery(criteria).getResultList();
  }
  
}
