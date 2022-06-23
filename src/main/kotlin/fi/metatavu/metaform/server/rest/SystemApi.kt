package fi.metatavu.metaform.server.rest

import javax.ws.rs.core.Response

class SystemApi: fi.metatavu.metaform.api.spec.SystemApi {
  override suspend fun ping(): Response {
    TODO("Not yet implemented")
  }
}