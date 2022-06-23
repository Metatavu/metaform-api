package fi.metatavu.metaform.server.rest

import fi.metatavu.metaform.api.spec.model.ExportThemeFile
import java.util.*
import javax.ws.rs.core.Response

class ExportThemeFilesApi: fi.metatavu.metaform.api.spec.ExportThemeFilesApi {
  override suspend fun createExportThemeFile(
    exportThemeId: UUID,
    exportThemeFile: ExportThemeFile
  ): Response {
    TODO("Not yet implemented")
  }

  override suspend fun deleteExportThemeFile(
    exportThemeId: UUID,
    exportThemeFileId: UUID
  ): Response {
    TODO("Not yet implemented")
  }

  override suspend fun findExportThemeFile(exportThemeId: UUID, exportThemeFileId: UUID): Response {
    TODO("Not yet implemented")
  }

  override suspend fun listExportThemeFiles(exportThemeId: UUID): Response {
    TODO("Not yet implemented")
  }

  override suspend fun updateExportThemeFile(
    exportThemeId: UUID,
    exportThemeFileId: UUID,
    exportThemeFile: ExportThemeFile
  ): Response {
    TODO("Not yet implemented")
  }


}