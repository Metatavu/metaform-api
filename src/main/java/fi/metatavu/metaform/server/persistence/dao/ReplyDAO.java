package fi.metatavu.metaform.server.persistence.dao;

import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
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
  
}
