package fi.metatavu.metaform.rest

import fi.metatavu.metaform.api.model.MetaformVersion
import java.util.*
import javax.ws.rs.core.Response

class VersionsApi: fi.metatavu.metaform.api.spec.VersionsApi {
  override suspend fun createMetaformVersion(
    metaformId: UUID,
    metaformVersion: MetaformVersion
  ): Response {
    TODO("Not yet implemented")
  }

  override suspend fun deleteMetaformVersion(metaformId: UUID, versionId: UUID): Response {
    TODO("Not yet implemented")
  }

  override suspend fun findMetaformVersion(metaformId: UUID, versionId: UUID): Response {
    TODO("Not yet implemented")
  }

  override suspend fun listMetaformVersions(metaformId: UUID): Response {
    TODO("Not yet implemented")
  }
}