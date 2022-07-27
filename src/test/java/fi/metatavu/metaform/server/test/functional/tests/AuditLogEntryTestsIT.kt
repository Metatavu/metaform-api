package fi.metatavu.metaform.server.test.functional.tests

import fi.metatavu.metaform.api.client.models.AuditLogEntry
import fi.metatavu.metaform.api.client.models.Metaform
import fi.metatavu.metaform.api.client.models.Reply
import fi.metatavu.metaform.server.rest.ReplyMode
import fi.metatavu.metaform.server.test.functional.AbstractTest
import fi.metatavu.metaform.server.test.functional.builder.PermissionScope
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import fi.metatavu.metaform.server.test.functional.builder.auth.TestBuilderAuthentication
import fi.metatavu.metaform.server.test.functional.builder.resources.KeycloakResource
import fi.metatavu.metaform.server.test.functional.builder.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * Tests for AuditLogEntriesApi
 *
 * @author Katja Danilova
 */
@QuarkusTest
@QuarkusTestResource.List(
        QuarkusTestResource(MysqlResource::class),
        QuarkusTestResource(KeycloakResource::class)
)
@TestProfile(GeneralTestProfile::class)
class AuditLogEntryTestsIT : AbstractTest() {
    @Test
    @Throws(Exception::class)
    fun basicActionsOnReply() {
        TestBuilder().use { builder ->
            val metaform: Metaform = builder.systemAdmin.metaforms.createFromJsonFile("simple")
            val replyData: MutableMap<String, Any> = HashMap()
            replyData["text"] = "Test text value"
            val createdReply: Reply = builder.test1.replies.createReplyWithData(replyData)
            val reply: Reply = builder.test1.replies.create(metaform.id!!, null, ReplyMode.REVISION.toString(), createdReply)
            builder.test1.replies.findReply(metaform.id, reply.id!!, null)
            builder.test1.replies.listReplies(metaform.id, null, null, null, null, null, true, null, null, null)
            builder.test1.replies.updateReply(metaform.id, reply.id, reply, reply.ownerKey)
            builder.test1.replies.delete(metaform.id, reply.id, reply.ownerKey)
            val auditLogEntries: Array<AuditLogEntry> = builder.test1.auditLogs.listAuditLogEntries(metaform.id, null, reply.id, null, null)
            Assertions.assertEquals(5, auditLogEntries.size)
            Assertions.assertEquals(java.lang.String.format("user %s created reply %s", REALM1_USER_1_ID, reply.id), auditLogEntries[0].message)
            Assertions.assertEquals(java.lang.String.format("user %s viewed reply %s", REALM1_USER_1_ID, reply.id), auditLogEntries[1].message)
            Assertions.assertEquals(java.lang.String.format("user %s listed reply %s", REALM1_USER_1_ID, reply.id), auditLogEntries[2].message)
            Assertions.assertEquals(java.lang.String.format("user %s modified reply %s", REALM1_USER_1_ID, reply.id), auditLogEntries[3].message)
            Assertions.assertEquals(java.lang.String.format("user %s deleted reply %s", REALM1_USER_1_ID, reply.id), auditLogEntries[4].message)
        }
    }

    @Test
    @Throws(Exception::class)
    fun queryByUser() {
        TestBuilder().use { builder ->
            val metaform: Metaform = builder.systemAdmin.metaforms.createFromJsonFile("simple")
            val replyData: MutableMap<String, Any> = HashMap()
            replyData["text"] = "Test text value"
            val replyWithData: Reply = builder.test1.replies.createReplyWithData(replyData)
            val reply1 = builder.test1.replies.create(metaform.id!!, null, ReplyMode.REVISION.toString(), replyWithData)
            val reply2 = builder.test2.replies.create(metaform.id, null, ReplyMode.REVISION.toString(), replyWithData)
            val auditLogEntriesForUser1: Array<AuditLogEntry> = builder.test1.auditLogs.listAuditLogEntries(metaform.id, REALM1_USER_1_ID, null, null, null)
            val auditLogEntriesForUser2: Array<AuditLogEntry> = builder.test1.auditLogs.listAuditLogEntries(metaform.id, REALM1_USER_2_ID, null, null, null)
            Assertions.assertEquals(java.lang.String.format("user %s created reply %s", REALM1_USER_1_ID, reply1.id),
                    auditLogEntriesForUser1[0].message)
            Assertions.assertEquals(java.lang.String.format("user %s created reply %s", REALM1_USER_2_ID, reply2.id),
                    auditLogEntriesForUser2[0].message)
        }
    }

