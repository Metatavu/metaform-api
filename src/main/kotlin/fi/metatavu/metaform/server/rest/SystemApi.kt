package fi.metatavu.metaform.server.rest

import jakarta.enterprise.context.RequestScoped
import jakarta.transaction.Transactional
import jakarta.ws.rs.core.Response

/**
 * Healthcheck to check if the API is running.
 */
@RequestScoped
@Transactional
class SystemApi: fi.metatavu.metaform.api.spec.SystemApi, AbstractApi() {
  override fun ping(): Response {
    return createOk("pong")
  }
}