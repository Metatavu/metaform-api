package fi.metatavu.metaform.persistence.dao

import fi.metatavu.metaform.persistence.model.Reply
import fi.metatavu.metaform.persistence.model.ReplyField
import fi.metatavu.metaform.persistence.model.ReplyField_

/**
 * Abstract base class for ReplyField DAOs
 *
 * @author Antti Leppä
 */
abstract class ReplyFieldDAO<T : ReplyField?> : AbstractDAO<T>() {

  /**
   * Finds a reply field by reply by name
   *
   * @param reply a reply
   * @param name  a name
   * @return reply field or null if field is not found
   */
  fun findByReplyAndName(reply: Reply, name: String): ReplyField? {
    val entityManager = getEntityManager()
    val criteriaBuilder = entityManager.criteriaBuilder
    val criteria = criteriaBuilder.createQuery(
      ReplyField::class.java
    )
    val root = criteria.from(
      ReplyField::class.java
    )
    criteria.select(root)
    criteria.where(
      criteriaBuilder.and(
        criteriaBuilder.equal(root.get(ReplyField_.reply), reply),
        criteriaBuilder.equal(root.get(ReplyField_.name), name)
      )
    )
    return getSingleResult(entityManager.createQuery(criteria))
  }

  /**
   * List reply fields
   *
   * @param reply reply
   * @return list of fields
   */
  fun listByReply(reply: Reply): List<ReplyField> {
    val entityManager = getEntityManager()
    val criteriaBuilder = entityManager.criteriaBuilder
    val criteria = criteriaBuilder.createQuery(
      ReplyField::class.java
    )
    val root = criteria.from(
      ReplyField::class.java
    )
    criteria.select(root)
    criteria.where(
      criteriaBuilder.equal(root.get(ReplyField_.reply), reply)
    )
    return entityManager.createQuery(criteria).resultList
  }

  /**
   * Lists all field names by reply
   *
   * @param reply reply
   * @return field names
   */
  fun listNamesByReply(reply: Reply): List<String> {
    val entityManager = getEntityManager()
    val criteriaBuilder = entityManager.criteriaBuilder
    val criteria = criteriaBuilder.createQuery(
      String::class.java
    )
    val root = criteria.from(
      ReplyField::class.java
    )
    criteria.select(root.get(ReplyField_.name))
    criteria.where(
      criteriaBuilder.equal(root.get(ReplyField_.reply), reply)
    )
    return entityManager.createQuery(criteria).resultList
  }
}