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
    override fun clean(adminTheme: AdminTheme) {
        api.deleteAdminTheme(adminTheme.id!!) 
    }


    /**
     * Creates a simple admin theme
     *
     * @return created simple admin theme
     */
    fun getSimpleTheme(): AdminTheme {
        return AdminTheme(
            name = "simple-theme",
            data = exampleThemeData
        )
    }

    /**
     * Creates a new admin theme
     *
     * @param adminTheme admin theme
     * @return created admin theme
     */
    @Throws(IOException::class)
    fun create(adminTheme: AdminTheme): AdminTheme {
        val createdAdminTheme = api.createAdminTheme(adminTheme)
        return addClosable(createdAdminTheme)
    }

    /**
     * Creates a simple admin theme
     *
     * @return created simple admin theme
     */
    fun createSimpleTheme(): AdminTheme {
        val createdAdminTheme = api.createAdminTheme(getSimpleTheme())
        return addClosable(createdAdminTheme)
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
    fun list(): List<AdminTheme> {
        return listOf(*api.listAdminTheme())
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
        api.deleteAdminTheme(themeId)
        removeCloseable { closable ->
            if (closable is AdminTheme) {
                return@removeCloseable themeId == closable.id
            }
            false   
        }
    }

    /**
     * Asserts expected status for search
     *
     * @param status expected status
     * @param themeId theme id
     */
    @Throws(IOException::class)
    fun assertSearchFailStatus(status: Int, themeId: UUID?) {
        try {
            api.findAdminTheme(themeId!!)
            Assert.fail(String.format("Expected find to fail with status %d", status))
        } catch (e: ClientException) {
            Assert.assertEquals(status.toLong(), e.statusCode.toLong())
        }
    }
    
    /**
     * Asserts update to fail with given status
     *
     * @param status expected status
     * @param themeId theme id
     * @param adminTheme data for the admin theme
     */
    @Throws(IOException::class)
    fun assertUpdateFailStatus(status: Int, themeId: UUID, adminTheme: AdminTheme) {
        try {
            api.updateAdminTheme(themeId, adminTheme)
            Assert.fail(String.format("Expected update to fail with status %d", status))
        } catch (e: ClientException) {
            Assert.assertEquals(status.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Assert create to fail with given status
     * 
     * @param status expected status
     * @param adminTheme admin theme
     */
    @Throws(IOException::class)
    fun assertCreateFailStatus(status: Int, adminTheme: AdminTheme) {
        try {
            api.createAdminTheme(adminTheme)
            Assert.fail(String.format("Expected create to fail with status %d", status))
        } catch (e: ClientException) {
            Assert.assertEquals(status.toLong(), e.statusCode.toLong())
        }
    }

    /***
     * Assert list to fail with given status
     * 
     * @param status expected status
     */
    @Throws(IOException::class)
    fun assertListFailStatus(status: Int) {
        try {
            api.listAdminTheme()
            Assert.fail(String.format("Expected list to fail with status %d", status))
        } catch (e: ClientException) {
            Assert.assertEquals(status.toLong(), e.statusCode.toLong())
        }
    }
    
    /**
     * Assert find to fail with given status
     * 
     * @param status expected status
     * @param themeId theme id
     */
    @Throws(IOException::class)
    fun assertFindFailStatus(status: Int, themeId: UUID) {
        try {
            api.findAdminTheme(themeId)
            Assert.fail(String.format("Expected find to fail with status %d", status))
        } catch (e: ClientException) {
            Assert.assertEquals(status.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Gets example theme data
     *
     * @return example theme data
     */
    val exampleThemeData: Map<String, String>
        get() {
            val themeData: MutableMap<String, String> = HashMap()
            themeData["formData"] = "form value"
            return themeData
        }
}