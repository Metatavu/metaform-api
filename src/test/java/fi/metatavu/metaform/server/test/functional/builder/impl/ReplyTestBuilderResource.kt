package fi.metatavu.metaform.server.test.functional.builder.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.metaform.api.client.apis.RepliesApi
import fi.metatavu.metaform.api.client.infrastructure.ApiClient
import fi.metatavu.metaform.api.client.infrastructure.ApiClient.Companion.accessToken
import fi.metatavu.metaform.api.client.infrastructure.ClientException
import fi.metatavu.metaform.api.client.models.Reply
import fi.metatavu.metaform.server.rest.ReplyMode
import fi.metatavu.metaform.server.test.functional.ApiTestSettings
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import fi.metatavu.pickway.cloud.api.test.functional.impl.ApiTestBuilderResource
import org.json.JSONException
import org.junit.Assert
import java.io.IOException
import java.util.*

/**
 * Test builder resource for replies
 *
 * @author Antti Leppä
 */
class ReplyTestBuilderResource(
        testBuilder: TestBuilder,
        private val accessTokenProvider: AccessTokenProvider?,
        apiClient: ApiClient
): ApiTestBuilderResource<Reply, ApiClient?>(testBuilder, apiClient) {
    private val replyMetaformIds: MutableMap<UUID?, UUID?> = HashMap()

    override fun getApi(): RepliesApi {
        try {
            accessToken = accessTokenProvider?.accessToken
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return RepliesApi(ApiTestSettings.apiBasePath)
    }

    @Throws(IOException::class)
    override fun clean(reply: Reply) {
        val metaformId = replyMetaformIds[reply.id]
        api.deleteReply(metaformId!!, reply.id!!, null)
    }

    /**
     * Creates new reply
     *
     * @param metaformId     metaform id
     * @param updateExisting whether to update existing reply
     * @param replyMode      reply mode
     * @param payload        payload
     * @return created reply
     */
    @Throws(IOException::class)
    fun create(metaformId: UUID, updateExisting: Boolean?, replyMode: String?, payload: Reply): Reply {
        val result = api.createReply(metaformId, payload, updateExisting, replyMode)
        replyMetaformIds[result.id] = metaformId
        return addClosable(result)
    }

    /**
     * Creates new reply
     *
     * @param metaformId metaform id
     * @param replyMode  reply mode
     * @param payload    payload
     * @return created reply
     */
    @Throws(IOException::class)
    fun create(metaformId: UUID, replyMode: String?, payload: Reply): Reply {
        return create(metaformId, null, replyMode, payload)
    }

    /**
     * Lists all replies by filters
     *
     * @param metaformId       metaform id
     * @param userId           user id
     * @param createdBefore    createdBefore
     * @param createdAfter     createdAfter
     * @param modifiedBefore   modifiedBefore
     * @param modifiedAfter    modifiedAfter
     * @param includeRevisions includeRevisions
     * @param fields           fields
     * @param firstResult      firstResult
     * @param maxResults       maxResults
     * @return array of found replies
     */
    @Throws(IOException::class)
    fun listReplies(
            metaformId: UUID,
            userId: UUID?,
            createdBefore: String?,
            createdAfter: String?,
            modifiedBefore: String?,
            modifiedAfter: String?,
            includeRevisions: Boolean?,
            fields: Array<String>?,
            firstResult: Int?,
            maxResults: Int?
    ): Array<Reply> {
        return api.listReplies(metaformId, userId, createdBefore, createdAfter, modifiedBefore, modifiedAfter, includeRevisions, fields, firstResult, maxResults)
    }

    /**
     * Lists all replies by metaform
     *
     * @param metaformId metaform id
     * @return all found replies
     */
    @Throws(IOException::class)
    fun listReplies(metaformId: UUID): Array<Reply> {
        return api.listReplies(metaformId, null, null, null, null, null, null, null, null, null)
    }

    /**
     * Creates new reply for TBNC form
     *
     * @param metaformId metaform id
     * @param text      text value
     * @param bool      boolean value
     * @param number    number value
     * @param checklist checklist value
     * @return reply
     */
    @Throws(IOException::class)
    fun createTBNCReply(metaformId: UUID, text: String, bool: Boolean?, number: Double, checklist: Array<String?>): Reply {
        val replyData: MutableMap<String, Any> = HashMap()
        replyData["text"] = text
//        TODO check object nullability
        bool?.let { replyData["boolean"] = bool }
        replyData["number"] = number
        replyData["checklist"] = checklist
        val reply = createReplyWithData(replyData)
        return api.createReply(metaformId, reply, null, ReplyMode.REVISION.toString())
    }

    /**
     * Creates new reply for the simple form
     *
     * @param metaform  metaform
     * @param value     value
     * @param replyMode reply model
     * @return reply
     */
    @Throws(IOException::class)
    fun createSimpleReply(metaformId: UUID, value: String, replyMode: ReplyMode): Reply {
        val replyData1: MutableMap<String, Any> = HashMap()
        replyData1["text"] = value
        val reply = createReplyWithData(replyData1)
        return create(metaformId, replyMode.toString(), reply)
    }

    /**
     * Asserts reply can not be found with given owner key
     *
     * @param metaformId metaform id
     * @param reply    reply
     * @param ownerKey owner key
     */
    @Throws(IOException::class)
    fun assertReplyOwnerKeyFindForbidden(metaformId: UUID, replyId: UUID, ownerKey: String?) {
        try {
            api.findReply(metaformId, replyId, ownerKey)
            Assert.fail(String.format("Should not be able to find reply %s", replyId.toString()))
        } catch (e: ClientException) {
            Assert.assertEquals(403, e.statusCode.toLong())
        }
    }

    /**
     * Finds a reply
     *
     * @param metaformId metaform id
     * @param replyId    reply id
     * @param ownerKey   owner key
     * @return found reply
     */
    @Throws(IOException::class)
    fun findReply(metaformId: UUID, replyId: UUID, ownerKey: String?): Reply {
        return api.findReply(metaformId, replyId, ownerKey)
    }

    /**
     * Updates a reply into the API
     *
     * @param metaformId metaform id
     * @param body       body payload
     * @param ownerKey   owner key
     */
    @Throws(IOException::class)
    fun updateReply(metaformId: UUID, replyId: UUID, body: Reply, ownerKey: String?) {
        api.updateReply(metaformId, replyId, body, ownerKey)
    }

    /**
     * Deletes a reply from the API
     *
     * @param metaformId metaform id
     * @param reply      reply to be deleted
     * @param ownerKey   owner key
     */
    @Throws(IOException::class)
    fun delete(metaformId: UUID, replyId: UUID, ownerKey: String?) {
        Assert.assertNotNull(replyId)
        api.deleteReply(metaformId, replyId, ownerKey)
        removeCloseable { closable ->
            if (closable is Reply) {
                return@removeCloseable replyId == closable.id
            }
            false
        }
    }

    /**
     * Asserts reply count within the system
     *
     * @param expected         expected count
     * @param metaformId       Metaform id (required)
     * @param userId           Filter results by user id. If this parameter is not specified all replies are returned, this requires logged user to have proper permission to do so (optional)
     * @param createdBefore    Filter results created before specified time (optional)
     * @param createdAfter     Filter results created after specified time (optional)
     * @param modifiedBefore   Filter results modified before specified time (optional)
     * @param modifiedAfter    Filter results modified after specified time (optional)
     * @param includeRevisions Specifies that revisions should be included into response (optional)
     * @param fields           Filter results by field values. Format is field:value, multiple values can be added by using comma separator. E.g. field1&#x3D;value,field2&#x3D;another (optional)
     * @param firstResult      First index of results to be returned (optional)
     * @param maxResults       How many items to return at one time (optional)
     */
    @Throws(IOException::class)
    fun assertCount(expected: Int, metaformId: UUID, userId: UUID?, createdBefore: String?, createdAfter: String?, modifiedBefore: String?, modifiedAfter: String?, includeRevisions: Boolean?, fields: Array<String>?, firstResult: Int?, maxResults: Int?) {
        Assert.assertEquals(expected.toLong(), api.listReplies(metaformId, userId, createdBefore, createdAfter, modifiedBefore, modifiedAfter, includeRevisions, fields, firstResult, maxResults).size.toLong())
    }

    /**
     * Asserts create simple reply status fails with given status code
     *
     * @param expectedStatus expected status code
     * @param metaform id       metaform id
     * @param value          value
     * @param replyMode      replyMode
     */
    @Throws(IOException::class)
    fun assertCreateSimpleReplyFail(expectedStatus: Int, metaformId: UUID, value: String, replyMode: ReplyMode) {
        try {
            createSimpleReply(metaformId, value, replyMode)
            Assert.fail(String.format("Expected find to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assert.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Asserts find status fails with given status code
     *
     * @param expectedStatus expected status code
     * @param metaformId     metaform id
     * @param replyId        reply id
     * @param ownerKey       owner key
     */
    @Throws(IOException::class)
    fun assertFindFailStatus(expectedStatus: Int, metaformId: UUID, replyId: UUID, ownerKey: String?) {
        try {
            api.findReply(metaformId, replyId, ownerKey)
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
     * @param updateExisting whether to update existing reply
     * @param replyMode      reply mode
     * @param payload        payload
     */
    @Throws(IOException::class)
    fun assertCreateFailStatus(expectedStatus: Int, metaformId: UUID, updateExisting: Boolean?, replyMode: String?, payload: Reply) {
        try {
            api.createReply(metaformId, payload, updateExisting, replyMode)
            Assert.fail(String.format("Expected create to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assert.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Asserts update status fails with given status code
     *
     * @param expectedStatus expected status code
     * @param metaformId     metaform id
     * @param body           body payload
     * @param ownerKey       owner key
     */
    @Throws(IOException::class)
    fun assertUpdateFailStatus(expectedStatus: Int, metaformId: UUID, body: Reply, ownerKey: String?) {
        try {
            api.updateReply(metaformId, body.id!!, body, ownerKey)
            Assert.fail(String.format("Expected update to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assert.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Asserts delete status fails with given status code
     *
     * @param expectedStatus expected status code
     * @param metaformId     metaform id
     * @param replyId        reply id
     * @param ownerKey       owner key
     */
    @Throws(IOException::class)
    fun assertDeleteFailStatus(expectedStatus: Int, metaformId: UUID, replyId: UUID, ownerKey: String?) {
        try {
            api.deleteReply(metaformId, replyId, ownerKey)
            Assert.fail(String.format("Expected delete to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assert.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Asserts list status fails with given status code
     *
     * @param expectedStatus   expected status code
     * @param metaformId       Metaform id (required)
     * @param userId           Filter results by user id. If this parameter is not specified all replies are returned, this requires logged user to have proper permission to do so (optional)
     * @param createdBefore    Filter results created before specified time (optional)
     * @param createdAfter     Filter results created after specified time (optional)
     * @param modifiedBefore   Filter results modified before specified time (optional)
     * @param modifiedAfter    Filter results modified after specified time (optional)
     * @param includeRevisions Specifies that revisions should be included into response (optional)
     * @param fields           Filter results by field values. Format is field:value, multiple values can be added by using comma separator. E.g. field1&#x3D;value,field2&#x3D;another (optional)
     * @param firstResult      First index of results to be returned (optional)
     * @param maxResults       How many items to return at one time (optional)
     */
    @Throws(IOException::class)
    fun assertListFailStatus(expectedStatus: Int, metaformId: UUID, userId: UUID?, createdBefore: String?, createdAfter: String?, modifiedBefore: String?, modifiedAfter: String?, includeRevisions: Boolean?, fields: Array<String>?, firstResult: Int?, maxResults: Int?) {
        try {
            api.listReplies(metaformId, userId, createdBefore, createdAfter, modifiedBefore, modifiedAfter, includeRevisions, fields, firstResult, maxResults)
            Assert.fail(String.format("Expected list to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assert.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Asserts that actual reply equals expected reply when both are serialized into JSON
     *
     * @param expected expected reply
     * @param actual   actual reply
     * @throws JSONException thrown when JSON serialization error occurs
     * @throws IOException   thrown when IO Exception occurs
     */
    @Throws(IOException::class, JSONException::class)
    fun assertRepliesEqual(expected: Reply?, actual: Reply?) {
        assertJsonsEqual(expected, actual)
    }

    /**
     * Creates a reply object with given data
     *
     * @param replyData reply data
     * @return reply object with given data
     */
    fun createReplyWithData(replyData: Map<String, Any>?): Reply {
        return Reply(null, null, null, null, null, null, replyData)
    }

    /**
     * Asserts that table datas equal
     *
     * @param expected expected table data
     * @param actual   actual table data
     */
    fun assertTableDataEquals(expected: Map<String, Any>, actual: Map<String, Any>) {
        Assert.assertNotNull(actual["table"])
        val expectedTableData = expected["table"] as List<Map<String, Any>>
        val actualTableData = actual["table"] as List<Map<String, Any>>
        Assert.assertEquals(expectedTableData.size.toLong(), actualTableData.size.toLong())
        expectedTableData.forEachIndexed { rowIndex, it ->
            it.forEach { tableCell ->
                Assert.assertEquals(tableCell.value, it[tableCell.key])
            }
        }
    }

    /**
     * Creates permission select reply with given value
     *
     * @param value value
     * @return permission select reply with given value
     */
    fun createPermissionSelectReply(value: String): Reply {
        val replyData = createPermissionSelectReplyData(value)
        return createReplyWithData(replyData)
    }

    /**
     * Creates permission select reply data with given value
     *
     * @param value value
     * @return permission select reply data with given value
     */
    fun createPermissionSelectReplyData(value: String): Map<String, Any> {
        val replyData: MutableMap<String, Any> = HashMap()
        replyData["permission-select"] = value
        return replyData
    }
}