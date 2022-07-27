package fi.metatavu.metaform.server.test.functional.tests

import fi.metatavu.metaform.api.client.models.Metaform
import fi.metatavu.metaform.api.client.models.OrderRate
import fi.metatavu.metaform.api.client.models.Reply
import fi.metatavu.metaform.api.client.models.ReplyOrderCriteria
import fi.metatavu.metaform.server.rest.ReplyMode
import fi.metatavu.metaform.server.test.functional.AbstractTest
import fi.metatavu.metaform.server.test.functional.ApiTestSettings.Companion.apiBasePath
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import fi.metatavu.metaform.server.test.functional.builder.resources.KeycloakResource
import fi.metatavu.metaform.server.test.functional.builder.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import io.restassured.RestAssured
import org.apache.commons.lang3.ArrayUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.lang.Boolean
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.Any
import kotlin.Array
import kotlin.Exception
import kotlin.String
import kotlin.Throws
import kotlin.arrayOf

/**
 * Tests that test Metaform replies
 */
@QuarkusTest
@QuarkusTestResource.List(
        QuarkusTestResource(MysqlResource::class),
        QuarkusTestResource(KeycloakResource::class)
)
@TestProfile(GeneralTestProfile::class)
class ReplyTestsIT : AbstractTest() {
    @Test
    @Throws(Exception::class)
    fun createReplyNotLoggedIn() {
        TestBuilder().use { builder ->
            val metaform = builder.metaformAdmin.metaforms.createFromJsonFile("simple")
            RestAssured.given()
                    .baseUri(apiBasePath)
                    .header("Content-Type", "application/json")
                    .post("/v1/metaforms/{metaformId}/replies", metaform.id)
                    .then()
                    .assertThat()
                    .statusCode(401)
        }
    }

    @Test
    @Throws(Exception::class)
    fun createReply() {
        TestBuilder().use { builder ->
            val metaform: Metaform = builder.metaformAdmin.metaforms.createFromJsonFile("simple")
            val replyData: MutableMap<String, Any> = HashMap()
            replyData["text"] = "Test text value"
            val reply: Reply = builder.test1.replies.createReplyWithData(replyData)
            val reply1 = builder.metaformAdmin.replies.create(metaform.id!!, null, ReplyMode.REVISION.toString(), reply)
            val foundReply: Reply = builder.metaformAdmin.replies.findReply(metaform.id, reply1.id!!, null)
            Assertions.assertNotNull(foundReply)
            Assertions.assertNotNull(foundReply.id)
            Assertions.assertNotNull(foundReply.data)
            assertEquals("Test text value", foundReply.data!!["text"])
        }
    }

    @Test
    @Throws(Exception::class)
    fun createReplyUpdateExisting() {
        val builder = TestBuilder()
        val metaform: Metaform = builder.metaformAdmin.metaforms.createFromJsonFile("simple")
        try {
            val replyData1: MutableMap<String, Any> = HashMap()
            replyData1["text"] = "Test text value"
            val reply1: Reply = builder.metaformAdmin.replies.createReplyWithData(replyData1)
            val replyData2: MutableMap<String, Any> = HashMap()
            replyData2["text"] = "Updated text value"
            val reply2: Reply = builder.metaformAdmin.replies.createReplyWithData(replyData2)
            val createdReply1: Reply = builder.metaformAdmin.replies.create(metaform.id!!, null, ReplyMode.UPDATE.toString(), reply1)
            try {
                Assertions.assertNotNull(createdReply1)
                Assertions.assertNotNull(createdReply1.id)
                Assertions.assertNotNull(createdReply1.data)
                assertEquals("Test text value", createdReply1.data!!["text"])
                val createdReply2: Reply = builder.metaformAdmin.replies.create(metaform.id, null, ReplyMode.UPDATE.toString(), reply2)
                Assertions.assertNotNull(createdReply2)
                assertEquals(createdReply1.id, createdReply2.id)
                Assertions.assertNotNull(createdReply2.data)
                assertEquals("Updated text value", createdReply2.data!!["text"])
            } finally {
                builder.metaformAdmin.replies.delete(metaform.id, createdReply1.id!!, null)
            }
        } finally {
            builder.metaformAdmin.metaforms.delete(metaform.id!!)
        }
    }

