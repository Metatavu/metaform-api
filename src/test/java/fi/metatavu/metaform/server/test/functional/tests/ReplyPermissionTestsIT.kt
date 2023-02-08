package fi.metatavu.metaform.server.test.functional.tests

import com.github.tomakehurst.wiremock.client.WireMock
import fi.metatavu.metaform.api.client.models.Metaform
import fi.metatavu.metaform.api.client.models.MetaformMemberGroup
import fi.metatavu.metaform.api.client.models.PermissionGroups
import fi.metatavu.metaform.api.client.models.Reply
import fi.metatavu.metaform.server.rest.ReplyMode
import fi.metatavu.metaform.server.test.functional.AbstractTest
import fi.metatavu.metaform.server.test.functional.MailgunMocker
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import fi.metatavu.metaform.server.test.functional.builder.resources.MetaformKeycloakResource
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
        QuarkusTestResource(MetaformKeycloakResource::class)
)
@TestProfile(GeneralTestProfile::class)
class ReplyPermissionTestsIT : AbstractTest() {

    /**
     * Test that asserts that user may find his / her own reply
     */
    @Test
    fun findOwnReply() {
        TestBuilder().use { builder ->
            val metaform: Metaform = builder.systemAdmin.metaforms.createFromJsonFile("simple-permission-context")
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
    fun findOwnReplyAnonymous() {
        TestBuilder().use { builder ->
            val metaform: Metaform = builder.systemAdmin.metaforms.createFromJsonFile("simple-permission-context")
            val reply1 = builder.test1.replies.create(metaform.id!!, null, ReplyMode.REVISION.toString(),
                    builder.test1.replies.createReplyWithData(createPermissionSelectReplyData("group-1")))
            builder.anonymousToken.replies.assertFindFailStatus(403, metaform.id, reply1.id!!, null)
        }
    }

    /**
     * Test that asserts that other users may not find their replies
     */
    @Test
    fun findOthersReplyUser() {
        TestBuilder().use { builder ->
            val metaform = builder.systemAdmin.metaforms.createFromJsonFile("simple-permission-context")
            val reply = builder.test1.replies.create(metaform.id!!, null, ReplyMode.REVISION.toString(),
                    builder.test1.replies.createReplyWithData(createPermissionSelectReplyData("group-1")))
            builder.test2.replies.assertFindFailStatus(403, metaform.id, reply.id!!, null)
        }
    }

    /**
     * Test that asserts that metaform-admin may find replies created by others
     */
    @Test
    fun findOthersReplyAdmin() {
        TestBuilder().use { builder ->
            val metaform = builder.systemAdmin.metaforms.createFromJsonFile("simple-permission-context")
            val reply = builder.test1.replies.create(metaform.id!!, null, ReplyMode.REVISION.toString(),
                    builder.test1.replies.createReplyWithData(createPermissionSelectReplyData("group-1")))
            val foundReply: Reply = builder.systemAdmin.replies.findReply(metaform.id, reply.id!!, null)
            Assertions.assertNotNull(foundReply)
        }
    }

    /**
     * Test that asserts that user may list only his / her own replies
     */
    @Test
    fun listOwnReplies() {
        TestBuilder().use { builder ->
            val metaform = builder.systemAdmin.metaforms.createFromJsonFile("simple-permission-context")
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
    fun listPermissionContextReplies() {
        TestBuilder().use { builder ->
            val metaform: Metaform = builder.systemAdmin.metaforms.createFromJsonFile("simple-permission-context")
            val metaformId = metaform.id!!

            val permittedGroup = builder.systemAdmin.metaformMemberGroups.create(
                metaformId = metaformId,
                payload = MetaformMemberGroup(
                    displayName = "Permitted group",
                    memberIds = arrayOf(USER_2_ID)
                )
            )

            val permittedGroupId = permittedGroup.id!!

            val updatedForm = setOptionGroupPermission(
                metaform = metaform,
                optionName = "group-2",
                viewGroupIds = arrayOf(permittedGroupId),
                editGroupIds = arrayOf(permittedGroupId)
            )

            builder.systemAdmin.metaforms.updateMetaform(id = metaformId, body = updatedForm)

            val replies = arrayOf(builder.test1, builder.test2, builder.test3).map { user ->
                user.replies.create(
                    metaformId = metaformId,
                    updateExisting = null,
                    replyMode = ReplyMode.REVISION.toString(),
                    payload = user.replies.createReplyWithData(createPermissionSelectReplyData("group-2"))
                )
            }

            val replies1: Array<Reply> = builder.test1.replies.listReplies(metaformId)
            val replies2: Array<Reply> = builder.test2.replies.listReplies(metaformId)
            val replies3: Array<Reply> = builder.test3.replies.listReplies(metaformId)

            Assertions.assertEquals(1, replies1.size)
            Assertions.assertEquals(replies[0].id, replies1[0].id)
            Assertions.assertEquals(3, replies2.size)

            val reply2Ids = Arrays.stream(replies2).map(Reply::id).collect(Collectors.toSet())

            Assertions.assertTrue(reply2Ids.containsAll(replies.map(Reply::id)))

            Assertions.assertEquals(1, replies3.size)
            Assertions.assertEquals(replies[2].id, replies3[0].id)
        }
    }

    /**
     * Test that asserts that user in permission context group may see replies targeted to that group
     */
    @Test
    fun exportPermissionContextReplyPdf() {
        TestBuilder().use { builder ->
            val exportTheme = builder.systemAdmin.exportThemes.createSimpleExportTheme("theme 1")
            builder.systemAdmin.exportFiles.createSimpleExportThemeFile(exportTheme.id!!, "reply/pdf.ftl", "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></meta><title>title</title></head><body>content</body></html>")
            val metaform: Metaform = builder.systemAdmin.metaforms.createFromJsonFile("simple-permission-context")
            val metaformId = metaform.id!!

            val permittedGroup = builder.systemAdmin.metaformMemberGroups.create(
                metaformId = metaformId,
                payload = MetaformMemberGroup(
                    displayName = "Permitted group",
                    memberIds = arrayOf(USER_2_ID)
                )
            )

            val permittedGroupId = permittedGroup.id!!

            val updatedForm = setOptionGroupPermission(
                metaform = metaform,
                optionName = "group-2",
                viewGroupIds = arrayOf(permittedGroupId),
                editGroupIds = arrayOf(permittedGroupId)
            )

            builder.systemAdmin.metaforms.updateMetaform(id = metaformId, body = updatedForm.copy(
                exportThemeId = exportTheme.id
            ))

            builder.test1.replies.assertCount(0, metaformId = metaformId)
            builder.test2.replies.assertCount(0, metaformId = metaformId)

            val replies = arrayOf(builder.test1, builder.test2, builder.test3).map { user ->
                user.replies.create(
                    metaformId = metaform.id,
                    updateExisting = null,
                    replyMode = ReplyMode.REVISION.toString(),
                    payload = user.replies.createReplyWithData(createPermissionSelectReplyData("group-2"))
                )
            }

            // test1.realm1 may download only own reply
            assertPdfDownloadStatus(200, builder.test1.token, metaform, replies[0])
            assertPdfDownloadStatus(403, builder.test1.token, metaform, replies[1])
            assertPdfDownloadStatus(403, builder.test1.token, metaform, replies[2])

            // test2.realm1 may download all the replies
            assertPdfDownloadStatus(200, builder.test2.token, metaform, replies[0])
            assertPdfDownloadStatus(200, builder.test2.token, metaform, replies[1])
            assertPdfDownloadStatus(200, builder.test2.token, metaform, replies[2])

            // test3.realm1 may download only own reply
            assertPdfDownloadStatus(403, builder.test3.token, metaform, replies[0])
            assertPdfDownloadStatus(403, builder.test3.token, metaform, replies[1])
            assertPdfDownloadStatus(200, builder.test3.token, metaform, replies[2])

            // anonymous may not download any replies
            assertPdfDownloadStatus(403, builder.anonymousToken.token, metaform, replies[0])
            assertPdfDownloadStatus(403, builder.anonymousToken.token, metaform, replies[1])
            assertPdfDownloadStatus(403, builder.anonymousToken.token, metaform, replies[2])
        }
    }

    /**
     * Test that asserts that admin may list all replies
     */
    @Test
    fun listRepliesAdmin() {
        TestBuilder().use { builder ->
            val metaform = builder.systemAdmin.metaforms.createFromJsonFile("simple-permission-context")
            builder.test1.replies.create(metaform.id!!, null, ReplyMode.REVISION.toString(),
                    builder.test1.replies.createReplyWithData(createPermissionSelectReplyData("group-2")))
            builder.test2.replies.create(metaform.id, null, ReplyMode.REVISION.toString(),
                    builder.test1.replies.createReplyWithData(createPermissionSelectReplyData("group-2")))
            builder.test3.replies.create(metaform.id, null, ReplyMode.REVISION.toString(),
                    builder.test1.replies.createReplyWithData(createPermissionSelectReplyData("group-2")))
            val replies: Array<Reply> = builder.systemAdmin.replies.listReplies(metaform.id)
            Assertions.assertEquals(3, replies.size)
        }
    }

    /**
     * Test that asserts that user in permission context receives an email when notification is posted and
     * another user receives when reply is updated
     */
    @Test
    fun notifyPermissionContextReplyUpdate() {
        val mailgunMocker: MailgunMocker = startMailgunMocker()
        try {
            TestBuilder().use { builder ->
                val metaform = builder.systemAdmin.metaforms.createFromJsonFile("simple-permission-context")
                val metaformId = metaform.id!!

                builder.systemAdmin.emailNotifications.createEmailNotification(metaformId, "Permission context subject", "Permission context content", emptyList())

                val group1 = builder.systemAdmin.metaformMemberGroups.create(
                    metaformId = metaformId,
                    payload = MetaformMemberGroup(
                        displayName = "Group 1",
                        memberIds = arrayOf(USER_1_ID)
                    )
                )

                val group2 = builder.systemAdmin.metaformMemberGroups.create(
                    metaformId = metaformId,
                    payload = MetaformMemberGroup(
                        displayName = "Group 2",
                        memberIds = arrayOf(USER_2_ID)
                    )
                )

                var updatedForm = setOptionGroupPermission(
                    metaform = metaform,
                    optionName = "group-1",
                    notifyGroupIds = arrayOf(group1.id!!)
                )

                updatedForm = setOptionGroupPermission(
                    metaform = updatedForm,
                    optionName = "group-2",
                    notifyGroupIds = arrayOf(group2.id!!)
                )

                builder.systemAdmin.metaforms.updateMetaform(id = metaformId, body = updatedForm)

                val reply = builder.test3.replies.create(
                    metaformId = metaformId,
                    replyMode = ReplyMode.REVISION.toString(),
                    payload = builder.test3.replies.createReplyWithData(createPermissionSelectReplyData("group-2"))
                )

                builder.test3.replies.updateReply(
                    metaformId = metaformId,
                    replyId = reply.id!!,
                    body = builder.test3.replies.createPermissionSelectReply("group-1")
                )

                builder.test3.replies.updateReply(
                    metaformId = metaformId,
                    replyId = reply.id,
                    body = builder.test3.replies.createPermissionSelectReply("group-1")
                )

                mailgunMocker.verifyHtmlMessageSent(1, "Metaform Test", "metaform-test@example.com", "user2@example.com", "Permission context subject", "Permission context content")
                mailgunMocker.verifyHtmlMessageSent(1, "Metaform Test", "metaform-test@example.com", "user1@example.com", "Permission context subject", "Permission context content")
            }
        } finally {
            stopMailgunMocker(mailgunMocker)
        }
    }


    /**
     * Test that asserts that user in permission context receives an email but no-one else does
     */
    @Test
    fun notifyPermissionContextReplyTestWithScopes() {
        val mailgunMocker: MailgunMocker = startMailgunMocker()
        try {
            TestBuilder().use { builder ->

                val subject = "Permission context subject"
                val content = "Permission context content"

                // Create metaform with permission context
                var metaform = builder.systemAdmin.metaforms.createFromJsonFile("simple-permission-context-anon")
                val metaformId = metaform.id!!

                // Add an email notification
                builder.systemAdmin.emailNotifications.createEmailNotification(metaformId, subject, content, emptyList())

                // Create test groups

                val groups = mapOf(
                    "Edit and notify" to USER_1_ID,
                    "Notify" to USER_2_ID,
                    "Edit" to USER_3_ID
                ).map {
                    builder.systemAdmin.metaformMemberGroups.create(
                        metaformId = metaformId,
                        payload = MetaformMemberGroup(
                            displayName = it.key,
                            memberIds = arrayOf(it.value)
                        )
                    )
                }

                // Assign permission: group 1 notify, group 2 edit and notify, group 3 edit only

                metaform = setOptionGroupPermission(
                    metaform = metaform,
                    optionName = "group-1",
                    notifyGroupIds = arrayOf(groups[0].id!!)
                )

                metaform = setOptionGroupPermission(
                    metaform = metaform,
                    optionName = "group-2",
                    notifyGroupIds = arrayOf(groups[1].id!!),
                    editGroupIds = arrayOf(groups[1].id!!)
                )

                metaform = setOptionGroupPermission(
                    metaform = metaform,
                    optionName = "group-3",
                    editGroupIds = arrayOf(groups[2].id!!)
                )

                builder.systemAdmin.metaforms.updateMetaform(id = metaformId, body = metaform)

                // Create replies and assert that group 1 & 2 receive email and 3 does not

                val scenarios = mapOf(
                    "group-1" to mapOf(
                        "user1@example.com" to 1,
                        "user2@example.com" to 0,
                        "user3@example.com" to 0
                    ),
                    "group-2" to mapOf(
                        "user1@example.com" to 1,
                        "user2@example.com" to 1,
                        "user3@example.com" to 0
                    ),
                    "group-3" to mapOf(
                        "user1@example.com" to 1,
                        "user2@example.com" to 1,
                        "user3@example.com" to 0
                    )
                )

                scenarios.forEach { (answer, asserts) ->
                    val reply = builder.anon.replies.create(
                        metaformId = metaformId,
                        replyMode = ReplyMode.REVISION.toString(),
                        payload = builder.anon.replies.createReplyWithData(createPermissionSelectReplyData(answer))
                    )

                    asserts.forEach { (to, count) ->
                        mailgunMocker.verifyHtmlMessageSent(
                            count = count,
                            fromName = "Metaform Test",
                            fromEmail = "metaform-test@example.com",
                            to = to,
                            subject = subject,
                            content = content
                        )
                    }

                    builder.systemAdmin.replies.delete(
                        metaformId = metaformId,
                        replyId = reply.id!!,
                        ownerKey = null
                    )
                }
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

    /**
     * Returns updated metaform where given groups has been added to field permission groups
     *
     * @param metaform metaform
     * @param optionName option name
     * @param viewGroupIds view group ids
     * @param editGroupIds edit group ids
     * @param notifyGroupIds notify group ids
     * @return updated metaform
     */
    private fun setOptionGroupPermission(
        metaform: Metaform,
        optionName: String,
        viewGroupIds: Array<UUID>? = null,
        editGroupIds: Array<UUID>? = null,
        notifyGroupIds: Array<UUID>? = null
    ): Metaform {
        val sections = metaform.sections!!
        val fields = sections[0].fields!!
        val options = fields[0].options!!

        val updatedOptions = options.map {
            if (it.name == optionName) {
                it.copy(
                    permissionGroups = PermissionGroups(
                        viewGroupIds = viewGroupIds,
                        editGroupIds = editGroupIds,
                        notifyGroupIds = notifyGroupIds
                    )
                )
            } else {
                it
            }
        }.toTypedArray()

        val updatedFields = arrayOf(fields[0].copy(
            options = updatedOptions
        ))

        val updatedSections = arrayOf(sections[0].copy(
            fields = updatedFields
        ))

        return metaform.copy(
            sections = updatedSections
        )
    }

    @BeforeAll
    fun setMocker() {
        val host = ConfigProvider.getConfig().getValue("wiremock.host", String::class.java)
        val port = ConfigProvider.getConfig().getValue("wiremock.port", String::class.java).toInt()
        WireMock.configureFor(host, port)
    }
}