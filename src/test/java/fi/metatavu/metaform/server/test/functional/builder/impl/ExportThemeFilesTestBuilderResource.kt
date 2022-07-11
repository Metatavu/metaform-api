package fi.metatavu.metaform.server.test.functional.builder.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.metaform.api.client.apis.ExportThemeFilesApi
import fi.metatavu.metaform.api.client.infrastructure.ApiClient
import fi.metatavu.metaform.api.client.infrastructure.ApiClient.Companion.accessToken
import fi.metatavu.metaform.api.client.infrastructure.ClientException
import fi.metatavu.metaform.api.client.models.ExportThemeFile
import fi.metatavu.metaform.server.test.functional.ApiTestSettings
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import fi.metatavu.pickway.cloud.api.test.functional.impl.ApiTestBuilderResource
import org.junit.Assert
import java.io.IOException
import java.util.*

/**
 * Test builder resource for Export theme files API
 */
class ExportThemeFilesTestBuilderResource(
        testBuilder: TestBuilder,
        private val accessTokenProvider: AccessTokenProvider?,
        apiClient: ApiClient
): ApiTestBuilderResource<ExportThemeFile, ApiClient?>(testBuilder, apiClient) {

    override fun getApi(): ExportThemeFilesApi {
        accessToken = accessTokenProvider?.accessToken
        return ExportThemeFilesApi(ApiTestSettings.apiBasePath)
    }

    @Throws(IOException::class)
    override fun clean(exportThemeFile: ExportThemeFile) {
        api.deleteExportThemeFile(exportThemeFile.themeId, exportThemeFile.id!!)
    }

    /**
     * Creates export theme file from payload
     *
     * @param themeId theme id
     * @param path    path
     * @param content content
     * @return generated export theme file
     */
    @Throws(IOException::class)
    fun createSimpleExportThemeFile(themeId: UUID, path: String, content: String): ExportThemeFile {
        val payload = ExportThemeFile(path, themeId, content, null)
        return createExportThemeFile(themeId, payload)
    }

    /**
     * Creates export theme file
     *
     * @param themeId parent theme
     * @param payload payload
     * @return created export theme file
     */
    @Throws(IOException::class)
    fun createExportThemeFile(themeId: UUID, payload: ExportThemeFile): ExportThemeFile {
        val exportThemeFile = api.createExportThemeFile(themeId, payload)
        return addClosable(exportThemeFile)
    }

    /**
     * Finds export theme file
     *
     * @param exportThemeId      exportThemeId
     * @param exportThemeFieldId exportThemeFieldId
     * @return found export theme file
     */
    @Throws(IOException::class)
    fun findExportThemeFile(exportThemeId: UUID, exportThemeFieldId: UUID): ExportThemeFile {
        return api.findExportThemeFile(exportThemeId, exportThemeFieldId)
    }

    /**
     * List export theme files by export theme id
     *
     * @param id export theme id
     * @return list of export theme files
     */
    @Throws(IOException::class)
    fun listExportThemeFiles(id: UUID): List<ExportThemeFile> {
        return listOf(*api.listExportThemeFiles(id))
    }

    /**
     * Updates export theme file
     *
     * @param exportThemeId     exportThemeId
     * @param exportThemeFileId exportThemeFileId
     * @param createdThemeFile  createdThemeFile
     * @return updated export theme file
     */
    @Throws(IOException::class)
    fun updateExportThemeFile(exportThemeId: UUID, exportThemeFileId: UUID, createdThemeFile: ExportThemeFile): ExportThemeFile {
        return api.updateExportThemeFile(exportThemeId, exportThemeFileId, createdThemeFile)
    }

    /**
     * Delete export theme file
     *
     * @param themeId         themeId
     * @param exportThemeFileId exportThemeFile id
     */
    @Throws(IOException::class)
    fun deleteExportThemeFile(themeId: UUID?, exportThemeFileId: UUID) {
        api.deleteExportThemeFile(themeId!!, exportThemeFileId)
        removeCloseable { closable ->
            if (closable is ExportThemeFile) {
                return@removeCloseable exportThemeFileId == closable.id
            }
            false
        }
    }

    /**
     * Asserts search status
     *
     * @param status            expected status
     * @param exportThemeId     exportThemeId
     * @param exportThemeFileId exportThemeFileId
     */
    @Throws(IOException::class)
    fun assertFindFailStatus(status: Int, exportThemeId: UUID?, exportThemeFileId: UUID?) {
        try {
            api.findExportThemeFile(exportThemeId!!, exportThemeFileId!!)
            Assert.fail(String.format("Expected find to fail with status %d", status))
        } catch (e: ClientException) {
            Assert.assertEquals(status.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Asserts export theme file creation to fail with given status
     *
     * @param status          expected status
     * @param exportThemeFile new export theme file payload
     */
    @Throws(IOException::class)
    fun assertCreateFailStatus(status: Int, exportThemeFile: ExportThemeFile) {
        try {
            api.createExportThemeFile(exportThemeFile.themeId, exportThemeFile)
            Assert.fail(String.format("Expected find to fail with status %d", status))
        } catch (e: ClientException) {
            Assert.assertEquals(status.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Asserts finding export theme file to fail with given status
     *
     * @param status            expected status
     * @param exportThemeId     export theme id
     * @param exportThemeFileId export theme file id
     */
    @Throws(IOException::class)
    fun assertFindExportThemeFileFailStatus(status: Int, exportThemeId: UUID?, exportThemeFileId: UUID?) {
        try {
            api.findExportThemeFile(exportThemeId!!, exportThemeFileId!!)
            Assert.fail(String.format("Expected find to fail with status %d", status))
        } catch (e: ClientException) {
            Assert.assertEquals(status.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Asserts listing export theme files to fail with given status
     *
     * @param status        expected status
     * @param exportThemeId export theme id
     */
    @Throws(IOException::class)
    fun assertListFailStatus(status: Int, exportThemeId: UUID?) {
        try {
            api.listExportThemeFiles(exportThemeId!!)
            Assert.fail(String.format("Expected find to fail with status %d", status))
        } catch (e: ClientException) {
            Assert.assertEquals(status.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Asserts updating export theme file to fail
     *
     * @param status            expected status
     * @param exportThemeId     export theme id
     * @param exportThemeFileId export theme file id
     * @param exportThemeFile   new export theme file
     */
    @Throws(IOException::class)
    fun assertUpdateFailStatus(status: Int, exportThemeId: UUID?, exportThemeFileId: UUID?, exportThemeFile: ExportThemeFile?) {
        try {
            api.updateExportThemeFile(exportThemeId!!, exportThemeFileId!!, exportThemeFile!!)
            Assert.fail(String.format("Expected find to fail with status %d", status))
        } catch (e: ClientException) {
            Assert.assertEquals(status.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Asserts deleting export theme file to fail
     *
     * @param status            expected status
     * @param exportThemeId     export theme id
     * @param exportThemeFileId export theme file id
     */
    @Throws(IOException::class)
    fun assertDeleteFailStatus(status: Int, exportThemeId: UUID?, exportThemeFileId: UUID?) {
        try {
            api.deleteExportThemeFile(exportThemeId!!, exportThemeFileId!!)
            Assert.fail(String.format("Expected find to fail with status %d", status))
        } catch (e: ClientException) {
            Assert.assertEquals(status.toLong(), e.statusCode.toLong())
        }
    }
}