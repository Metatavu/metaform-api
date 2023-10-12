package fi.metatavu.metaform.server.test.functional.builder.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.metaform.api.client.apis.DraftsApi
import fi.metatavu.metaform.api.client.apis.TemplatesApi
import fi.metatavu.metaform.api.client.infrastructure.ApiClient
import fi.metatavu.metaform.api.client.models.Metaform
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import fi.metatavu.metaform.api.client.models.Template
import fi.metatavu.metaform.server.test.functional.ApiTestSettings
import java.io.IOException

/**
 * Test builder resource for Template API
 */
class TemplateTestBuilderResource (
        testBuilder: TestBuilder,
        private val accessTokenProvider: AccessTokenProvider?,
        apiClient: ApiClient
): ApiTestBuilderResource<Template, ApiClient?>(testBuilder, apiClient) {

    override fun getApi(): TemplatesApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return TemplatesApi(ApiTestSettings.apiBasePath)
    }
    override fun clean(template: Template?) {
        val testBuilder = TestBuilder()
        val templatesAdminApi = testBuilder.systemAdmin.templates.api

    }

    @Throws(IOException::class)
    fun createTemplate(): Template {
        TODO("Not yet implemented")
    }

    @Throws(IOException::class)
    fun deleteTemplate(): Template {
        TODO("Not yet implemented")
    }

    @Throws(IOException::class)
    fun findTemplate(): Template {
        TODO("Not yet implemented")
    }

}