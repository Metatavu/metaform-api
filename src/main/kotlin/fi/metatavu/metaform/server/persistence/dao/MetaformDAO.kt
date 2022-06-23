package fi.metatavu.metaform.server.persistence.dao

import fi.metatavu.metaform.server.persistence.model.ExportTheme
import fi.metatavu.metaform.server.persistence.model.Metaform
import fi.metatavu.metaform.server.persistence.model.Metaform_
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.EntityManager
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery

/**
 * DAO class for Metaform entity
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class MetaformDAO : AbstractDAO<Metaform>() {

  /**
   * Creates new Metaform
   *
   * @param id id
   * @param slug form slug
   * @param exportTheme export theme
   * @param allowAnonymous whether to allow anonymous repliers
   * @param data form JSON
   * @return created Metaform
   */
  fun create(
    id: UUID,
    slug: String,
    exportTheme: ExportTheme?,
    allowAnonymous: Boolean?,
    data: String
  ): Metaform {
    val metaform = Metaform()
    metaform.id = id
    metaform.exportTheme = exportTheme
    metaform.data = data
    metaform.slug = slug
    metaform.allowAnonymous = allowAnonymous
    return persist(metaform)
  }

  /**
   * Finds Metaform by slug
   *
   * @param slug slug
   * @return found Metaform or null if non found
   */
  fun findBySlug(slug: String): Metaform? {
    val entityManager: EntityManager = getEntityManager()
    val criteriaBuilder: CriteriaBuilder = entityManager.criteriaBuilder
    val criteria: CriteriaQuery<Metaform> = criteriaBuilder.createQuery<Metaform>(
      Metaform::class.java
    )
    val root = criteria.from(
      Metaform::class.java
    )
    criteria.select(root)
    criteria.where(criteriaBuilder.equal(root.get(Metaform_.slug), slug))
    return getSingleResult<Metaform>(entityManager.createQuery(criteria))
  }

  /**
   * Updates JSON data
   *
   * @param metaform Metaform
   * @param data form JSON
   * @return Updated Metaform
   */
  fun updateData(metaform: Metaform, data: String): Metaform {
    metaform.data = data
    return persist(metaform)
  }

  /**
   * Updates allowAnonymous value
   *
   * @param metaform Metaform
   * @param allowAnonymous allow anonymous
   * @return Updated Metaform
   */
  fun updateAllowAnonymous(metaform: Metaform, allowAnonymous: Boolean?): Metaform {
    metaform.allowAnonymous = allowAnonymous
    return persist(metaform)
  }

  /**
   * Updates exportTheme value
   *
   * @param metaform Metaform
   * @param exportTheme export theme
   * @return Updated Metaform
   */
  fun updateExportTheme(metaform: Metaform, exportTheme: ExportTheme?): Metaform {
    metaform.exportTheme = exportTheme
    return persist(metaform)
  }

  /**
   * Updates slug value
   *
   * @param metaform Metaform
   * @param slug slug
   * @return Updated Metaform
   */
  fun updateSlug(metaform: Metaform, slug: String): Metaform {
    metaform.slug = slug
    return persist(metaform)
  }
}