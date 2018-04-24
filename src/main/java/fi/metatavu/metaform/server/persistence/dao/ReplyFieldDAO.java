package fi.metatavu.metaform.server.persistence.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import fi.metatavu.metaform.server.persistence.model.Reply;
import fi.metatavu.metaform.server.persistence.model.ReplyField;
import fi.metatavu.metaform.server.persistence.model.ReplyField_;

/**
 * Abstract base class for ReplyField DAOs
 * 
 * @author Antti Leppä
 */
public abstract class ReplyFieldDAO <T extends ReplyField> extends AbstractDAO<T> {
  
  /**
   * Finds a reply field by reply by name
   * 
   * @param reply a reply
   * @param name a name
   * @return reply field or null if field is not found
   */
  public ReplyField findByReplyAndName(Reply reply, String name) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<ReplyField> criteria = criteriaBuilder.createQuery(ReplyField.class);
    Root<ReplyField> root = criteria.from(ReplyField.class);
    criteria.select(root);

    criteria.where(
      criteriaBuilder.and(
        criteriaBuilder.equal(root.get(ReplyField_.reply), reply),
        criteriaBuilder.equal(root.get(ReplyField_.name), name)
      )
    );
    
    return getSingleResult(entityManager.createQuery(criteria));
  }

  /**
   * List reply fields
   * 
   * @param reply reply
   * @return list of fields
   */
  public List<ReplyField> listByReply(Reply reply) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<ReplyField> criteria = criteriaBuilder.createQuery(ReplyField.class);
    Root<ReplyField> root = criteria.from(ReplyField.class);
    criteria.select(root);

    criteria.where(
      criteriaBuilder.equal(root.get(ReplyField_.reply), reply)
    );
    
    return entityManager.createQuery(criteria).getResultList();
  }

  /**
   * Lists all field names by reply
   * 
   * @param reply reply
   * @return field names
   */
  public List<String> listNamesByReply(Reply reply) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<String> criteria = criteriaBuilder.createQuery(String.class);
    Root<ReplyField> root = criteria.from(ReplyField.class);
    criteria.select(root.get(ReplyField_.name));

    criteria.where(
      criteriaBuilder.equal(root.get(ReplyField_.reply), reply)
    );
    
    return entityManager.createQuery(criteria).getResultList();
  }
  
}
