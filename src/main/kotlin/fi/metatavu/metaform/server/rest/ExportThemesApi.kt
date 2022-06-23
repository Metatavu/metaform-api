package fi.metatavu.metaform.server.rest

import fi.metatavu.metaform.api.spec.model.ExportTheme
import java.util.*
import javax.ws.rs.core.Response

class ExportThemesApi: fi.metatavu.metaform.api.spec.ExportThemesApi {
  override suspend fun createExportTheme(exportTheme: ExportTheme): Response {
    TODO("Not yet implemented")
  }

  override suspend fun deleteExportTheme(exportThemeId: UUID): Response {
    TODO("Not yet implemented")
  }

  override suspend fun findExportTheme(exportThemeId: UUID): Response {
    TODO("Not yet implemented")
  }

  override suspend fun listExportThemes(): Response {
    TODO("Not yet implemented")
  }

  override suspend fun updateExportTheme(exportThemeId: UUID, exportTheme: ExportTheme): Response {
    TODO("Not yet implemented")
  }
}