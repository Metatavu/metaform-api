package fi.metatavu.metaform.server.rest.translate

import fi.metatavu.metaform.api.spec.model.ExportTheme
import fi.metatavu.metaform.api.spec.model.ExportThemeFile
import javax.enterprise.context.ApplicationScoped

/**
 * Translator for export themes
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class ExportThemeTranslator {

  /**
   * Translates JPA ExportTheme object into REST ExportTheme object
   *
   * @param exportTheme JPA ExportTheme object
   * @return REST ExportTheme
   */
  fun translateExportTheme(exportTheme: fi.metatavu.metaform.server.persistence.model.ExportTheme): ExportTheme {
    return ExportTheme(
      id = exportTheme.id,
      locales = exportTheme.locales,
      name = exportTheme.name,
      parentId = exportTheme.parent?.id
    )
  }

  /**
   * Translates JPA ExportThemeFile object into REST ExportThemeFile object
   *
   * @param exportThemeFile JPA ExportThemeFile object
   * @return REST ExportThemeFile
   */
  fun translateExportThemeFile(exportThemeFile: fi.metatavu.metaform.server.persistence.model.ExportThemeFile): ExportThemeFile {
    return ExportThemeFile(
      id = exportThemeFile.id,
      content = exportThemeFile.content,
      path = exportThemeFile.path,
      themeId = exportThemeFile.theme!!.id!!
    )
  }
}