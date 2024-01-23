package fi.metatavu.metaform.server.persistence.dao

import fi.metatavu.metaform.server.persistence.model.ExportTheme
import fi.metatavu.metaform.server.persistence.model.ExportTheme_
import java.util.*
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.TypedQuery
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery

/**
 * DAO class for ExportThemeFile entity
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class ExportThemeDAO : AbstractDAO<ExportTheme>() {
  /**
   * Creates new exportTheme
   *
   * @param id id
   * @param locales locales
   * @param parent parent
   * @param name name
   * @param creator creator id
   * @param lastModifier modifier
   * @return created exportTheme
   */
  fun create(
    id: UUID?,
    locales: String?,
    parent: ExportTheme?,
    name: String,
    creator: UUID,
    lastModifier: UUID
  ): ExportTheme {
    val exportTheme = ExportTheme()
    exportTheme.id = id
    exportTheme.locales = locales
    exportTheme.parent = parent
    exportTheme.name = name
    exportTheme.lastModifier = (lastModifier)
    exportTheme.creator = (creator)
    return persist(exportTheme)
  }

  /**
   * Finds a theme by name
   *
   * @param name name
   * @return Found theme or null if not found
   */
  fun findByName(name: String): ExportTheme? {
    val criteriaBuilder: CriteriaBuilder = entityManager.criteriaBuilder
    val criteria: CriteriaQuery<ExportTheme> = criteriaBuilder.createQuery(
      ExportTheme::class.java
    )
    val root = criteria.from(
      ExportTheme::class.java
    )
    criteria.select(root)
    criteria.where(criteriaBuilder.equal(root.get(ExportTheme_.name), name))
    val query: TypedQuery<ExportTheme> = entityManager.createQuery(criteria)
    return getSingleResult(query)
  }

  /**
   * Lists themes
   *
   * @return List of themes
   */
  fun list(): List<ExportTheme> {
    val criteriaBuilder: CriteriaBuilder = entityManager.criteriaBuilder
    val criteria: CriteriaQuery<ExportTheme> = criteriaBuilder.createQuery<ExportTheme>(
      ExportTheme::class.java
    )
    val root = criteria.from(
      ExportTheme::class.java
    )
    criteria.select(root)
    criteria.orderBy(criteriaBuilder.asc(root.get(ExportTheme_.createdAt)))
    return entityManager.createQuery(criteria).resultList
  }

  /**
   * Updates locales
   *
   * @param locales locales
   * @param lastModifier modifier
   * @return updated exportTheme
   */
  fun updateLocales(
    exportTheme: ExportTheme,
    locales: String?,
    lastModifier: UUID,
  ): ExportTheme? {
    exportTheme.lastModifier = lastModifier
    exportTheme.locales = locales
    return persist(exportTheme)
  }

  /**
   * Updates parent
   *
   * @param parent parent
   * @param lastModifier modifier
   * @return updated exportTheme
   */
  fun updateParent(
    exportTheme: ExportTheme,
    parent: ExportTheme?,
    lastModifier: UUID,
  ): ExportTheme? {
    exportTheme.lastModifier = lastModifier
    exportTheme.parent = parent
    return persist(exportTheme)
  }

  /**
   * Updates name
   *
   * @param name name
   * @param lastModifier modifier
   * @return updated exportTheme
   */
  fun updateName(exportTheme: ExportTheme, name: String, lastModifier: UUID): ExportTheme? {
    exportTheme.lastModifier = lastModifier
    exportTheme.name = name
    return persist(exportTheme)
  }
}