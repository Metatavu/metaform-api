package fi.metatavu.metaform.rest

import fi.metatavu.metaform.api.model.Draft
import java.util.*
import javax.ws.rs.core.Response

class DraftsApi: fi.metatavu.metaform.api.spec.DraftsApi {
  override suspend fun createDraft(metaformId: UUID, draft: Draft): Response {
    TODO("Not yet implemented")
  }

  override suspend fun deleteDraft(metaformId: UUID, draftId: UUID): Response {
    TODO("Not yet implemented")
  }

  override suspend fun findDraft(metaformId: UUID, draftId: UUID): Response {
    TODO("Not yet implemented")
  }

  override suspend fun updateDraft(metaformId: UUID, draftId: UUID, draft: Draft): Response {
    TODO("Not yet implemented")
  }


}