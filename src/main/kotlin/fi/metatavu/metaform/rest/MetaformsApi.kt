package fi.metatavu.metaform.rest

import fi.metatavu.metaform.api.model.Metaform
import java.util.*
import javax.ws.rs.core.Response

class MetaformsApi: fi.metatavu.metaform.api.spec.MetaformsApi {
  override suspend fun createMetaform(metaform: Metaform): Response {
    TODO("Not yet implemented")
  }

  override suspend fun deleteMetaform(metaformId: UUID): Response {
    TODO("Not yet implemented")
  }

  override suspend fun findMetaform(metaformId: UUID, replyId: UUID?, ownerKey: String?): Response {
    TODO("Not yet implemented")
  }

  override suspend fun listMetaforms(): Response {
    TODO("Not yet implemented")
  }

  override suspend fun updateMetaform(metaformId: UUID, metaform: Metaform): Response {
    TODO("Not yet implemented")
  }
}