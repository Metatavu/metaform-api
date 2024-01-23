package fi.metatavu.metaform.server.test.functional.builder.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.metaform.api.client.apis.ScriptsApi
import fi.metatavu.metaform.api.client.infrastructure.ApiClient
import fi.metatavu.metaform.api.client.models.Script
import fi.metatavu.metaform.server.test.functional.ApiTestSettings
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import java.io.IOException
import java.util.*

/**
 * Test builder resource for scripts API
 */
class ScriptsTestBuilderResource(
  testBuilder: TestBuilder,
  private val accessTokenProvider: AccessTokenProvider?,
  apiClient: ApiClient
): ApiTestBuilderResource<Script, ApiClient?>(testBuilder, apiClient)  {
  /**
   * Creates new script
   *
   * @param payload payload
   */
  @Throws(IOException::class)
  fun create(payload: Script): Script {
    return addClosable(api.createScript(payload))
  }

  /**
   * Updates a script
   *
   * @param payload payload
   */
  @Throws(IOException::class)
  fun update(payload: Script): Script {
    return api.updateScript(payload.id!!, payload)
  }

  /**
   * Lists scripts
   */
  @Throws(IOException::class)
  fun list(): Array<Script> {
    return api.listScripts()
  }

  /**
   * Finds a script
   *
   * @param scriptId script id
   */
  @Throws(IOException::class)
  fun find(scriptId: UUID): Script {
    return api.findScript(scriptId)
  }

  /**
   * Deletes a script from the API
   *
   * @param scriptId id of script to be deleted
   */
  @Throws(IOException::class)
  fun delete(scriptId: UUID) {
    api.deleteScript(scriptId)
    removeCloseable { closable ->
      if (closable is Script) {
        return@removeCloseable scriptId == closable.id
      }
      false
    }
  }

  @Throws(IOException::class)
  override fun clean(script: Script) {
    api.deleteScript(script.id!!)
  }

  override fun getApi(): ScriptsApi {
    ApiClient.accessToken = accessTokenProvider?.accessToken
    return ScriptsApi(ApiTestSettings.apiBasePath)
  }
}