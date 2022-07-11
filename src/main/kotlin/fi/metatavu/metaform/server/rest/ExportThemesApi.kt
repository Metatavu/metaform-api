package fi.metatavu.metaform.server.rest

import fi.metatavu.metaform.api.spec.model.ExportTheme
import fi.metatavu.metaform.server.controllers.ExportThemeController
import fi.metatavu.metaform.server.rest.translate.ExportThemeTranslator
import java.util.*
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.core.Response

@RequestScoped
@Transactional
class ExportThemesApi: fi.metatavu.metaform.api.spec.ExportThemesApi, AbstractApi() {

  @Inject
  lateinit var exportThemeController: ExportThemeController

  @Inject
  lateinit var exportThemeTranslator: ExportThemeTranslator

  /**
   * Creates a new export theme
   * 
   * @param exportTheme export theme to create
   * @return created export theme
   */
  override suspend fun createExportTheme(exportTheme: ExportTheme): Response {
    val userId = loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isRealmMetaformSuper) {
      return createForbidden(createNotAllowedMessage(CREATE, EXPORT_THEME))
    }

    val parent = if (exportTheme.parentId != null) {
      exportThemeController.findExportTheme(exportTheme.parentId)
              ?: return createBadRequest(createNotFoundMessage(EXPORT_THEME, exportTheme.parentId))
    } else null

    val createdExportTheme = exportThemeController.createExportTheme(
            exportTheme.locales,
            parent,
            exportTheme.name,
            userId
    )

    return createOk(exportThemeTranslator.translateExportTheme(createdExportTheme))
  }

  /**
   * Deletes an export theme
   * 
   * @param exportThemeId export theme id
   * @return deleted export theme
   */
  override suspend fun deleteExportTheme(exportThemeId: UUID): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isRealmMetaformSuper) {
      return createForbidden(createNotAllowedMessage(UPDATE, EXPORT_THEME))
    }

    val theme = exportThemeController.findExportTheme(exportThemeId)
            ?: return createNotFound(createNotFoundMessage(EXPORT_THEME, exportThemeId))

    exportThemeController.deleteTheme(theme)

    return createNoContent()
  }

  /**
   * Finds export theme by id
   * 
   * @param exportThemeId export theme id
   * @return export theme
   */
  override suspend fun findExportTheme(exportThemeId: UUID): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isRealmMetaformSuper) {
      return createForbidden(createNotAllowedMessage(UPDATE, EXPORT_THEME))
    }

    val exportTheme = exportThemeController.findExportTheme(exportThemeId)
            ?: return createNotFound(createNotFoundMessage(EXPORT_THEME, exportThemeId))

    return createOk(exportThemeTranslator.translateExportTheme(exportTheme))
  }

  /**
   * Lists export themes
   */
  override suspend fun listExportThemes(): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    return if (!isRealmMetaformSuper) {
      createForbidden(createNotAllowedMessage(LIST, EXPORT_THEME))
    } else createOk(exportThemeController.listExportThemes()
            .map(exportThemeTranslator::translateExportTheme))

  }

  /**
   * Updates an export theme
   * 
   * @param exportThemeId export theme id
   * @param exportTheme export theme data
   */
  override suspend fun updateExportTheme(exportThemeId: UUID, exportTheme: ExportTheme): Response {
    val userId = loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isRealmMetaformSuper) {
      return createForbidden(createNotAllowedMessage(UPDATE, EXPORT_THEME))
    }

    val theme = exportThemeController.findExportTheme(exportThemeId)
            ?: return createNotFound(createNotFoundMessage(EXPORT_THEME, exportThemeId))

    val parent = if (exportTheme.parentId != null) {
      exportThemeController.findExportTheme(exportTheme.parentId)
              ?: return createBadRequest(createNotFoundMessage(EXPORT_THEME, exportTheme.parentId))
    } else null

    return createOk(exportThemeTranslator.translateExportTheme(
            exportThemeController.updateExportTheme(
              theme,
              exportTheme.locales,
              parent,
              exportTheme.name,
              userId
            )
      )
    )
  }
}