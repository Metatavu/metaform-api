package fi.metatavu.metaform.server.test.functional.tests

import fi.metatavu.metaform.api.client.models.Metaform
import fi.metatavu.metaform.server.rest.ReplyMode
import fi.metatavu.metaform.server.test.functional.AbstractTest
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import fi.metatavu.metaform.server.test.functional.builder.resources.MetaformKeycloakResource
import fi.metatavu.metaform.server.test.functional.builder.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.apache.commons.lang3.ArrayUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

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
    fun testDeleteMetaform() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val draftData: MutableMap<String, Any> = HashMap()

            draftData["text"] = "draft value"


            for (i in 1..22) {
                testBuilder.systemAdmin.replies.createSimpleReply(metaform.id!!, "test $i", ReplyMode.REVISION)
            }

            for (i in 1..10) {
                testBuilder.systemAdmin.emailNotifications.createEmailNotification(metaform.id!!, "Simple subject", "Simple content", listOf("user@example.com"))
            }
            testBuilder.systemAdmin.drafts.createDraft(metaform, draftData)
            testBuilder.systemAdmin.metaformMembers.createSimpleMember(metaform.id!!, "create-test")
            assertEquals(22, testBuilder.systemAdmin.replies.listReplies(metaform.id!!, null, null, null, null, null,
                true, null, null, null, null, null).size)
            testBuilder.systemAdmin.metaforms.setMetaFormDeleted(metaform.id)
            println("Waiting 65 seconds for reply deletion job 1/3")
            Thread.sleep(11000)
            assertEquals(12, testBuilder.systemAdmin.replies.listReplies(metaform.id!!, null, null, null, null, null,
                true, null, null, null, null, null).size)
            println("Reply deletion job 1/3 succeeded")
            println("Waiting 65 seconds for reply deletion job 2/3")
            Thread.sleep(11000)
            assertEquals(10, testBuilder.systemAdmin.emailNotifications.listEmailNotifications(metaform.id).size)
            assertEquals(1, testBuilder.systemAdmin.metaformMembers.list(metaform.id!!, role = null).size)
            assertEquals(2, testBuilder.systemAdmin.replies.listReplies(metaform.id!!, null, null, null, null, null,
                true, null, null, null, null, null).size)
            println("Deletion job 2/3 succeeded")
            println("Waiting 65 seconds for deletion job 3/3")
            Thread.sleep(11000)
            assertEquals(3, testBuilder.systemAdmin.emailNotifications.listEmailNotifications(metaform.id).size)
            assertEquals(0, testBuilder.systemAdmin.replies.listReplies(metaform.id!!, null, null, null, null, null,
                true, null, null, null, null, null).size)
            assertEquals(0, testBuilder.systemAdmin.metaformMembers.list(metaform.id!!, role = null).size)
            assertEquals(0, testBuilder.systemAdmin.replies.listReplies(metaform.id!!, null, null, null, null, null,
                true, null, null, null, null, null).size)

            println("Deletion job 3/3 succeeded")
            println("Waiting 65 seconds for deletion job 4/4")
            Thread.sleep(11000)
            assertEquals(0, testBuilder.systemAdmin.emailNotifications.listEmailNotifications(metaform.id).size)

        }
    }
}