package fi.metatavu.metaform.server.test.functional.tests

import fi.metatavu.metaform.api.client.models.Metaform
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
    fun testDeleteReplies() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("tbnc")

            for (i in 1..22) {
                testBuilder.test1.replies.createTBNCReply(metaform.id!!, "test $i", true, 1.0, arrayOf("option 1"))
            }

            assertEquals(22, testBuilder.systemAdmin.replies.listReplies(metaform.id!!, null, null, null, null, null,
                true, null, null, null, null, null).size)
            testBuilder.systemAdmin.metaforms.setMetaFormDeleted(metaform.id)
            println("Waiting 65 seconds for reply deletion job 1/3")
            Thread.sleep(65000)
            assertEquals(12, testBuilder.systemAdmin.replies.listReplies(metaform.id!!, null, null, null, null, null,
                true, null, null, null, null, null).size)
            println("Reply deletion job 1/3 succeeded")
            println("Waiting 65 seconds for reply deletion job 2/3")
            Thread.sleep(65000)
            assertEquals(2, testBuilder.systemAdmin.replies.listReplies(metaform.id!!, null, null, null, null, null,
                true, null, null, null, null, null).size)
            println("Reply deletion job 2/3 succeeded")
            println("Waiting 65 seconds for reply deletion job 3/3")
            Thread.sleep(65000)
            assertEquals(0, testBuilder.systemAdmin.replies.listReplies(metaform.id!!, null, null, null, null, null,
                true, null, null, null, null, null).size)
            println("Reply deletion job 3/3 succeeded")
        }
    }
}