    @Test
    @Throws(Exception::class)
    fun createReplyVersionExisting() {
        TestBuilder().use { builder ->
            val metaform: Metaform = builder.metaformAdmin.metaforms.createFromJsonFile("simple")
            val replyData1: MutableMap<String, Any> = HashMap()
            replyData1["text"] = "Test text value"
            val reply1: Reply = builder.metaformAdmin.replies.createReplyWithData(replyData1)
            val replyData2: MutableMap<String, Any> = HashMap()
            replyData2["text"] = "Updated text value"
            val reply2: Reply = builder.metaformAdmin.replies.createReplyWithData(replyData2)
            val createdReply1: Reply = builder.test1.replies.create(metaform.id!!, null, ReplyMode.UPDATE.toString(), reply1)
            Assertions.assertNotNull(createdReply1)
            Assertions.assertNotNull(createdReply1.id)
            assertEquals("Test text value", createdReply1.data!!["text"])
            val createdReply2: Reply = builder.test1.replies.create(metaform.id, null, ReplyMode.REVISION.toString(), reply2)
            Assertions.assertNotNull(createdReply2)
            Assertions.assertNotEquals(createdReply1.id, createdReply2.id)
            assertEquals("Updated text value", createdReply2.data!!["text"])
            val replies: List<Reply> = builder.test1.replies.listReplies(metaform.id,
                    REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE,
                    null, null, null, null, null).clone().toList()
            assertEquals(2, replies.size)
            Assertions.assertNotNull(replies[0].revision)
            assertEquals("Test text value", replies[0].data!!["text"])
            Assertions.assertNull(replies[1].revision)
            assertEquals("Updated text value", replies[1].data!!["text"])
        }
    }

    @Test
    @Throws(Exception::class)
    fun createReplyCumulative() {
        TestBuilder().use { builder ->
            val metaform: Metaform = builder.metaformAdmin.metaforms.createFromJsonFile("simple")
            Assertions.assertNotNull(metaform)
            builder.test1.replies.createSimpleReply(metaform.id!!, "val 1", ReplyMode.CUMULATIVE)
            builder.test1.replies.createSimpleReply(metaform.id, "val 2", ReplyMode.CUMULATIVE)
            builder.test1.replies.createSimpleReply(metaform.id, "val 3", ReplyMode.CUMULATIVE)
            val replies: List<Reply> = builder.test1.replies.listReplies(metaform.id, REALM1_USER_1_ID, null,
                    null, null, null, Boolean.TRUE, null, null, null, null, null).clone().toList()
            assertEquals(3, replies.size)
            assertEquals("val 1", replies[0].data!!["text"])
            assertEquals("val 2", replies[1].data!!["text"])
            assertEquals("val 3", replies[2].data!!["text"])
        }
    }