    @Test
    @Throws(Exception::class)
    fun queryByMetaform() {
        TestBuilder().use { builder ->
            val metaform1: Metaform = builder.systemAdmin.metaforms.createFromJsonFile("simple")
            val metaform2: Metaform = builder.systemAdmin.metaforms.createFromJsonFile("simple")
            val replyData: MutableMap<String, Any> = HashMap()
            replyData["text"] = "Test text value"
            val replyWithData: Reply = builder.test1.replies.createReplyWithData(replyData)
            val reply1 = builder.test1.replies.create(metaform1.id!!, null, ReplyMode.REVISION.toString(), replyWithData)
            val reply2 = builder.test1.replies.create(metaform2.id!!, null, ReplyMode.REVISION.toString(), replyWithData)
            val metaform1AuditLogs: Array<AuditLogEntry> = builder.test1.auditLogs.listAuditLogEntries(metaform1.id, null, null, null, null)
            val metaform2AuditLogs: Array<AuditLogEntry> = builder.test1.auditLogs.listAuditLogEntries(metaform2.id, null, null, null, null)
            Assertions.assertEquals(1, metaform1AuditLogs.size)
            Assertions.assertEquals(1, metaform2AuditLogs.size)
            Assertions.assertEquals(java.lang.String.format("user %s created reply %s", REALM1_USER_1_ID, reply1.id), metaform1AuditLogs[0].message)
            Assertions.assertEquals(java.lang.String.format("user %s created reply %s", REALM1_USER_1_ID, reply2.id), metaform2AuditLogs[0].message)
        }
    }

    /**
     * test verifies that sorting by reply id words
     *
     * @throws IOException
     */
    @Test
    @Throws(Exception::class)
    fun queryByReplyId() {
        TestBuilder().use { builder ->
            val metaform: Metaform = builder.systemAdmin.metaforms.createFromJsonFile("simple")
            val replyData: MutableMap<String, Any> = HashMap()
            replyData["text"] = "Test text value"
            val replyWithData1: Reply = builder.test1.replies.createReplyWithData(replyData)
            val replyWithData2: Reply = builder.test1.replies.createReplyWithData(replyData)
            val reply3 = builder.test1.replies.create(metaform.id!!, null, ReplyMode.REVISION.toString(), replyWithData1)
            builder.test1.replies.create(metaform.id, null, ReplyMode.REVISION.toString(), replyWithData2)
            val auditLogEntries: Array<AuditLogEntry> = builder.test1.auditLogs.listAuditLogEntries(metaform.id, null, null, null, null)
            Assertions.assertEquals(2, auditLogEntries.size)
            val entryByReply: Array<AuditLogEntry> = builder.test1.auditLogs.listAuditLogEntries(metaform.id, null, reply3.id, null, null)
            Assertions.assertEquals(1, entryByReply.size)
            Assertions.assertEquals(java.lang.String.format("user %s created reply %s", REALM1_USER_1_ID, reply3.id), entryByReply[0].message)
        }
    }

    @Test
    @Throws(Exception::class)
    fun accessRights() {
        TestBuilder().use { builder ->
            val metaform: Metaform = builder.systemAdmin.metaforms.createFromJsonFile("simple")
            val replyData: MutableMap<String, Any> = HashMap()
            replyData["text"] = "Test text value"
            val reply1: Reply = builder.test1.replies.createReplyWithData(replyData)
            builder.test1.replies.create(metaform.id!!, null, ReplyMode.REVISION.toString(), reply1)
            val auditLogEntries: Array<AuditLogEntry> = builder.test1.auditLogs.listAuditLogEntries(metaform.id, null, null, null, null)
            Assertions.assertNotNull(auditLogEntries)
            builder.test2.auditLogs.assertListFailStatus(403, metaform.id, null, null, null, null)
        }
    }

    @Test
    @Throws(Exception::class)
    fun listAuditLogEntryPermission() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")

            testBuilder.permissionTestByScopes(
                scope = PermissionScope.METAFORM_ADMIN,
                apiCaller = { authentication: TestBuilderAuthentication, _: Int ->
                    authentication.auditLogs.listAuditLogEntries(
                        metaformId = metaform.id!!,
                        userId = null,
                        replyId = null,
                        createdBefore = null,
                        createdAfter = null
                    )
                },
                metaformId = metaform.id
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun deleteAuditLogEntryPermission() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")

            testBuilder.permissionTestByScopes(
                scope = PermissionScope.ANONYMOUS,
                apiCaller = { authentication: TestBuilderAuthentication, _: Int ->
                    val replyData: MutableMap<String, Any> = HashMap()
                    replyData["text"] = "Test text value"
                    val createdReply: Reply = testBuilder.test1.replies.createReplyWithData(replyData)
                    testBuilder.test1.replies.create(metaform.id!!, null, ReplyMode.REVISION.toString(), createdReply)

                    val auditLog = testBuilder.systemAdmin.auditLogs.listAuditLogEntries(
                        metaformId = metaform.id,
                        userId = null,
                        replyId = null,
                        createdBefore = null,
                        createdAfter = null
                    ).first()

                    authentication.auditLogs.deleteAuditLogsEntries(metaform.id, auditLog.id!!)
                }
            )
        }
    }
}