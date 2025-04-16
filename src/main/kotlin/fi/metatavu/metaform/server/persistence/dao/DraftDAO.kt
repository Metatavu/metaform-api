package fi.metatavu.metaform.server.persistence.dao

import fi.metatavu.metaform.server.persistence.model.Draft
import fi.metatavu.metaform.server.persistence.model.Metaform
import fi.metatavu.metaform.server.persistence.model.*
import java.util.*
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.TypedQuery
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery

/**
 * DAO class for draft entity
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class DraftDAO : AbstractDAO<Draft>() {

  /**
   * Creates new draft
   *
   * @param id id
   * @param userId user id
   * @param metaform Metaform
   * @param data data
   * @return created Metaform
   */
  fun create(
    id: UUID,
    userId: UUID,
    metaform: Metaform,
    data: String
  ): Draft {
    val draft = Draft()
    draft.id = id
    draft.metaform = metaform
    draft.userId = userId
    draft.data = data
    return persist(draft)
  }

  /**
   * Updates data
   *
   * @param draft draft
   * @param data data
   * @return updated draft
   */
  fun updateData(draft: Draft, data: String?): Draft {
    draft.data = data
    return persist(draft)
  }

  /**
   * Lists drafts by Metaform
   *
   * @param metaform Metaform
   * @param firstResult first result
   * @param maxResults max results
   * @return list of drafts
   */
  fun listByMetaform(metaform: Metaform, firstResult: Int?, maxResults: Int?): List<Draft> {
    val criteriaBuilder: CriteriaBuilder = entityManager.criteriaBuilder
    val criteria: CriteriaQuery<Draft> = criteriaBuilder.createQuery(
      Draft::class.java
    )
    val root = criteria.from(
      Draft::class.java
    )
    criteria.select(root)
    criteria.where(criteriaBuilder.equal(root.get(Draft_.metaform), metaform))
    val query: TypedQuery<Draft> = entityManager.createQuery(criteria)

    if (firstResult != null) {
      query.firstResult = firstResult
    }

    if (maxResults != null) {
      query.maxResults = maxResults
    }

    return query.resultList
  }

}