package fi.metatavu.metaform.server.test.functional.builder.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.metaform.api.client.apis.MetaformsApi
import fi.metatavu.metaform.api.client.infrastructure.ApiClient
import fi.metatavu.metaform.api.client.infrastructure.ApiClient.Companion.accessToken
import fi.metatavu.metaform.api.client.infrastructure.ClientException
import fi.metatavu.metaform.api.client.models.Metaform
import fi.metatavu.metaform.server.test.functional.ApiTestSettings
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import org.json.JSONException
import org.junit.Assert
import java.io.IOException
import java.util.*

/**
 * Test builder resource for metaforms API
 *
 * @author Antti Leppä
 */
class MetaformTestBuilderResource(
    testBuilder: TestBuilder,
    private val accessTokenProvider: AccessTokenProvider?,
    apiClient: ApiClient
): ApiTestBuilderResource<Metaform, ApiClient?>(testBuilder, apiClient) {

    override fun getApi(): MetaformsApi {
        accessToken = accessTokenProvider?.accessToken
        return MetaformsApi(ApiTestSettings.apiBasePath)
    }

    @Throws(IOException::class)
    override fun clean(metaform: Metaform) {
        api.deleteMetaform(metaform.id!!)
    }

    /**
     * Creates new metaform
     *
     * @param payload payload
     * @return created metaform
     */
    @Throws(IOException::class)
    fun create(payload: Metaform): Metaform {
        return addClosable(api.createMetaform(payload))
    }

    /**
     * Finds a metaform
     *
     * @param metaformId metaform id
     * @param replyId    Id of reply the form is loaded for. Reply id needs to be defined when unanonymous form is authenticated with owner key  (optional)
     * @param ownerKey   Reply owner key (optional)
     * @return found metaform
     */
    @Throws(IOException::class)
    fun findMetaform(metaformId: UUID, replyId: UUID?, ownerKey: String?): Metaform {
        return api.findMetaform(metaformId, replyId, ownerKey)
    }

    /**
     * Updates a metaform into the API
     *
     * @param body body payload
     */
    @Throws(IOException::class)
    fun updateMetaform(id: UUID, body: Metaform): Metaform {
        return api.updateMetaform(id, body)
    }

    /**
     * Lists all metaforms
     *
     * @return all metaforms
     */
    @Throws(IOException::class)
    fun list(): Array<Metaform> {
        return api.listMetaforms()
    }

    /**
     * Deletes a metaform from the API
     *
     * @param metaformId id of metaform to be deleted
     */
    @Throws(IOException::class)
    fun delete(metaformId: UUID) {
        api.deleteMetaform(metaformId)
        removeCloseable { closable ->
            if (closable is Metaform) {
                return@removeCloseable metaformId != closable.id
            }
            false
        }
    }

    /**
     * Asserts metaform count within the system
     *
     * @param expected expected count
     */
    @Throws(IOException::class)
    fun assertCount(expected: Int) {
        Assert.assertEquals(expected.toLong(), api.listMetaforms().size.toLong())
    }

    /**
     * Asserts find status fails with given status code
     *
     * @param expectedStatus expected status code
     * @param metaformId     metaform id
     * @param replyId        Id of reply the form is loaded for. Reply id needs to be defined when anonymous form is authenticated with owner key  (optional)
     * @param ownerKey       Reply owner key (optional)
     */
    @JvmOverloads
    @Throws(IOException::class)
    fun assertFindFailStatus(expectedStatus: Int, metaformId: UUID, replyId: UUID? = null, ownerKey: String? = null) {
        try {
            api.findMetaform(metaformId, replyId, ownerKey)
            Assert.fail(String.format("Expected find to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assert.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Asserts create status fails with given status code
     *
     * @param expectedStatus expected status code
     * @param payload        payload
     */
    @Throws(IOException::class)
    fun assertCreateFailStatus(expectedStatus: Int, payload: Metaform) {
        try {
            api.createMetaform(payload)
            Assert.fail(String.format("Expected create to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assert.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Asserts update status fails with given status code
     *
     * @param expectedStatus expected status code
     * @param metaformId     metaformId
     * @param metaform       metaform
     */
    @Throws(IOException::class)
    fun assertUpdateFailStatus(expectedStatus: Int, metaformId: UUID, metaform: Metaform) {
        try {
            api.updateMetaform(metaformId, metaform)
            Assert.fail(String.format("Expected update to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assert.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Asserts delete status fails with given status code
     *
     * @param expectedStatus expected status code
     * @param metaformId metaform id
     */
    @Throws(IOException::class)
    fun assertDeleteFailStatus(expectedStatus: Int, metaformId: UUID) {
        try {
            api.deleteMetaform(metaformId)
            Assert.fail(String.format("Expected delete to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assert.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Asserts list status fails with given status code
     *
     * @param expectedStatus expected status code
     */
    @Throws(IOException::class)
    fun assertListFailStatus(expectedStatus: Int) {
        try {
            api.listMetaforms()
            Assert.fail(String.format("Expected list to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assert.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Asserts that actual metaform equals expected metaform when both are serialized into JSON
     *
     * @param expected expected metaform
     * @param actual   actual metaform
     * @throws JSONException thrown when JSON serialization error occurs
     * @throws IOException   thrown when IO Exception occurs
     */
    @Throws(IOException::class, JSONException::class)
    fun assertMetaformsEqual(expected: Metaform?, actual: Metaform?) {
        assertJsonsEqual(expected, actual)
    }

    /**
     * Reads a Metaform from JSON file
     *
     * @param form file name
     * @return Metaform object
     * @throws IOException throws IOException when JSON reading fails
     */
    @Throws(IOException::class)
    fun readMetaform(form: String): Metaform? {
        return MetaformsReader.readMetaform(form)
    }

    /**
     * Creates new metaform using predefined test form
     *
     * @param form form's file name
     * @return created metaform
     */
    @Throws(IOException::class)
    fun createFromJsonFile(form: String): Metaform {
        return create(readMetaform(form)!!)
    }
}