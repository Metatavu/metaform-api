package fi.metatavu.metaform.server.persistence.dao

import fi.metatavu.metaform.api.spec.model.MetaformVersionType
import fi.metatavu.metaform.server.persistence.model.Metaform
import fi.metatavu.metaform.server.persistence.model.MetaformVersion
import fi.metatavu.metaform.server.persistence.model.MetaformVersion_
import java.util.*
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery

/**
 * DAO class for Metaform version entities
 *
 * @author Tianxing Wu
 */
@ApplicationScoped
class MetaformVersionDAO : AbstractDAO<MetaformVersion>() {
  /**
   * Creates new Metaform version
   *
   * @param id id
   * @param metaform Metaform
   * @param type Metaform version type
   * @param data Metaform form JSON
   * @param creatorId creator id
   * @param lastModifierId last modifier id
   *
   * @return created Metaform version
   */
  fun create(
    id: UUID,
    metaform: Metaform,
    type: MetaformVersionType,
    data: String,
    creatorId: UUID,
    lastModifierId: UUID
  ): MetaformVersion {
    val metaformVersion = MetaformVersion()
    metaformVersion.id = id
    metaformVersion.metaform = metaform
    metaformVersion.type = type
    metaformVersion.data = data
    metaformVersion.creatorId = creatorId
    metaformVersion.lastModifierId = lastModifierId
    return persist(metaformVersion)
  }

  /**
   * Lists Metaform version by Metaform
   *
   * @param metaform Metaform
   * @param firstResult first result
   * @param maxResults max results
   *
   * @return Metaform versions
   */
  fun listByMetaform(
    metaform: Metaform,
    firstResult: Int?,
    maxResults: Int?
  ): List<MetaformVersion> {
    val criteriaBuilder: CriteriaBuilder = entityManager.criteriaBuilder
    val criteria: CriteriaQuery<MetaformVersion> = criteriaBuilder.createQuery(
      MetaformVersion::class.java
    )
    val root = criteria.from(
      MetaformVersion::class.java
    )
    criteria.select(root)
    criteria.where(
      criteriaBuilder.equal(root.get(MetaformVersion_.metaform), metaform)
    )

    val query = entityManager.createQuery(criteria)

    if (firstResult != null) {
      query.firstResult = firstResult
    }

    if (maxResults != null) {
      query.maxResults = maxResults
    }

    return query.resultList
  }

  /**
   * Updates Metaform version type
   *
   * @param metaformVersion Metaform Version
   * @param type Metaform Version type
   * @param lastModifierId last modifier id
   * @return Updated MetaformVersion
   */
  fun updateType(
    metaformVersion: MetaformVersion,
    type: MetaformVersionType,
    lastModifierId: UUID
  ): MetaformVersion {
    metaformVersion.type = type
    metaformVersion.lastModifierId = lastModifierId
    return persist(metaformVersion)
  }

  /**
   * Updates Metaform version data
   *
   * @param metaformVersion Metaform Version
   * @param data Metaform Version data
   * @param lastModifierId last modifier id
   * @return Updated MetaformVision
   */
  fun updateData(
    metaformVersion: MetaformVersion,
    data: String,
    lastModifierId: UUID
  ): MetaformVersion {
    metaformVersion.data = data
    metaformVersion.lastModifierId = lastModifierId
    return persist(metaformVersion)
  }
}