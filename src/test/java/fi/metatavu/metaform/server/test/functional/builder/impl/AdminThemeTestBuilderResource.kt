package fi.metatavu.metaform.server.test.functional.builder.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.metaform.api.client.apis.AdminThemeApi
import fi.metatavu.metaform.api.client.infrastructure.ApiClient
import fi.metatavu.metaform.api.client.infrastructure.ApiClient.Companion.accessToken
import fi.metatavu.metaform.api.client.infrastructure.ClientException
import fi.metatavu.metaform.api.client.models.AdminTheme
import fi.metatavu.metaform.server.test.functional.ApiTestSettings
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import java.io.IOException
import java.util.*
import org.junit.Assert

/**
 * Test builder resource for admin themes
 * 
 * @author Otto Hooper
 */
class AdminThemeTestBuilderResource(
    testBuilder: TestBuilder,
    private val accessTokenProvider: AccessTokenProvider?,
    apiClient: ApiClient
): ApiTestBuilderResource<AdminTheme, ApiClient?>(testBuilder, apiClient) {

    override fun getApi(): AdminThemeApi {
        accessToken = accessTokenProvider?.accessToken
        return AdminThemeApi(ApiTestSettings.apiBasePath)
    }

    @Throws(IOException::class)
    override fun clean(adminTheme: AdminTheme) {}

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
    @Throws(IOException::class)
    fun create(data: String, name: String, slug: String): AdminTheme {
        val adminTheme = AdminTheme(data, name, slug)
        return api.createAdminTheme(adminTheme)
    }

    /**
     * Find an admin theme by it's id
     * 
     * @param id
     * 
     * @return admin theme
     */
    @Throws(IOException::class)
    fun findById(id: UUID): AdminTheme {
        return api.findAdminTheme(id)
    }

    /**
     * Updates an admin theme
     * 
     * @param themeId id of the admin theme
     * @param adminTheme data for the admin theme
     * 
     * @return updated admin theme
     */
    @Throws(IOException::class)
    fun update(themeId: UUID, adminTheme: AdminTheme): AdminTheme {
        return api.updateAdminTheme(themeId, adminTheme)
    }

    /**
     * List admin theme
     * 
     * @return admin themes
     */
    @Throws(IOException::class)
    fun list(): Array<AdminTheme> {
        return api.listAdminTheme()
    }

    /**
     * Deletes an admin theme
     * 
     * @param themeId id of the admin theme
     * 
     * @return deleted admin theme
     */
    @Throws(IOException::class)
    fun delete(themeId: UUID) {
        return api.deleteAdminTheme(themeId)
    }

    /**
     * Asserts expected status for search
     *
     * @param status expected status
     * @param themeId theme id
     */
    @Throws(IOException::class)
    fun assertSearchFailStatus(status: Int, themeId: UUID) {
        try {
            api.findAdminTheme(themeId)
            Assert.fail(String.format("Expected find to fail with status %d", status))
        } catch (e: ClientException) {
            Assert.assertEquals(status.toLong(), e.statusCode.toLong())
        }
    }
}