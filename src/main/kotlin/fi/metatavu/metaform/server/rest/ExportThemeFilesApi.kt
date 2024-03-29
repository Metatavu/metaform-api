package fi.metatavu.metaform.server.rest

import fi.metatavu.metaform.api.spec.model.ExportThemeFile
import fi.metatavu.metaform.server.controllers.ExportThemeController
import fi.metatavu.metaform.server.persistence.model.ExportTheme
import fi.metatavu.metaform.server.rest.translate.ExportThemeTranslator
import java.util.*
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.core.Response

@RequestScoped
@Transactional
class ExportThemeFilesApi: fi.metatavu.metaform.api.spec.ExportThemeFilesApi, AbstractApi() {

  @Inject
  lateinit var exportThemeController: ExportThemeController

  @Inject
  lateinit var exportThemeTranslator: ExportThemeTranslator

  override fun createExportThemeFile(
    exportThemeId: UUID,
    exportThemeFile: ExportThemeFile
  ): Response {
    val userId = loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isMetatavuAdmin && !isRealmSystemAdmin) {
      return createForbidden(createNotAllowedMessage(CREATE, EXPORT_THEME))
    }

    val theme: ExportTheme = exportThemeController.findExportTheme(exportThemeFile.themeId)
            ?: return createBadRequest(createNotFoundMessage(EXPORT_THEME, exportThemeFile.themeId))

    if (theme.id != exportThemeId) {
      return createNotFound(createNotBelongMessage(EXPORT_THEME))
    }

    val themeFile = exportThemeController.createExportThemeFile(
            theme, exportThemeFile.path, exportThemeFile.content, userId)

    return createOk(exportThemeTranslator.translateExportThemeFile(themeFile))

  }

  override fun deleteExportThemeFile(
    exportThemeId: UUID,
    exportThemeFileId: UUID
  ): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isMetatavuAdmin && !isRealmSystemAdmin) {
      return createForbidden(createNotAllowedMessage(DELETE, EXPORT_THEME))
    }

    val theme: ExportTheme = exportThemeController.findExportTheme(exportThemeId)
            ?: return createBadRequest(createNotFoundMessage(EXPORT_THEME, exportThemeId))

    val exportThemeFile = exportThemeController.findExportThemeFile(exportThemeFileId)
            ?: return createBadRequest(createNotFoundMessage(EXPORT_THEME_FILE, exportThemeFileId))

    if (theme.id != exportThemeId) {
      return createNotFound(createNotBelongMessage(EXPORT_THEME))
    }

    exportThemeController.deleteThemeFile(exportThemeFile)

    return createNoContent()

  }

  override fun findExportThemeFile(exportThemeId: UUID, exportThemeFileId: UUID): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isMetatavuAdmin && !isRealmSystemAdmin) {
      return createForbidden(createNotAllowedMessage(FIND, EXPORT_THEME))
    }

    val theme: ExportTheme = exportThemeController.findExportTheme(exportThemeId)
            ?: return createNotFound(createNotFoundMessage(EXPORT_THEME, exportThemeId))

    val exportThemeFile = exportThemeController.findExportThemeFile(exportThemeFileId)
            ?: return createNotFound(createNotFoundMessage(EXPORT_THEME_FILE, exportThemeFileId))

    return if (exportThemeFile.theme?.id != theme.id) {
      return createNotFound(createNotBelongMessage(EXPORT_THEME))
    } else createOk(exportThemeTranslator.translateExportThemeFile(exportThemeFile))

  }

  override fun listExportThemeFiles(exportThemeId: UUID): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isMetatavuAdmin && !isRealmSystemAdmin) {
      return createForbidden(createNotAllowedMessage(LIST, EXPORT_THEME))
    }

    val theme: ExportTheme = exportThemeController.findExportTheme(exportThemeId)
            ?: return createBadRequest(createNotFoundMessage(EXPORT_THEME, exportThemeId))

    return createOk(exportThemeController.listExportThemeFiles(theme)
            .map(exportThemeTranslator::translateExportThemeFile))

  }

  override fun updateExportThemeFile(
    exportThemeId: UUID,
    exportThemeFileId: UUID,
    exportThemeFile: ExportThemeFile
  ): Response {
    val userId = loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isMetatavuAdmin && !isRealmSystemAdmin) {
      return createForbidden(createNotAllowedMessage(UPDATE, EXPORT_THEME))
    }

    val theme = exportThemeController.findExportTheme(exportThemeId)
            ?: return createNotFound(createNotFoundMessage(EXPORT_THEME, exportThemeId))

    val foundExportThemeFile = exportThemeController.findExportThemeFile(exportThemeFileId)
            ?: return createBadRequest(createNotFoundMessage(EXPORT_THEME_FILE, exportThemeFileId))

    if (theme.id != exportThemeId) {
      return createNotFound(createNotBelongMessage(EXPORT_THEME))
    }

    val updatedExportTheme = exportThemeController.updateExportThemeFile(
            foundExportThemeFile, exportThemeFile.path, exportThemeFile.content, userId)

    return createOk(exportThemeTranslator.translateExportThemeFile(updatedExportTheme))
  }
}