package fi.metatavu.metaform.server.test.functional.builder.impl
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.metaform.api.client.apis.AdminThemeApi
import fi.metatavu.metaform.api.client.infrastructure.ApiClient
import fi.metatavu.metaform.api.client.models.AdminTheme
import fi.metatavu.metaform.server.test.functional.ApiTestSettings
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import java.util.*

class AdminThemeTestBuilderResource(
    testBuilder: TestBuilder,
    private val accessTokenProvider: AccessTokenProvider?,
    apiClient: ApiClient
): ApiTestBuilderResource<AdminTheme, ApiClient?>(testBuilder, apiClient) {

    override fun getApi(): AdminThemeApi {
        accessToken = accessTokenProvider?.accessToken
        return AdminThemeApi(ApiTestSettings.apiBasePath)
    }

    /**
     * Creates a new admin theme
     *
     * @param data data for the admin theme
     * @param name name of the admin theme
     * @param slug slug of the admin theme
     * @param creatorId creator id of the admin theme
     * @param lastModifierId last modifier id of the admin theme
     *
     * @return created admin theme
     */
    fun create(data: String, name: String, slug: String): AdminTheme {
        val adminTheme = AdminTheme(data, name, slug)
        return api.create(adminTheme)
    }
}
