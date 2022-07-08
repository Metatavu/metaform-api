package fi.metatavu.metaform.server.test.functional.builder.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.metaform.api.client.apis.ExportThemesApi
import fi.metatavu.metaform.api.client.infrastructure.ApiClient
import fi.metatavu.metaform.api.client.infrastructure.ApiClient.Companion.accessToken
import fi.metatavu.metaform.api.client.infrastructure.ClientException
import fi.metatavu.metaform.api.client.models.ExportTheme
import fi.metatavu.metaform.server.test.functional.ApiTestSettings
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import fi.metatavu.pickway.cloud.api.test.functional.impl.ApiTestBuilderResource
import org.junit.Assert
import java.io.IOException
import java.util.*

/**
 * Test builder resource for Export Themes API
 */
class ExportThemeTestBuilderResource(
        testBuilder: TestBuilder,
        private val accessTokenProvider: AccessTokenProvider?,
        apiClient: ApiClient
): ApiTestBuilderResource<ExportTheme, ApiClient?>(testBuilder, apiClient) {

    override fun getApi(): ExportThemesApi {
        accessToken = accessTokenProvider?.accessToken
        return ExportThemesApi(ApiTestSettings.apiBasePath)
    }

    @Throws(IOException::class)
    override fun clean(exportTheme: ExportTheme) {
        api.deleteExportTheme(exportTheme.id!!)
    }
    /**
     * Creates simple export theme
     *
     * @param name name
     * @return export theme
     */
    /**
     * Creates simple export theme
     *
     * @return export theme
     * @throws IOException thrown when request fails
     */
    @JvmOverloads
    @Throws(IOException::class)
    fun createSimpleExportTheme(name: String? = "simple"): ExportTheme {
        val payload = ExportTheme(name!!, null, null, null)
        return createExportTheme(payload)
    }

    /**
     * Creates export theme
     *
     * @param payload payload
     * @return export theme
     */
    @Throws(IOException::class)
    fun createExportTheme(payload: ExportTheme?): ExportTheme {
        val exportTheme = api.createExportTheme(payload!!)
        return addClosable(exportTheme)
    }

    /**
     * Finds export theme by id
     *
     * @param exportThemeId exportThemeId
     * @return found export theme
     */
    @Throws(IOException::class)
    fun findExportTheme(exportThemeId: UUID?): ExportTheme {
        return api.findExportTheme(exportThemeId!!)
    }

    /**
     * Lists all export themes
     *
     * @return export theme list
     */
    @Throws(IOException::class)
    fun listExportThemes(): List<ExportTheme> {
        return Arrays.asList(*api.listExportThemes())
    }

    /**
     * Updates export theme
     *
     * @param exportThemeId exportThemeId
     * @param exportTheme   exportTheme
     * @return updated export theme
     */
    @Throws(IOException::class)
    fun updateExportTheme(exportThemeId: UUID?, exportTheme: ExportTheme?): ExportTheme {
        return api.updateExportTheme(exportThemeId!!, exportTheme!!)
    }

    /**
     * Deletes export theme
     *
     * @param exportThemeId id of export theme to delete
     */
    @Throws(IOException::class)
    fun deleteExportTheme(exportThemeId: UUID) {
        api.deleteExportTheme(exportThemeId)
        removeCloseable { closable ->
            if (closable is ExportTheme) {
                return@removeCloseable exportThemeId == closable.id
            }
            false
        }
    }

    /**
     * Asserts expected status for search
     *
     * @param status        expected status
     * @param exportThemeId exportThemeId
     */
    @Throws(IOException::class)
    fun assertSearchFailStatus(status: Int, exportThemeId: UUID?) {
        try {
            api.findExportTheme(exportThemeId!!)
            Assert.fail(String.format("Expected find to fail with status %d", status))
        } catch (e: ClientException) {
            Assert.assertEquals(status.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Asserts expected status for creation
     *
     * @param status      expected status
     * @param exportTheme exportTheme
     */
    @Throws(IOException::class)
    fun assertCreateFailStatus(status: Int, exportTheme: ExportTheme?) {
        try {
            api.createExportTheme(exportTheme!!)
            Assert.fail(String.format("Expected find to fail with status %d", status))
        } catch (e: ClientException) {
            Assert.assertEquals(status.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Asserts list to fail with given status
     *
     * @param status expected status
     */
    @Throws(IOException::class)
    fun assertListFailStatus(status: Int) {
        try {
            api.listExportThemes()
            Assert.fail(String.format("Expected find to fail with status %d", status))
        } catch (e: ClientException) {
            Assert.assertEquals(status.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Asserts update to fail with given status
     *
     * @param status        expected status
     * @param exportThemeId export theme id
     */
    @Throws(IOException::class)
    fun assertUpdateFailStatus(status: Int, exportThemeId: UUID?, exportTheme: ExportTheme?) {
        try {
            api.updateExportTheme(exportThemeId!!, exportTheme!!)
            Assert.fail(String.format("Expected find to fail with status %d", status))
        } catch (e: ClientException) {
            Assert.assertEquals(status.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Asserts delete to fail with given status
     *
     * @param status        expected status
     * @param exportThemeId export theme id
     */
    @Throws(IOException::class)
    fun assertDeleteFailStatus(status: Int, exportThemeId: UUID?) {
        try {
            api.deleteExportTheme(exportThemeId!!)
            Assert.fail(String.format("Expected find to fail with status %d", status))
        } catch (e: ClientException) {
            Assert.assertEquals(status.toLong(), e.statusCode.toLong())
        }
    }
}