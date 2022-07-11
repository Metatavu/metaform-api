package fi.metatavu.metaform.server.test.functional.tests

import fi.metatavu.metaform.api.client.models.Metaform
import fi.metatavu.metaform.api.client.models.Reply
import fi.metatavu.metaform.server.rest.ReplyMode
import fi.metatavu.metaform.server.test.functional.AbstractTest
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import fi.metatavu.metaform.server.test.functional.builder.resources.KeycloakResource
import fi.metatavu.metaform.server.test.functional.builder.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Tests that test the table fields of metaforms
 */
@QuarkusTest
@QuarkusTestResource.List(
        QuarkusTestResource(MysqlResource::class),
        QuarkusTestResource(KeycloakResource::class)
)
@TestProfile(GeneralTestProfile::class)
class TableFieldTestsIT : AbstractTest() {
    @Test
    @Throws(Exception::class)
    fun createTableReply() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple-table")
            val tableData: List<Map<String, Any>> = listOf(createSimpleTableRow("Text 1", 10.0), createSimpleTableRow("Text 2", 20.0))
            val replyData: MutableMap<String, Any> = HashMap()
            replyData["table"] = tableData
            val reply: Reply = testBuilder.test1.replies.createReplyWithData(replyData)
            val reply1: Reply = testBuilder.test1.replies.create(metaform.id!!, null, ReplyMode.REVISION.toString(), reply)
            val foundReply: Reply = testBuilder.test1.replies.findReply(metaform.id, reply1.id!!, null)
            Assertions.assertNotNull(foundReply)
            Assertions.assertNotNull(foundReply.id)
            testBuilder.test1.replies.assertTableDataEquals(replyData, foundReply.data!!)
        }
    }

    @Test
    @Throws(Exception::class)
    fun updateTableReply() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple-table")
            val createReplyData: MutableMap<String, Any> = HashMap()
            createReplyData["table"] = listOf(createSimpleTableRow("Text 1", 10.0), createSimpleTableRow("Text 2", 20.0))
            val reply: Reply = testBuilder.test1.replies.createReplyWithData(createReplyData)
            val reply1: Reply = testBuilder.test1.replies.create(metaform.id!!, null, ReplyMode.REVISION.toString(), reply)
            testBuilder.test1.replies.assertTableDataEquals(createReplyData, reply1.data!!)
            val foundReply: Reply = testBuilder.test1.replies.findReply(metaform.id, reply1.id!!, null)
            Assertions.assertNotNull(foundReply)
            Assertions.assertNotNull(foundReply.id)
            testBuilder.test1.replies.assertTableDataEquals(createReplyData, foundReply.data!!)
            val updateReplyData: MutableMap<String, Any> = HashMap()
            updateReplyData["table"] = listOf(createSimpleTableRow("Added new text", -210.0), createSimpleTableRow("Text 1", 10.0), createSimpleTableRow("Updated Text 2", 45.5))
            val updateReply: Reply = testBuilder.test1.replies.createReplyWithData(updateReplyData)
            testBuilder.test1.replies.updateReply(metaform.id, reply1.id, updateReply, null)
            testBuilder.test1.replies.assertTableDataEquals(updateReplyData, updateReply.data!!)
            val foundUpdatedReply: Reply = testBuilder.test1.replies.findReply(metaform.id, reply1.id, null)
            Assertions.assertNotNull(foundUpdatedReply)
            Assertions.assertNotNull(foundUpdatedReply.id)
            testBuilder.test1.replies.assertTableDataEquals(updateReplyData, foundUpdatedReply.data!!)
        }
    }

    @Test
    @Throws(Exception::class)
    fun nulledTableReply() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple-table")
            val tableData: List<Map<String, Any>> = listOf(createSimpleTableRow(null, null))
            val replyData: MutableMap<String, Any> = HashMap()
            replyData["table"] = tableData
            val reply: Reply = testBuilder.test1.replies.createReplyWithData(replyData)
            val reply1 = testBuilder.test1.replies.create(metaform.id!!, null, ReplyMode.REVISION.toString(), reply)
            val foundReply: Reply = testBuilder.test1.replies.findReply(metaform.id, reply1.id!!, null)
            Assertions.assertNotNull(foundReply)
            Assertions.assertNotNull(foundReply.id)
            testBuilder.test1.replies.assertTableDataEquals(replyData, foundReply.data!!)
        }
    }

    @Test
    @Throws(Exception::class)
    fun invalidTableReply() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple-table")
            val replyData: MutableMap<String, Any> = HashMap()
            replyData["table"] = "table data"
            val reply: Reply = testBuilder.test1.replies.createReplyWithData(replyData)
            testBuilder.test1.replies.assertCreateFailStatus(400, metaform.id!!, null, ReplyMode.REVISION.toString(), reply)
        }
    }

    @Test
    @Throws(Exception::class)
    fun deleteTableReply() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple-table")
            val tableData: List<Map<String, Any>> = listOf(createSimpleTableRow("Text 1", 10.0), createSimpleTableRow("Text 2", 20.0))
            val replyData: MutableMap<String, Any> = HashMap()
            replyData["table"] = tableData
            val reply: Reply = testBuilder.test1.replies.createReplyWithData(replyData)
            val createdReply: Reply = testBuilder.test1.replies.create(metaform.id!!, null, ReplyMode.REVISION.toString(), reply)
            val foundReply: Reply = testBuilder.test1.replies.findReply(metaform.id, createdReply.id!!, null)
            Assertions.assertNotNull(foundReply)
            testBuilder.metaformAdmin.replies.delete(metaform.id, createdReply.id, null)
            testBuilder.test1.replies.assertFindFailStatus(404, metaform.id, createdReply.id, null)
        }
    }
}