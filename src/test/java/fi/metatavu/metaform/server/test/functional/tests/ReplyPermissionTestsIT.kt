package fi.metatavu.metaform.server.test.functional.tests

import com.github.tomakehurst.wiremock.client.WireMock
import fi.metatavu.metaform.api.client.models.Metaform
import fi.metatavu.metaform.api.client.models.Reply
import fi.metatavu.metaform.server.rest.ReplyMode
import fi.metatavu.metaform.server.test.functional.AbstractTest
import fi.metatavu.metaform.server.test.functional.MailgunMocker
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import fi.metatavu.metaform.server.test.functional.builder.resources.KeycloakResource
import fi.metatavu.metaform.server.test.functional.builder.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.eclipse.microprofile.config.ConfigProvider
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*
import java.util.stream.Collectors

/**
 * Tests that test the reply permissions
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTest
@QuarkusTestResource.List(
        QuarkusTestResource(MysqlResource::class),
        QuarkusTestResource(KeycloakResource::class)
)
@TestProfile(GeneralTestProfile::class)
class ReplyPermissionTestsIT : AbstractTest() {
    /**
     * Test that asserts that user may find his / her own reply
     */
    @Test
    @Throws(Exception::class)
    fun findOwnReply() {
        TestBuilder().use { builder ->
            val metaform: Metaform = builder.metaformAdmin.metaforms.createFromJsonFile("simple-permission-context")
            val reply = builder.test1.replies.create(metaform.id!!, null, ReplyMode.REVISION.toString(),
                    builder.test1.replies.createReplyWithData(createPermissionSelectReplyData("group-1")))
            val foundReply: Reply = builder.test1.replies.findReply(metaform.id, reply.id!!, null)
            Assertions.assertNotNull(foundReply)
        }
    }

    /**
     * Test that asserts that anonymous users may not find their "own" replies
     */
    @Test
    @Throws(Exception::class)
    fun findOwnReplyAnonymous() {
        TestBuilder().use { builder ->
            val metaform: Metaform = builder.metaformAdmin.metaforms.createFromJsonFile("simple-permission-context")
            val reply1 = builder.test1.replies.create(metaform.id!!, null, ReplyMode.REVISION.toString(),
                    builder.test1.replies.createReplyWithData(createPermissionSelectReplyData("group-1")))
            builder.anonymousToken.replies.assertFindFailStatus(403, metaform.id, reply1.id!!, null)
        }
    }

    /**
     * Test that asserts that other users may not find their replies
     */
    @Test
    @Throws(Exception::class)
    fun findOthersReplyUser() {
        TestBuilder().use { builder ->
            val metaform = builder.metaformAdmin.metaforms.createFromJsonFile("simple-permission-context")
            val reply = builder.test1.replies.create(metaform.id!!, null, ReplyMode.REVISION.toString(),
                    builder.test1.replies.createReplyWithData(createPermissionSelectReplyData("group-1")))
            builder.test2.replies.assertFindFailStatus(403, metaform.id, reply.id!!, null)
        }
    }

    /**
     * Test that asserts that metaform-admin may find replies created by others
     */
    @Test
    @Throws(Exception::class)
    fun findOthersReplyAdmin() {
        TestBuilder().use { builder ->
            val metaform = builder.metaformAdmin.metaforms.createFromJsonFile("simple-permission-context")
            val reply = builder.test1.replies.create(metaform.id!!, null, ReplyMode.REVISION.toString(),
                    builder.test1.replies.createReplyWithData(createPermissionSelectReplyData("group-1")))
            val foundReply: Reply = builder.metaformAdmin.replies.findReply(metaform.id, reply.id!!, null)
            Assertions.assertNotNull(foundReply)
        }
    }

    /**
     * Test that asserts that user may list only his / her own replies
     */
    @Test
    @Throws(Exception::class)
    fun listOwnReplies() {
        TestBuilder().use { builder ->
            val metaform = builder.metaformAdmin.metaforms.createFromJsonFile("simple-permission-context")
            val reply1 = builder.test1.replies.create(metaform.id!!, null, ReplyMode.REVISION.toString(),
                    builder.test1.replies.createReplyWithData(createPermissionSelectReplyData("group-1")))
            builder.test2.replies.create(metaform.id, null, ReplyMode.REVISION.toString(),
                    builder.test1.replies.createReplyWithData(createPermissionSelectReplyData("group-2")))
            builder.test3.replies.create(metaform.id, null, ReplyMode.REVISION.toString(),
                    builder.test1.replies.createReplyWithData(createPermissionSelectReplyData("group-3")))
            val replies: Array<Reply> = builder.test1.replies.listReplies(metaform.id)
            Assertions.assertEquals(1, replies.size.toLong())
            Assertions.assertEquals(reply1.id, replies[0].id)
        }
    }

    /**
     * Test that asserts that user in permission context group may see replies targeted to that group
     */
    @Test
    @Throws(Exception::class)
    fun listPermissionContextReplies() {
        TestBuilder().use { builder ->
            val metaform: Metaform = builder.metaformAdmin.metaforms.createFromJsonFile("simple-permission-context")
            val reply1 = builder.test1.replies.create(metaform.id!!, null, ReplyMode.REVISION.toString(),
                    builder.test1.replies.createReplyWithData(createPermissionSelectReplyData("group-2")))
            val reply2 = builder.test2.replies.create(metaform.id, null, ReplyMode.REVISION.toString(),
                    builder.test1.replies.createReplyWithData(createPermissionSelectReplyData("group-2")))
            val reply3 = builder.test3.replies.create(metaform.id, null, ReplyMode.REVISION.toString(),
                    builder.test1.replies.createReplyWithData(createPermissionSelectReplyData("group-2")))
            val replies1: Array<Reply> = builder.test1.replies.listReplies(metaform.id)
            val replies2: Array<Reply> = builder.test2.replies.listReplies(metaform.id)
            val replies3: Array<Reply> = builder.test3.replies.listReplies(metaform.id)
            Assertions.assertEquals(1, replies1.size)
            Assertions.assertEquals(reply1.id, replies1[0].id)
            Assertions.assertEquals(3, replies2.size)
            val reply2Ids = Arrays.stream(replies2).map(Reply::id).collect(Collectors.toSet())
            Assertions.assertTrue(reply2Ids.contains(reply1.id))
            Assertions.assertTrue(reply2Ids.contains(reply2.id))
            Assertions.assertTrue(reply2Ids.contains(reply3.id))
            Assertions.assertEquals(1, replies3.size)
            Assertions.assertEquals(reply3.id, replies3[0].id)
        }
    }

    /**
     * Test that asserts that user in permission context group may see replies targeted to that group
     */
    @Test
    @Throws(Exception::class)
    fun exportPermissionContextReplyPdf() {
        TestBuilder().use { builder ->
            val exportTheme = builder.metaformAdmin.exportThemes.createSimpleExportTheme("theme 1")
            builder.metaformAdmin.exportFiles.createSimpleExportThemeFile(exportTheme.id!!, "reply/pdf.ftl", "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></meta><title>title</title></head><body>content</body></html>")
            val metaform: Metaform = builder.metaformAdmin.metaforms.createFromJsonFile("simple-permission-context")
            val updateData = Metaform(metaform.id, metaform.visibility, exportTheme.id, metaform.allowAnonymous, metaform.allowDrafts,
                    metaform.allowReplyOwnerKeys, metaform.allowInvitations, metaform.autosave, metaform.title, metaform.slug, metaform.sections, metaform.filters, metaform.scripts)
            builder.metaformAdmin.metaforms.updateMetaform(metaform.id!!, updateData)
            val reply1: Reply = builder.test1.replies.create(metaform.id, null, ReplyMode.REVISION.toString(),
                    builder.test1.replies.createReplyWithData(createPermissionSelectReplyData("group-2")))
            val reply2: Reply = builder.test2.replies.create(metaform.id, null, ReplyMode.REVISION.toString(),
                    builder.test1.replies.createReplyWithData(createPermissionSelectReplyData("group-2")))
            val reply3: Reply = builder.test3.replies.create(metaform.id, null, ReplyMode.REVISION.toString(),
                    builder.test1.replies.createReplyWithData(createPermissionSelectReplyData("group-2")))

            // test1.realm1 may download only own reply
            assertPdfDownloadStatus(200, builder.test1.token, metaform, reply1)
            assertPdfDownloadStatus(403, builder.test1.token, metaform, reply2)
            assertPdfDownloadStatus(403, builder.test1.token, metaform, reply3)

            // test2.realm1 may download all the replies
            assertPdfDownloadStatus(200, builder.test2.token, metaform, reply1)
            assertPdfDownloadStatus(200, builder.test2.token, metaform, reply2)
            assertPdfDownloadStatus(200, builder.test2.token, metaform, reply3)

            // test3.realm1 may download only own reply
            assertPdfDownloadStatus(403, builder.test3.token, metaform, reply1)
            assertPdfDownloadStatus(403, builder.test3.token, metaform, reply2)
            assertPdfDownloadStatus(200, builder.test3.token, metaform, reply3)

            // anonymous may not download any replies
            assertPdfDownloadStatus(403, builder.anonymousToken.token, metaform, reply1)
            assertPdfDownloadStatus(403, builder.anonymousToken.token, metaform, reply2)
            assertPdfDownloadStatus(403, builder.anonymousToken.token, metaform, reply3)
        }
    }

    /**
     * Test that asserts that admin may list all replies
     */
    @Test
    @Throws(Exception::class)
    fun listRepliesAdmin() {
        TestBuilder().use { builder ->
            val metaform = builder.metaformAdmin.metaforms.createFromJsonFile("simple-permission-context")
            builder.test1.replies.create(metaform.id!!, null, ReplyMode.REVISION.toString(),
                    builder.test1.replies.createReplyWithData(createPermissionSelectReplyData("group-2")))
            builder.test2.replies.create(metaform.id, null, ReplyMode.REVISION.toString(),
                    builder.test1.replies.createReplyWithData(createPermissionSelectReplyData("group-2")))
            builder.test3.replies.create(metaform.id, null, ReplyMode.REVISION.toString(),
                    builder.test1.replies.createReplyWithData(createPermissionSelectReplyData("group-2")))
            val replies: Array<Reply> = builder.metaformAdmin.replies.listReplies(metaform.id)
            Assertions.assertEquals(3, replies.size)
        }
    }

    /**
     * Test that asserts that user in permission context receives an email when notification is posted and
     * another user receives when reply is updated
     */
    @Test
    @Throws(Exception::class)
    fun notifyPermissionContextReply() {
        val mailgunMocker: MailgunMocker = startMailgunMocker()
        try {
            TestBuilder().use { builder ->
                val metaform = builder.metaformAdmin.metaforms.createFromJsonFile("simple-permission-context")
                builder.metaformAdmin.emailNotifications.createEmailNotification(metaform.id!!, "Permission context subject", "Permission context content", emptyList())
                val reply = builder.test3.replies.create(metaform.id, null, ReplyMode.REVISION.toString(),
                        builder.test3.replies.createReplyWithData(createPermissionSelectReplyData("group-2")))
                builder.test3.replies.updateReply(metaform.id,
                        reply.id!!,
                        builder.test3.replies.createPermissionSelectReply("group-1"), null as String?)
                builder.test3.replies.updateReply(metaform.id,
                        reply.id,
                        builder.test3.replies.createPermissionSelectReply("group-1"), null as String?)
                mailgunMocker.verifyHtmlMessageSent(1, "Metaform Test", "metaform-test@example.com", "user1@example.com", "Permission context subject", "Permission context content")
                mailgunMocker.verifyHtmlMessageSent(1, "Metaform Test", "metaform-test@example.com", "user2@example.com", "Permission context subject", "Permission context content")
            }
        } finally {
            stopMailgunMocker(mailgunMocker)
        }
    }

    /**
     * Creates permission select reply data with given value
     *
     * @param value value
     */
    private fun createPermissionSelectReplyData(value: String): Map<String, Any> {
        val replyData: MutableMap<String, Any> = HashMap()
        replyData["permission-select"] = value
        return replyData
    }

    @BeforeAll
    fun setMocker() {
        val host = ConfigProvider.getConfig().getValue("wiremock.host", String::class.java)
        val port = ConfigProvider.getConfig().getValue("wiremock.port", String::class.java).toInt()
        WireMock.configureFor(host, port)
    }
}