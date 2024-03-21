package fi.metatavu.metaform.server.persistence.dao

import fi.metatavu.metaform.api.spec.model.MetaformVisibility
import fi.metatavu.metaform.server.persistence.model.ExportTheme
import fi.metatavu.metaform.server.persistence.model.Metaform
import fi.metatavu.metaform.server.persistence.model.Metaform_
import java.time.OffsetDateTime
import java.util.*
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery

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
   * @param creatorId creator id
   * @return created Metaform
   */
  fun create(
    id: UUID,
    slug: String,
    exportTheme: ExportTheme?,
    visibility: MetaformVisibility,
    allowAnonymous: Boolean?,
    data: String,
    active: Boolean,
    creatorId: UUID
  ): Metaform {
    val metaform = Metaform()
    metaform.id = id
    metaform.exportTheme = exportTheme
    metaform.visibility = visibility
    metaform.data = data
    metaform.slug = slug
    metaform.allowAnonymous = allowAnonymous
    metaform.active = active
    metaform.creatorId = creatorId
    metaform.lastModifierId = creatorId
    return persist(metaform)
  }

  /**
   * Finds Metaform by slug
   *
   * @param slug slug
   * @return found Metaform or null if non found
   */
  fun findBySlug(slug: String): Metaform? {
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
   * @param lastModifierId last modifier id
   * @return Updated Metaform
   */
  fun updateData(metaform: Metaform, data: String, lastModifierId: UUID): Metaform {
    metaform.data = data
    metaform.lastModifierId = lastModifierId
    return persist(metaform)
  }

  /**
   * Updates allowAnonymous value
   *
   * @param metaform Metaform
   * @param allowAnonymous allow anonymous
   * @param lastModifierId last modifier id
   * @return Updated Metaform
   */
  fun updateAllowAnonymous(metaform: Metaform, allowAnonymous: Boolean?, lastModifierId: UUID): Metaform {
    metaform.allowAnonymous = allowAnonymous
    metaform.lastModifierId = lastModifierId
    return persist(metaform)
  }

  /**
   * Updates exportTheme value
   *
   * @param metaform Metaform
   * @param exportTheme export theme
   * @param lastModifierId last modifier id
   * @return Updated Metaform
   */
  fun updateExportTheme(metaform: Metaform, exportTheme: ExportTheme?, lastModifierId: UUID): Metaform {
    metaform.exportTheme = exportTheme
    metaform.lastModifierId = lastModifierId
    return persist(metaform)
  }

  /**
   * Updates visibility
   *
   * @param metaform Metaform
   * @param visibility visibility
   * @param lastModifierId last modifier id
   * @return Updated Metaform
   */
  fun updateVisibility(metaform: Metaform, visibility: MetaformVisibility, lastModifierId: UUID): Metaform {
    metaform.visibility = visibility
    metaform.lastModifierId = lastModifierId
    return persist(metaform)
  }

  /**
   * Updates slug value
   *
   * @param metaform Metaform
   * @param slug slug
   * @param lastModifierId last modifier id
   * @return Updated Metaform
   */
  fun updateSlug(metaform: Metaform, slug: String, lastModifierId: UUID): Metaform {
    metaform.slug = slug
    metaform.lastModifierId = lastModifierId
    return persist(metaform)
  }

  /**
   * Lists metaform by visibility
   *
   * @param visibility visibility
   * @return list of Metaforms
   */
  fun listByVisibility(visibility: MetaformVisibility): List<Metaform> {
    val criteriaBuilder = entityManager.criteriaBuilder
    val criteria = criteriaBuilder.createQuery(
      Metaform::class.java
    )
    val root = criteria.from(
      Metaform::class.java
    )
    criteria.select(root)
    criteria.where(
      criteriaBuilder.equal(root.get(Metaform_.visibility), visibility)
    )
    return entityManager.createQuery(criteria).resultList
  }

  fun listByActive(active: Boolean): List<Metaform> {
    val criteriaBuilder = entityManager.criteriaBuilder
    val criteria = criteriaBuilder.createQuery(
      Metaform::class.java
    )
    val root = criteria.from(
      Metaform::class.java
    )
    criteria.select(root)
    criteria.where(
      criteriaBuilder.equal(root.get(Metaform_.active), active)
    )
    return entityManager.createQuery(criteria).resultList
  }
}