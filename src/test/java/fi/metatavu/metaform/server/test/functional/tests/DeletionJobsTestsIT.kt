package fi.metatavu.metaform.server.test.functional.tests

import fi.metatavu.metaform.api.client.models.Draft
import fi.metatavu.metaform.api.client.models.Metaform
import fi.metatavu.metaform.api.client.models.MetaformVersion
import fi.metatavu.metaform.api.client.models.MetaformVersionType
import fi.metatavu.metaform.server.rest.ReplyMode
import fi.metatavu.metaform.server.test.functional.AbstractTest
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import fi.metatavu.metaform.server.test.functional.builder.resources.MetaformKeycloakResource
import fi.metatavu.metaform.server.test.functional.builder.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.testcontainers.shaded.org.awaitility.Awaitility
import java.time.Duration

/**
 * Tests scheduled deletion jobs
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(MysqlResource::class),
    QuarkusTestResource(MetaformKeycloakResource::class)
)
@TestProfile(GeneralTestProfile::class)
class DeletionJobsTestsIT: AbstractTest() {
    @Test
    fun testMetaformDeletionJob() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple", false)

            val draftData: MutableMap<String, Any> = HashMap()
            draftData["text"] = "draft value"
            testBuilder.test1.drafts.createDraft(metaform, draftData, false)
            assertEquals(1, testBuilder.systemAdmin.drafts.listDraftsByMetaform(metaform.id!!).size)

            for (i in 1..10) {
                testBuilder.systemAdmin.replies.createSimpleReply(metaform.id, "test $i", ReplyMode.REVISION, false)
            }

            for (i in 1..12) {
                testBuilder.systemAdmin.emailNotifications.createEmailNotification(metaform.id, "Simple subject", "Simple content", listOf("user@example.com"), null, false)
            }

            val versionData = testBuilder.systemAdmin.metaformVersions.exampleVersionData
            val version = MetaformVersion(
                type = MetaformVersionType.ARCHIVED,
                data = versionData
            )

            testBuilder.systemAdmin.metaformVersions.create(metaform.id, version, false,)
            testBuilder.systemAdmin.metaformMembers.createSimpleMember(metaform.id, "create-test", false)
            assertEquals(10, testBuilder.test1.auditLogs.listAuditLogEntries(metaform.id, null, null, null, null).size)

            testBuilder.systemAdmin.metaforms.delete(
                metaformId = metaform.id,
                immediate = false
            )

            Awaitility.await().pollDelay(Duration.ofSeconds(21)).until { true }

            assertEquals(0, testBuilder.systemAdmin.replies.listReplies(metaform.id, null, null, null, null, null,
                true, null, null, null, null, null).size)
            assertEquals(0, testBuilder.systemAdmin.metaformMembers.list(metaform.id, role = null).size)
            assertEquals(0, testBuilder.systemAdmin.metaformVersions.list(metaform.id).size)
            assertEquals(0, testBuilder.systemAdmin.emailNotifications.listEmailNotifications(metaform.id).size)
            assertEquals(0, testBuilder.test1.auditLogs.listAuditLogEntries(metaform.id, null, null, null, null).size)
            assertEquals(0, testBuilder.systemAdmin.drafts.listDraftsByMetaform(metaform.id).size)

            Awaitility.await().pollDelay(Duration.ofSeconds(3)).until { true }

            assertEquals(0, testBuilder.systemAdmin.metaforms.list().size)
        }
    }
}