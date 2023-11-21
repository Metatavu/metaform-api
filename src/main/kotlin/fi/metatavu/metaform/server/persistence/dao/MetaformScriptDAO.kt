package fi.metatavu.metaform.server.persistence.dao

import fi.metatavu.metaform.server.persistence.model.*
import java.util.*
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery

/**
 * DAO class for MetaformScript-entity
 */
@ApplicationScoped
class MetaformScriptDAO: AbstractDAO<MetaformScript>() {
  /**
   * Creates a link between a script and a metaform
   *
   * @param id id
   * @param metaform metaform
   * @param script script
   * @param creatorId creator id
   */
  fun createMetaformScript(
    id: UUID,
    metaform: Metaform,
    script: Script,
    creatorId: UUID
  ): MetaformScript {
    val metaformScript = MetaformScript()
    metaformScript.id = id
    metaformScript.metaform = metaform
    metaformScript.script = script
    metaformScript.creatorId = creatorId
    metaformScript.lastModifierId = creatorId
    return persist(metaformScript)
  }

  /**
   * Lists MetaformScripts by Metaform
   *
   * @param metaform Metaform
   */
  fun listByMetaform(
    metaform: Metaform
  ): List<MetaformScript> {
    val criteriaBuilder: CriteriaBuilder = entityManager.criteriaBuilder
    val criteria: CriteriaQuery<MetaformScript> = criteriaBuilder.createQuery(
      MetaformScript::class.java
    )
    val root = criteria.from(
      MetaformScript::class.java
    )
    criteria.select(root)
    criteria.where(
      criteriaBuilder.equal(root.get(MetaformScript_.metaform), metaform)
    )
    return entityManager.createQuery(criteria).resultList
  }

  /**
   * Lists MetaformScripts by Script
   *
   * @param script script
   */
  fun listByScript(
    script: Script
  ): List<MetaformScript> {
    val criteriaBuilder: CriteriaBuilder = entityManager.criteriaBuilder
    val criteria: CriteriaQuery<MetaformScript> = criteriaBuilder.createQuery(
      MetaformScript::class.java
    )
    val root = criteria.from(
      MetaformScript::class.java
    )
    criteria.select(root)
    criteria.where(
      criteriaBuilder.equal(root.get(MetaformScript_.script), script)
    )
    return entityManager.createQuery(criteria).resultList
  }
}