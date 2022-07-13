package fi.metatavu.metaform.server.rest

import javax.enterprise.context.RequestScoped
import javax.transaction.Transactional
import javax.ws.rs.Consumes
import javax.ws.rs.core.Response

/**
 * Healthcheck to check if the API is running.
 */
@RequestScoped
@Transactional
class SystemApi: fi.metatavu.metaform.api.spec.SystemApi, AbstractApi() {
  @Consumes("application/json;charset=utf-8")
  override suspend fun ping(): Response {
    return createOk("pong")
  }
}