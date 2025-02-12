package fi.metatavu.metaform.server.test.functional.builder.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.metaform.api.client.apis.VersionsApi
import fi.metatavu.metaform.api.client.infrastructure.ApiClient
import fi.metatavu.metaform.api.client.infrastructure.ApiClient.Companion.accessToken
import fi.metatavu.metaform.api.client.infrastructure.ClientException
import fi.metatavu.metaform.api.client.models.MetaformVersion
import fi.metatavu.metaform.server.test.functional.ApiTestSettings
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import org.junit.Assert
import java.io.IOException
import java.util.*

/**
 * Test builder resource for metaform versions API
 *
 * @author Tianxing Wu
 */
class MetaformVersionTestBuilderResource(
    testBuilder: TestBuilder,
    private val accessTokenProvider: AccessTokenProvider?,
    apiClient: ApiClient
): ApiTestBuilderResource<MetaformVersion, ApiClient?>(testBuilder, apiClient) {
    private val versionsMetaforms: MutableMap<UUID?, UUID> = HashMap()

    override fun getApi(): VersionsApi {
        accessToken = accessTokenProvider?.accessToken
        return VersionsApi(ApiTestSettings.apiBasePath)
    }

    @Throws(IOException::class)
    override fun clean(metaformVersion: MetaformVersion) {
        val metaformId = versionsMetaforms[metaformVersion.id]
        api.deleteMetaformVersion(metaformId!!, metaformVersion.id!!)
    }

    /**
     * Creates new metaform version
     *
     * @param metaformId metaform id
     * @param payload    payload
     * @return created metaform version
     */
    @Throws(IOException::class)
    fun create(metaformId: UUID, payload: MetaformVersion, addClosable: Boolean = true): MetaformVersion {
        val result = api.createMetaformVersion(metaformId, payload)
        versionsMetaforms[result.id] = metaformId
        if (addClosable) addClosable(result)
        return result
    }

    /**
     * Updates a metaform version into the API
     *
     * @param metaformId metaform id
     * @param versionId metaform version id
     * @param body body payload
     */
    @Throws(IOException::class)
    fun updateMetaformVersion(metaformId: UUID, versionId: UUID, body: MetaformVersion): MetaformVersion {
        return api.updateMetaformVersion(
            metaformId = metaformId,
            versionId = versionId,
            metaformVersion = body
        )
    }

    /**
     * Lists metaform versions
     *
     * @param metaformId metaform id
     * @return metaform versions
     */
    @Throws(IOException::class)
    fun list(metaformId: UUID): Array<MetaformVersion> {
        return api.listMetaformVersions(metaformId)
    }

    /**
     * Finds a metaform version
     *
     * @param metaformId metaform id
     * @param versionId  version id
     * @return found metaform version
     */
    @Throws(IOException::class)
    fun findVersion(metaformId: UUID, versionId: UUID): MetaformVersion {
        return api.findMetaformVersion(metaformId, versionId)
    }

    /**
     * Deletes a metaform version from the API
     *
     * @param metaformId      metaform id
     * @param metaformVersionId metaform version id
     */
    @Throws(IOException::class)
    fun delete(metaformId: UUID, metaformVersionId: UUID) {
        Assert.assertNotNull(metaformVersionId)
        api.deleteMetaformVersion(metaformId, metaformVersionId)
        removeCloseable { closable ->
            if (closable is MetaformVersion) {
                return@removeCloseable metaformVersionId == closable.id
            }
            false
        }
    }

    /**
     * Asserts metaform version count within the system
     *
     * @param metaformId metaform id
     * @param expected   expected count
     */
    @Throws(IOException::class)
    fun assertCount(metaformId: UUID, expected: Int) {
        Assert.assertEquals(expected.toLong(), api.listMetaformVersions(metaformId).size.toLong())
    }

    /**
     * Asserts find status fails with given status code
     *
     * @param expectedStatus expected status code
     * @param metaformId     metaform id
     * @param versionId      version id
     */
    @Throws(IOException::class)
    fun assertFindFailStatus(expectedStatus: Int, metaformId: UUID, versionId: UUID) {
        try {
            api.findMetaformVersion(metaformId, versionId)
            Assert.fail(String.format("Expected find to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assert.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Asserts create status fails with given status code
     *
     * @param expectedStatus expected status code
     * @param metaformId     metaform id
     * @param payload        payload
     */
    @Throws(IOException::class)
    fun assertCreateFailStatus(expectedStatus: Int, metaformId: UUID, payload: MetaformVersion) {
        try {
            api.createMetaformVersion(metaformId, payload)
            Assert.fail(String.format("Expected create to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assert.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Asserts delete status fails with given status code
     *
     * @param expectedStatus expected status code
     * @param metaformId     metaform
     * @param versionId      version id
     */
    @Throws(IOException::class)
    fun assertDeleteFailStatus(expectedStatus: Int, metaformId: UUID, versionId: UUID) {
        try {
            api.deleteMetaformVersion(metaformId, versionId)
            Assert.fail(String.format("Expected delete to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assert.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Asserts list status fails with given status code
     *
     * @param expectedStatus expected status code
     * @param metaformId     metaform id
     */
    @Throws(IOException::class)
    fun assertListFailStatus(expectedStatus: Int, metaformId: UUID) {
        try {
            api.listMetaformVersions(metaformId)
            Assert.fail(String.format("Expected list to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assert.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Asserts update status fails with given status code
     *
     * @param expectedStatus    expected status code
     * @param metaformId        metaform id
     * @param versionId         version id
     * @param metaformVersion   metaform version
     */
    @Throws(IOException::class)
    fun assertUpdateFailStatus(expectedStatus: Int, metaformId: UUID, versionId: UUID, metaformVersion: MetaformVersion) {
        try {
            api.updateMetaformVersion(metaformId, versionId, metaformVersion)
        } catch (e: ClientException) {
            Assert.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Gets example version data
     *
     * @return example version data
     */
    val exampleVersionData: Map<String, String>
        get() {
            val versionData: MutableMap<String, String> = HashMap()
            versionData["formData"] = "form value"
            return versionData
        }
}