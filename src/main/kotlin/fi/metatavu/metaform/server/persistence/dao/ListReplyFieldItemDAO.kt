package fi.metatavu.metaform.server.persistence.dao

import fi.metatavu.metaform.server.persistence.model.ListReplyField
import fi.metatavu.metaform.server.persistence.model.ListReplyFieldItem
import fi.metatavu.metaform.server.persistence.model.ListReplyFieldItem_
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.EntityManager
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Root

/**
 * DAO class for ListReplyFieldItem entity
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class ListReplyFieldItemDAO : AbstractDAO<ListReplyFieldItem>() {
  /**
   * Creates new list reply field item
   *
   * @param id id
   * @param field field
   * @param value value
   * @return created ListReplyFieldItem
   */
  fun create(id: UUID, field: ListReplyField, value: String): ListReplyFieldItem {
    val listReplyFieldItem = ListReplyFieldItem()
    listReplyFieldItem.id = id
    listReplyFieldItem.field = field
    listReplyFieldItem.value = value
    return persist(listReplyFieldItem)
  }

  /**
   * Lists reply field items by field
   *
   * @param field list reply field
   * @return list of items
   */
  fun listByField(field: ListReplyField): List<ListReplyFieldItem> {
    val entityManager: EntityManager = getEntityManager()
    val criteriaBuilder: CriteriaBuilder = entityManager.getCriteriaBuilder()
    val criteria: CriteriaQuery<ListReplyFieldItem> =
      criteriaBuilder.createQuery(
        ListReplyFieldItem::class.java
      )
    val root: Root<ListReplyFieldItem> = criteria.from(
      ListReplyFieldItem::class.java
    )
    criteria.select(root)
    criteria.where(criteriaBuilder.equal(root.get(ListReplyFieldItem_.field), field))
    return entityManager.createQuery(criteria).resultList
  }

  /**
   * Lists item values by field
   *
   * @param field field
   * @return values
   */
  fun listItemValuesByField(field: ListReplyField): List<String> {
    val entityManager: EntityManager = getEntityManager()
    val criteriaBuilder: CriteriaBuilder = entityManager.criteriaBuilder
    val criteria: CriteriaQuery<String> = criteriaBuilder.createQuery(
      String::class.java
    )
    val root: Root<ListReplyFieldItem> = criteria.from(
      ListReplyFieldItem::class.java
    )
    criteria.select(root.get(ListReplyFieldItem_.value))
    criteria.where(criteriaBuilder.equal(root.get(ListReplyFieldItem_.field), field))
    return entityManager.createQuery(criteria).resultList
  }
}