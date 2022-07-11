package fi.metatavu.metaform.server.persistence.dao

import fi.metatavu.metaform.server.persistence.model.ExportTheme
import fi.metatavu.metaform.server.persistence.model.ExportThemeFile
import fi.metatavu.metaform.server.persistence.model.ExportThemeFile_
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.persistence.EntityManager
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery

/**
 * DAO class for ExportThemeFile entity
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class ExportThemeFileDAO : AbstractDAO<ExportThemeFile>() {
  /**
   * Creates new exportThemeFile
   *
   * @param id id
   * @param theme theme
   * @param path path
   * @param content content
   * @param creator creator id
   * @return created exportThemeFile
   */
  fun create(
    id: UUID?,
    theme: ExportTheme,
    path: String,
    content: String,
    creator: UUID,
  ): ExportThemeFile {
    val exportThemeFile = ExportThemeFile()
    exportThemeFile.id = id
    exportThemeFile.path = path
    exportThemeFile.content = content
    exportThemeFile.theme = theme
    exportThemeFile.lastModifier = creator
    exportThemeFile.creator = creator
    return persist(exportThemeFile)
  }

  /**
   * Finds a theme file by theme and path
   *
   * @param theme theme
   * @param path path
   * @return Found theme file or null if not found
   */
  fun findByThemeAndPath(theme: ExportTheme, path: String): ExportThemeFile? {
    val criteriaBuilder: CriteriaBuilder = entityManager.criteriaBuilder
    val criteria: CriteriaQuery<ExportThemeFile> = criteriaBuilder.createQuery<ExportThemeFile>(
      ExportThemeFile::class.java
    )
    val root = criteria.from(
      ExportThemeFile::class.java
    )
    criteria.select(root)
    criteria.where(
      criteriaBuilder.and(
        criteriaBuilder.equal(root.get(ExportThemeFile_.theme), theme),
        criteriaBuilder.equal(root.get(ExportThemeFile_.path), path)
      )
    )
    val query: TypedQuery<ExportThemeFile> = entityManager.createQuery(criteria)
    return getSingleResult(query)
  }

  /**
   * Lists theme files by theme
   *
   * @param theme theme
   * @return List of theme files
   */
  fun listByTheme(theme: ExportTheme): List<ExportThemeFile> {
    val criteriaBuilder: CriteriaBuilder = entityManager.criteriaBuilder
    val criteria: CriteriaQuery<ExportThemeFile> = criteriaBuilder.createQuery(
      ExportThemeFile::class.java
    )
    val root = criteria.from(
      ExportThemeFile::class.java
    )
    criteria.select(root)
    criteria.where(criteriaBuilder.equal(root.get(ExportThemeFile_.theme), theme))
    return entityManager.createQuery<ExportThemeFile>(criteria).getResultList()
  }

  /**
   * Updates path
   *
   * @param path path
   * @param lastModifier modifier
   * @return updated exportThemeFile
   */
  fun updatePath(
    exportThemeFile: ExportThemeFile,
    path: String,
    lastModifier: UUID
  ): ExportThemeFile? {
    exportThemeFile.lastModifier = lastModifier
    exportThemeFile.path = path
    return persist(exportThemeFile)
  }

  /**
   * Updates content
   *
   * @param content content
   * @param lastModifier modifier
   * @return updated exportThemeFile
   */
  fun updateContent(
    exportThemeFile: ExportThemeFile,
    content: String,
    lastModifier: UUID
  ): ExportThemeFile? {
    exportThemeFile.lastModifier = lastModifier
    exportThemeFile.content = content
    return persist(exportThemeFile)
  }

  /**
   * Updates theme
   *
   * @param theme theme
   * @param lastModifier modifier
   * @return updated exportThemeFile
   */
  fun updateTheme(
    exportThemeFile: ExportThemeFile,
    theme: ExportTheme,
    lastModifier: UUID
  ): ExportThemeFile? {
    exportThemeFile.lastModifier = lastModifier
    exportThemeFile.theme = theme
    return persist(exportThemeFile)
  }
}