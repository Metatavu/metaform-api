package fi.metatavu.metaform.server.test.functional.tests

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
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.testcontainers.shaded.org.awaitility.Awaitility
import org.testcontainers.shaded.org.awaitility.Awaitility.await
import java.time.Duration
import java.util.UUID

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

            assertFalse(isMetaformDeleted(metaform.id!!))

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

            await().atMost(Duration.ofMinutes(2)).until {
                testBuilder.systemAdmin.metaforms.list().isEmpty()
            }

            testBuilder.systemAdmin.replies.assertListFailStatus(
                expectedStatus = 404,
                metaformId = metaform.id,
                userId = null,
                createdBefore = null,
                createdAfter = null,
                modifiedBefore = null,
                modifiedAfter = null,
                includeRevisions = null,
                fields = null,
                firstResult = null,
                maxResults = null
            )

            testBuilder.systemAdmin.metaformMembers.assertListFailStatus(
                expectedStatus = 404,
                metaformId = metaform.id,
                role = null
            )

            testBuilder.systemAdmin.metaformVersions.assertListFailStatus(
                expectedStatus = 404,
                metaformId = metaform.id
            )

            testBuilder.systemAdmin.emailNotifications.assertListFailStatus(
                expectedStatus = 404,
                metaformId = metaform.id
            )

            testBuilder.systemAdmin.auditLogs.assertListFailStatus(
                status = 404,
                metaformId = metaform.id,
                userId = null,
                replyId = null,
                createdBefore = null,
                createdAfter = null
            )

            await()
                .timeout(Duration.ofMinutes(5))
                .until {
                    isMetaformDeleted(metaform.id)
                }

            assertTrue(isMetaformDeleted(metaform.id))
        }
    }

    /**
     * Returns whether metaform is deleted from database
     *
     * @param metaformId metaform id
     * @return whether metaform is deleted from database
     */
    private fun isMetaformDeleted(metaformId: UUID): Boolean {
        return executeSelect("SELECT id FROM Metaform WHERE id = ?", metaformId).isEmpty()
    }

}