    @Test
    @Throws(Exception::class)
    fun testUpdateReply() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
            val reply = testBuilder.test1.replies.createSimpleReply(metaform.id!!, "test 1", ReplyMode.UPDATE)
            val updateData: MutableMap<String, Any> = HashMap()
            updateData["text"] = "Updated text value"
            val secondReply = Reply(reply.id, reply.userId, reply.revision, reply.ownerKey, reply.createdAt,
                    reply.modifiedAt, updateData)
            testBuilder.test1.replies.updateReply(metaform.id, secondReply.id!!, secondReply, null)
            val foundReply = testBuilder.test1.replies.findReply(metaform.id, reply.id!!, null)
            assertEquals("Updated text value", foundReply.data!!["text"])
        }
    }

    @Test
    @Throws(Exception::class)
    fun listRepliesByTextFields() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("tbnc")
            Assertions.assertNotNull(metaform)
            testBuilder.test1.replies.createTBNCReply(metaform.id!!, "test 1", Boolean.TRUE, 1.0, arrayOf("option 1"))
            testBuilder.test1.replies.createTBNCReply(metaform.id, "test 2", Boolean.FALSE, 2.5, arrayOf("option 2"))
            testBuilder.test1.replies.createTBNCReply(metaform.id, "test 3", null, 0.0, emptyArray())
            val replies1: Array<Reply> = testBuilder.test1.replies.listReplies(metaform.id, REALM1_USER_1_ID, null, null, null, null,
                    Boolean.TRUE, ArrayUtils.toArray("text:test 1"), null, null, null, null)
            val replies2: Array<Reply> = testBuilder.test1.replies.listReplies(metaform.id, REALM1_USER_1_ID, null, null, null, null,
                    Boolean.TRUE, ArrayUtils.toArray("text:test 2"), null, null, null, null)
            val repliesBoth: Array<Reply> = testBuilder.test1.replies.listReplies(metaform.id, REALM1_USER_1_ID, null, null, null, null,
                    Boolean.TRUE, ArrayUtils.toArray("text:test 1", "text:test 2"), null, null, null, null)
            val repliesNone: Array<Reply> = testBuilder.test1.replies.listReplies(metaform.id, REALM1_USER_1_ID, null, null, null, null,
                    Boolean.TRUE, ArrayUtils.toArray("text:non", "text:existing"), null, null, null, null)
            val notReplies: Array<Reply> = testBuilder.test1.replies.listReplies(metaform.id, REALM1_USER_1_ID, null, null, null, null,
                    Boolean.TRUE, ArrayUtils.toArray("text^test 1"), null, null, null, null)
            assertEquals(1, replies1.size)
            assertEquals("test 1", replies1[0].data!!["text"])
            assertEquals(1, replies2.size)
            assertEquals("test 2", replies2[0].data!!["text"])
            assertEquals(0, repliesBoth.size)
            assertEquals(2, notReplies.size)
            assertEquals("test 2", notReplies[0].data!!["text"])
            assertEquals("test 3", notReplies[1].data!!["text"])
            assertEquals(0, repliesNone.size)
        }
    }

    @Test
    @Throws(Exception::class)
    fun listRepliesByListFields() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("tbnc")
            Assertions.assertNotNull(metaform)
            testBuilder.test1.replies.createTBNCReply(metaform.id!!, "test 1", Boolean.TRUE, 1.0, arrayOf("option 1"))
            testBuilder.test1.replies.createTBNCReply(metaform.id, "test 2", Boolean.FALSE, 2.5, arrayOf("option 2"))
            testBuilder.test1.replies.createTBNCReply(metaform.id, "test 3", null, 0.0, emptyArray())
            val replies1: Array<Reply> = testBuilder.test1.replies.listReplies(metaform.id, REALM1_USER_1_ID, null, null, null, null,
                    Boolean.TRUE, ArrayUtils.toArray("checklist:option 1"), null, null, null, null)
            val replies2: Array<Reply> = testBuilder.test1.replies.listReplies(metaform.id, REALM1_USER_1_ID, null, null, null, null,
                    Boolean.TRUE, ArrayUtils.toArray("checklist:option 2"), null, null, null, null)
            val repliesBoth: Array<Reply> = testBuilder.test1.replies.listReplies(metaform.id, REALM1_USER_1_ID, null, null, null, null,
                    Boolean.TRUE, ArrayUtils.toArray("checklist:option 1", "checklist:option 2"), null, null, null, null)
            val repliesNone: Array<Reply> = testBuilder.test1.replies.listReplies(metaform.id, REALM1_USER_1_ID, null, null, null, null,
                    Boolean.TRUE, ArrayUtils.toArray("checklist:non", "checklist:existing"), null, null, null, null)
            val notReplies: Array<Reply> = testBuilder.test1.replies.listReplies(metaform.id, REALM1_USER_1_ID, null, null, null, null,
                    Boolean.TRUE, ArrayUtils.toArray("checklist^option 1"), null, null, null, null)
            assertEquals(1, replies1.size)
            assertEquals("test 1", replies1[0].data!!["text"])
            assertEquals(1, replies2.size)
            assertEquals("test 2", replies2[0].data!!["text"])
            assertEquals(0, repliesBoth.size)
            assertEquals(0, repliesNone.size)
            assertEquals(2, notReplies.size)
            assertEquals("test 2", notReplies[0].data!!["text"])
            assertEquals("test 3", notReplies[1].data!!["text"])
        }
    }

    @Test
    @Throws(Exception::class)
    fun listRepliesByNumberFields() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("tbnc")
            Assertions.assertNotNull(metaform)
            testBuilder.test1.replies.createTBNCReply(metaform.id!!, "test 1", Boolean.TRUE, 1.0, arrayOf("option 1"))
            testBuilder.test1.replies.createTBNCReply(metaform.id, "test 2", Boolean.FALSE, 2.5, arrayOf("option 2"))
            testBuilder.test1.replies.createTBNCReply(metaform.id, "test 3", null, 0.0, emptyArray())
            val replies1: Array<Reply> = testBuilder.test1.replies.listReplies(metaform.id, REALM1_USER_1_ID, null, null, null, null,
                    Boolean.TRUE, ArrayUtils.toArray("number:1"), null, null, null, null)
            val replies2: Array<Reply> = testBuilder.test1.replies.listReplies(metaform.id, REALM1_USER_1_ID, null, null, null, null,
                    Boolean.TRUE, ArrayUtils.toArray("number:2.5"), null, null, null, null)
            val repliesBoth: Array<Reply> = testBuilder.test1.replies.listReplies(metaform.id, REALM1_USER_1_ID, null, null, null, null,
                    Boolean.TRUE, ArrayUtils.toArray("number:1", "number:2.5"), null, null, null, null)
            val repliesNone: Array<Reply> = testBuilder.test1.replies.listReplies(metaform.id, REALM1_USER_1_ID, null, null, null, null,
                    Boolean.TRUE, ArrayUtils.toArray("number:55", "number:66"), null, null, null, null)
            val notReplies: Array<Reply> = testBuilder.test1.replies.listReplies(metaform.id, REALM1_USER_1_ID, null, null, null, null,
                    Boolean.TRUE, ArrayUtils.toArray("number^1"), null, null, null, null)
            assertEquals(1, replies1.size)
            assertEquals("test 1", replies1[0].data!!["text"])
            assertEquals(1, replies2.size)
            assertEquals("test 2", replies2[0].data!!["text"])
            assertEquals(0, repliesBoth.size)
            assertEquals(0, repliesNone.size)
            assertEquals(2, notReplies.size)
            assertEquals("test 2", notReplies[0].data!!["text"])
            assertEquals("test 3", notReplies[1].data!!["text"])
        }
    }

    @Test
    @Throws(Exception::class)
    fun listRepliesByBooleanFields() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("tbnc")
            Assertions.assertNotNull(metaform)
            testBuilder.test1.replies.createTBNCReply(metaform.id!!, "test 1", Boolean.TRUE, 1.0, arrayOf("option 1"))
            testBuilder.test1.replies.createTBNCReply(metaform.id, "test 2", Boolean.FALSE, 2.5, arrayOf("option 2"))
            testBuilder.test1.replies.createTBNCReply(metaform.id, "test 3", null, 0.0, emptyArray())
            val replies1: Array<Reply> = testBuilder.test1.replies.listReplies(metaform.id, REALM1_USER_1_ID, null, null, null, null,
                    Boolean.TRUE, ArrayUtils.toArray("boolean:true"), null, null, null, null)
            val replies2: Array<Reply> = testBuilder.test1.replies.listReplies(metaform.id, REALM1_USER_1_ID, null, null, null, null,
                    Boolean.TRUE, ArrayUtils.toArray("boolean:false"), null, null, null, null)
            val notReplies: Array<Reply> = testBuilder.test1.replies.listReplies(metaform.id, REALM1_USER_1_ID, null, null, null, null,
                    Boolean.TRUE, ArrayUtils.toArray("boolean^false"), null, null, null, null)
            assertEquals(1, replies1.size)
            assertEquals("test 1", replies1[0].data!!["text"])
            assertEquals(1, replies2.size)
            assertEquals("test 2", replies2[0].data!!["text"])
            assertEquals(2, notReplies.size)
            assertEquals("test 1", notReplies[0].data!!["text"])
            assertEquals("test 3", notReplies[1].data!!["text"])
        }
    }

    @Test
    @Throws(Exception::class)
    fun listRepliesByMultiFields() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("tbnc")
            Assertions.assertNotNull(metaform)
            testBuilder.test1.replies.createTBNCReply(metaform.id!!, "test 1", Boolean.TRUE, 1.0, arrayOf("option 1"))
            testBuilder.test1.replies.createTBNCReply(metaform.id, "test 2", Boolean.FALSE, 2.5, arrayOf("option 2"))
            testBuilder.test1.replies.createTBNCReply(metaform.id, "test 3", null, 0.0, emptyArray())
            val replies1: Array<Reply> = testBuilder.test1.replies.listReplies(metaform.id, REALM1_USER_1_ID, null, null, null, null,
                    Boolean.TRUE, ArrayUtils.toArray("boolean:true", "number:1"), null, null, null, null)
            val replies2: Array<Reply> = testBuilder.test1.replies.listReplies(metaform.id, REALM1_USER_1_ID, null, null, null, null,
                    Boolean.TRUE, ArrayUtils.toArray("boolean:false", "number:1"), null, null, null, null)
            val replies3: Array<Reply> = testBuilder.test1.replies.listReplies(metaform.id, REALM1_USER_1_ID, null, null, null, null,
                    Boolean.TRUE, ArrayUtils.toArray("checklist:option 1", "boolean:true"), null, null, null, null)
            val replies4: Array<Reply> = testBuilder.test1.replies.listReplies(metaform.id, REALM1_USER_1_ID, null, null, null, null,
                    Boolean.TRUE, ArrayUtils.toArray("checklist^option 1", "boolean:false"), null, null, null, null)
            assertEquals(1, replies1.size)
            assertEquals("test 1", replies1[0].data!!["text"])
            assertEquals(0, replies2.size)
            assertEquals(1, replies3.size)
            assertEquals("test 1", replies3[0].data!!["text"])
            assertEquals(1, replies4.size)
            assertEquals("test 2", replies4[0].data!!["text"])
        }
    }

    /**
     * Updates reply to be created at specific time
     *
     * @param reply   reply
     * @param created created
     */
    private fun updateReplyCreated(reply: Reply, created: OffsetDateTime) {
        executeUpdate("UPDATE Reply SET createdAt = ? WHERE id = ?", created, reply.id!!)
        flushCache()
    }

    /**
     * Updates reply to be modified at specific time
     *
     * @param reply    reply
     * @param modified created
     */
    private fun updateReplyModified(reply: Reply, modified: OffsetDateTime) {
        executeUpdate("UPDATE Reply SET modifiedAt = ? WHERE id = ?", modified, reply.id!!)
        flushCache()
    }

    @Test
    @Throws(Exception::class)
    fun listRepliesByCreatedBefore() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
            val reply1: Reply = testBuilder.test1.replies.createSimpleReply(metaform.id!!, "test 1", ReplyMode.CUMULATIVE)
            val reply2: Reply = testBuilder.test1.replies.createSimpleReply(metaform.id, "test 2", ReplyMode.CUMULATIVE)
            val reply3: Reply = testBuilder.test1.replies.createSimpleReply(metaform.id, "test 3", ReplyMode.CUMULATIVE)
            updateReplyCreated(reply1, getOffsetDateTime(2018, 5, 25, TIMEZONE))
            updateReplyCreated(reply2, getOffsetDateTime(2018, 5, 27, TIMEZONE))
            updateReplyCreated(reply3, getOffsetDateTime(2018, 5, 29, TIMEZONE))
            val allReplies: Array<Reply> = testBuilder.test1.replies.listReplies(metaform.id, REALM1_USER_1_ID,
                    null, null, null, null,
                    Boolean.TRUE, null, null, null, null, null)
            val createdBefore26: Array<Reply> = testBuilder.test1.replies.listReplies(metaform.id, REALM1_USER_1_ID,
                    getIsoDateTime(2018, 5, 26, TIMEZONE), null, null,
                    null, Boolean.FALSE, null, null, null, null, null)
            val createdAfter26: Array<Reply> = testBuilder.test1.replies.listReplies(metaform.id, REALM1_USER_1_ID,
                    null, getIsoDateTime(2018, 5, 26, TIMEZONE), null,
                    null, Boolean.FALSE, null, null, null, null, null)
            assertEquals(3, allReplies.size)
            assertEquals("test 1", allReplies[0].data!!["text"])
            assertEquals("test 2", allReplies[1].data!!["text"])
            assertEquals("test 3", allReplies[2].data!!["text"])
            assertEquals(1, createdBefore26.size)
            assertEquals("test 1", createdBefore26[0].data!!["text"])
            assertEquals(2, createdAfter26.size)
            assertEquals("test 2", createdAfter26[0].data!!["text"])
            assertEquals("test 3", createdAfter26[1].data!!["text"])
        }
    }

    @Test
    @Throws(Exception::class)
    fun listRepliesByModifiedBefore() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
            val reply1: Reply = testBuilder.test1.replies.createSimpleReply(metaform.id!!, "test 1", ReplyMode.CUMULATIVE)
            val reply2: Reply = testBuilder.test1.replies.createSimpleReply(metaform.id, "test 2", ReplyMode.CUMULATIVE)
            val reply3: Reply = testBuilder.test1.replies.createSimpleReply(metaform.id, "test 3", ReplyMode.CUMULATIVE)
            updateReplyModified(reply1, getOffsetDateTime(2018, 5, 25, TIMEZONE))
            updateReplyModified(reply2, getOffsetDateTime(2018, 5, 27, TIMEZONE))
            updateReplyModified(reply3, getOffsetDateTime(2018, 5, 29, TIMEZONE))
            val allReplies: Array<Reply> = testBuilder.test1.replies.listReplies(metaform.id, REALM1_USER_1_ID, null, null, null, null, Boolean.FALSE, null, null, null, null, null)
            val modifiedBefore26: Array<Reply> = testBuilder.test1.replies.listReplies(metaform.id, REALM1_USER_1_ID, null, null, getIsoDateTime(2018, 5, 26, TIMEZONE), null, Boolean.FALSE, null, null, null, null, null)
            val modifiedAfter26: Array<Reply> = testBuilder.test1.replies.listReplies(metaform.id, REALM1_USER_1_ID, null, null, null, getIsoDateTime(2018, 5, 26, TIMEZONE), Boolean.FALSE, null, null, null, null, null)
            assertEquals(3, allReplies.size.toLong())
            assertEquals("test 1", allReplies[0].data!!["text"])
            assertEquals("test 2", allReplies[1].data!!["text"])
            assertEquals("test 3", allReplies[2].data!!["text"])
            assertEquals(1, modifiedBefore26.size.toLong())
            assertEquals("test 1", modifiedBefore26[0].data!!["text"])
            assertEquals(2, modifiedAfter26.size.toLong())
            assertEquals("test 2", modifiedAfter26[0].data!!["text"])
            assertEquals("test 3", modifiedAfter26[1].data!!["text"])
        }
    }

    @Test
    @Throws(Exception::class)
    fun testMetafields() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple-meta")
            val reply = testBuilder.test1.replies.createSimpleReply(metaform.id!!, "test 1", ReplyMode.CUMULATIVE)
            val parsedCreated = OffsetDateTime.parse(reply.createdAt)
            val parsedModified = OffsetDateTime.parse(reply.modifiedAt)
            val foundReply = testBuilder.test1.replies.findReply(metaform.id, reply.id!!, null)
            assertEquals(parsedCreated.truncatedTo(ChronoUnit.MINUTES).toInstant(), parseOffsetDateTime(foundReply.data!!["created"] as String).truncatedTo(ChronoUnit.MINUTES).toInstant())
            assertEquals(parsedModified.truncatedTo(ChronoUnit.MINUTES).toInstant(), parseOffsetDateTime(foundReply.data["modified"] as String).truncatedTo(ChronoUnit.MINUTES).toInstant())
            assertEquals(REALM1_USER_1_ID.toString(), foundReply.data["lastEditor"])
        }
    }

    @Test
    @Throws(Exception::class)
    fun testFindReplyOwnerKeys() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple-owner-keys")
            val reply1: Reply = testBuilder.test1.replies.createSimpleReply(metaform.id!!, "test 1", ReplyMode.CUMULATIVE)
            val reply2: Reply = testBuilder.test1.replies.createSimpleReply(metaform.id, "test 2", ReplyMode.CUMULATIVE)
            Assertions.assertNotNull(reply1.ownerKey)
            Assertions.assertNotNull(reply2.ownerKey)
            Assertions.assertNotEquals(reply1.ownerKey, reply2.ownerKey)
            testBuilder.anonymousToken.replies.findReply(metaform.id, reply1.id!!, reply1.ownerKey)
            testBuilder.anonymousToken.replies.assertReplyOwnerKeyFindForbidden(metaform.id, reply1.id, null)
            testBuilder.anonymousToken.replies.assertReplyOwnerKeyFindForbidden(metaform.id, reply1.id, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgZEinFR6yjNnV4utVbvU9KQ8lZasbAWKQJXjp2VzcyQfE1WvH5dMJmI-sgmPyaZFcJLk9tgLfZilytGmeopfafkWp7yVa9zWYAfni0eJmCL9EcemRb_rDNw07Mf1Vb1lbLhLth8r6a2SVGk_dK14TXeGQSX9zROmiqjeT_FXe2Yz8EnvUweLQ5TobVE-azzyW0dqzjoSkBNfZ8r2hot4hQ2mQthU5xaAnOfDeCc95E7cci4-Dnx3B8U_UaHOn7Srf6DdsL_ZDvnSh1CvYiGNOYMpk-TLK6ixH-m83rweBjQ1hl1N_5I3cplmuJGCJLBxDauyjfBYIfR9WkBoau1eDQIDAQAB")
            testBuilder.anonymousToken.replies.assertReplyOwnerKeyFindForbidden(metaform.id, reply1.id, reply2.ownerKey)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testUpdateReplyOwnerKeys() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple-owner-keys")
            val reply1: Reply = testBuilder.test1.replies.createSimpleReply(metaform.id!!, "test 1", ReplyMode.CUMULATIVE)
            val reply2: Reply = testBuilder.test1.replies.createSimpleReply(metaform.id, "test 2", ReplyMode.CUMULATIVE)
            Assertions.assertNotNull(reply1.ownerKey)
            Assertions.assertNotNull(reply2.ownerKey)
            Assertions.assertNotEquals(reply1.ownerKey, reply2.ownerKey)
            testBuilder.anonymousToken.replies.updateReply(metaform.id, reply1.id!!, reply1, reply1.ownerKey)
            testBuilder.anonymousToken.replies.assertReplyOwnerKeyFindForbidden(metaform.id, reply1.id, null)
            testBuilder.anonymousToken.replies.assertReplyOwnerKeyFindForbidden(metaform.id, reply1.id, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgZEinFR6yjNnV4utVbvU9KQ8lZasbAWKQJXjp2VzcyQfE1WvH5dMJmI-sgmPyaZFcJLk9tgLfZilytGmeopfafkWp7yVa9zWYAfni0eJmCL9EcemRb_rDNw07Mf1Vb1lbLhLth8r6a2SVGk_dK14TXeGQSX9zROmiqjeT_FXe2Yz8EnvUweLQ5TobVE-azzyW0dqzjoSkBNfZ8r2hot4hQ2mQthU5xaAnOfDeCc95E7cci4-Dnx3B8U_UaHOn7Srf6DdsL_ZDvnSh1CvYiGNOYMpk-TLK6ixH-m83rweBjQ1hl1N_5I3cplmuJGCJLBxDauyjfBYIfR9WkBoau1eDQIDAQAB")
            testBuilder.anonymousToken.replies.assertReplyOwnerKeyFindForbidden(metaform.id, reply1.id, reply2.ownerKey)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testDeleteReplyOwnerKeys() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple-owner-keys")
            val reply1: Reply = testBuilder.test1.replies.createSimpleReply(metaform.id!!, "test 1", ReplyMode.CUMULATIVE)
            val reply2: Reply = testBuilder.test1.replies.createSimpleReply(metaform.id, "test 2", ReplyMode.CUMULATIVE)
            Assertions.assertNotNull(reply1.ownerKey)
            Assertions.assertNotNull(reply2.ownerKey)
            Assertions.assertNotEquals(reply1.ownerKey, reply2.ownerKey)
            testBuilder.anonymousToken.replies.assertReplyOwnerKeyFindForbidden(metaform.id, reply1.id!!, null)
            testBuilder.anonymousToken.replies.assertReplyOwnerKeyFindForbidden(metaform.id, reply1.id,
                    "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgZEinFR6yjNnV4utVbvU9KQ8lZasbAWKQJXjp2VzcyQfE1WvH5dMJmI-sgmPyaZFcJLk9tgLfZilytGmeopfafkWp7yVa9zWYAfni0eJmCL9EcemRb_rDNw07Mf1Vb1lbLhLth8r6a2SVGk_dK14TXeGQSX9zROmiqjeT_FXe2Yz8EnvUweLQ5TobVE-azzyW0dqzjoSkBNfZ8r2hot4hQ2mQthU5xaAnOfDeCc95E7cci4-Dnx3B8U_UaHOn7Srf6DdsL_ZDvnSh1CvYiGNOYMpk-TLK6ixH-m83rweBjQ1hl1N_5I3cplmuJGCJLBxDauyjfBYIfR9WkBoau1eDQIDAQAB")
            testBuilder.anonymousToken.replies.assertReplyOwnerKeyFindForbidden(metaform.id, reply1.id, reply2.ownerKey)
            testBuilder.anonymousToken.replies.delete(metaform.id, reply1.id, reply1.ownerKey)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testExportReplyPdf() {
        TestBuilder().use { testBuilder ->
            val exportTheme = testBuilder.metaformSuper.exportThemes.createSimpleExportTheme()
            testBuilder.metaformSuper.exportFiles.createSimpleExportThemeFile(exportTheme.id!!, "reply/pdf.ftl", "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></meta><title>title</title></head><body>content</body></html>")
            val metaform: Metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
            val newMetaform = Metaform(metaform.id, metaform.replyStrategy, exportTheme.id, metaform.allowAnonymous,
                    metaform.allowDrafts, metaform.allowReplyOwnerKeys, metaform.allowInvitations, metaform.autosave,
                    metaform.title, metaform.slug, metaform.sections, metaform.filters, metaform.scripts)
            testBuilder.metaformAdmin.metaforms.updateMetaform(newMetaform.id!!, newMetaform)
            val reply: Reply = testBuilder.test1.replies.createSimpleReply(metaform.id!!, "test 1", ReplyMode.UPDATE)
            assertPdfDownloadStatus(200, testBuilder.metaformAdmin.token, metaform, reply)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testExportReplyPdfFilesEmpty() {
        TestBuilder().use { testBuilder ->
            val exportTheme = testBuilder.metaformSuper.exportThemes.createSimpleExportTheme()
            testBuilder.metaformSuper.exportFiles.createSimpleExportThemeFile(exportTheme.id!!, "reply/pdf.ftl", "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></meta><title>title</title></head><body>content</body></html>")
            val metaform: Metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple-files")
            val newMetaform = Metaform(metaform.id, metaform.replyStrategy, exportTheme.id, metaform.allowAnonymous,
                    metaform.allowDrafts, metaform.allowReplyOwnerKeys, metaform.allowInvitations, metaform.autosave,
                    metaform.title, metaform.slug, metaform.sections, metaform.filters, metaform.scripts)
            testBuilder.metaformAdmin.metaforms.updateMetaform(newMetaform.id!!, newMetaform)
            val reply: Reply = testBuilder.test1.replies.createSimpleReply(metaform.id!!, "test 1", ReplyMode.UPDATE)
            assertPdfDownloadStatus(200, testBuilder.metaformAdmin.token, metaform, reply)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testReplyOrdering() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
            val r1 = testBuilder.test1.replies.createSimpleReply(metaform.id!!, "ordering-test-1", ReplyMode.CUMULATIVE)
            val r2 = testBuilder.test1.replies.createSimpleReply(metaform.id, "ordering-test-2", ReplyMode.CUMULATIVE)
            val r3 = testBuilder.test1.replies.createSimpleReply(metaform.id, "ordering-test-3", ReplyMode.CUMULATIVE)
            println(metaform.id)
            // reorder them by date [1, 2, 3] -> [2, 3, 1]
            updateReplyCreated(r1, getOffsetDateTime(1978, 11, 12, TIMEZONE))
            updateReplyCreated(r3, getOffsetDateTime(1867, 12, 8, TIMEZONE))
            updateReplyCreated(r2, getOffsetDateTime(1582, 9, 2, TIMEZONE))

            val replies = testBuilder.test1.replies.listReplies(metaform.id, null, null, null, null, null, null, null, null, null, ReplyOrderCriteria.cREATED, OrderRate.aSCENDING)
            assertEquals(replies[0].data!!["text"], "ordering-test-2")
            assertEquals(replies[1].data!!["text"], "ordering-test-3")
            assertEquals(replies[2].data!!["text"], "ordering-test-1")
        }
    }

    companion object {
        private val TIMEZONE = ZoneId.of("Europe/Helsinki")
    }
}