package fi.metatavu.metaform.server.test.functional.tests

import fi.metatavu.metaform.server.test.functional.AbstractTest
import fi.metatavu.metaform.server.test.functional.builder.PermissionScope
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import fi.metatavu.metaform.server.test.functional.builder.auth.TestBuilderAuthentication
import fi.metatavu.metaform.server.test.functional.builder.resources.MetaformKeycloakResource
import fi.metatavu.metaform.server.test.functional.builder.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.time.OffsetDateTime
import org.awaitility.Awaitility.await
import java.time.Duration

/**
 * Tests for Metaform Statistics
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(MysqlResource::class),
    QuarkusTestResource(MetaformKeycloakResource::class)
)
@TestProfile(GeneralTestProfile::class)
class MetaformStatisticsTestsIT: AbstractTest() {

    @Test
    fun testLastReplyDate() {
        TestBuilder().use { builder ->
            val metaform = builder.systemAdmin.metaforms.createFromJsonFile("simple")
            val reply1 = builder.systemAdmin.metaformStatistics.createReplyForMetaform(metaform.id!!)
            val metaformStatistics1 = builder.systemAdmin.metaformStatistics.getMetaformStatistics(metaform.id)
            val reply2 = builder.systemAdmin.metaformStatistics.createReplyForMetaform(metaform.id)
            val metaformStatistics2 = builder.systemAdmin.metaformStatistics.getMetaformStatistics(metaform.id)

            assertEquals(OffsetDateTime.parse(reply1.createdAt), OffsetDateTime.parse(metaformStatistics1.lastReplyDate))
            assertEquals(OffsetDateTime.parse(reply2.createdAt), OffsetDateTime.parse(metaformStatistics2.lastReplyDate))
            assertTrue(OffsetDateTime.parse(metaformStatistics2.lastReplyDate) > OffsetDateTime.parse(metaformStatistics1.lastReplyDate))
        }
    }

    @Test
    fun testAverageReplyProcessDelay() {
        TestBuilder().use { builder ->
            val metaform = builder.systemAdmin.metaforms.createFromJsonFile("simple")
            val createdReply = builder.systemAdmin.metaformStatistics.createReplyForMetaform(metaform.id!!)
            val createdReply2 = builder.systemAdmin.metaformStatistics.createReplyForMetaform(metaform.id)

            await().atMost(Duration.ofMillis(1000))

            builder.systemAdmin.replies.findReply(metaform.id, createdReply.id!!, null)
            val statistics1 = builder.systemAdmin.metaformStatistics.getMetaformStatistics(metaform.id)

            await().atMost(Duration.ofMillis(30000))

            builder.systemAdmin.replies.findReply(metaform.id, createdReply2.id!!, null)
            val statistics2 = builder.systemAdmin.metaformStatistics.getMetaformStatistics(metaform.id)

            assertTrue(2 >= statistics1.averageReplyProcessDelay!!)
            assertTrue(17 >= statistics2.averageReplyProcessDelay!!)
        }
    }

    @Test
    fun testCountUnprocessedReplies() {
        TestBuilder().use { builder ->
            val metaform1 = builder.systemAdmin.metaforms.createFromJsonFile("simple-status")
            val metaform2 = builder.systemAdmin.metaforms.createFromJsonFile("simple-status")
            builder.systemAdmin.metaformStatistics.createNReplies(metaform1.id!!, 5)
            builder.systemAdmin.metaformStatistics.createNReplies(metaform2.id!!, 10)
            val statistics1 = builder.systemAdmin.metaformStatistics.getMetaformStatistics(metaform1.id)
            val statistics2 = builder.systemAdmin.metaformStatistics.getMetaformStatistics(metaform2.id)

            builder.systemAdmin.metaformStatistics.createNReplies(metaform1.id, 2, "processed")
            val statistics3 = builder.systemAdmin.metaformStatistics.getMetaformStatistics(metaform1.id)

            val reply = builder.systemAdmin.replies.listReplies(metaform2.id).first()
            val updatedReplyData: MutableMap<String, Any> = HashMap()
            updatedReplyData["status"] = "processed"
            val updatedReply = reply.copy(data = updatedReplyData)
            builder.systemAdmin.replies.updateReply(metaform2.id, reply.id!!, updatedReply)
            val statistics4 = builder.systemAdmin.metaformStatistics.getMetaformStatistics(metaform2.id)

            assertEquals(metaform1.id, statistics1.metaformId)
            assertEquals(statistics1.unprocessedReplies, 5)
            assertEquals(statistics2.metaformId, metaform2.id)
            assertEquals(10, statistics2.unprocessedReplies)
            assertEquals(5, statistics3.unprocessedReplies)
            assertEquals(9, statistics4.unprocessedReplies)
        }
    }

    @Test
    fun testAverageRepliesPerMonth() {
        TestBuilder().use { builder ->
            val metaform1 = builder.systemAdmin.metaforms.createFromJsonFile("simple-status")
            val metaform2 = builder.systemAdmin.metaforms.createFromJsonFile("simple-status")

            builder.systemAdmin.metaformStatistics.createNReplies(metaform1.id!!, 10)
            await().atMost(Duration.ofMinutes(1)).until{
                builder.systemAdmin.metaformStatistics.getMetaformStatistics(metaform1.id).averageMonthlyReplies == 10
            }
            val statistics1 = builder.systemAdmin.metaformStatistics.getMetaformStatistics(metaform1.id)
            assertEquals(10, statistics1.averageMonthlyReplies)

            builder.systemAdmin.metaformStatistics.createNReplies(metaform2.id!!, 20)
            await().atMost(Duration.ofMinutes(1)).until{
                builder.systemAdmin.metaformStatistics.getMetaformStatistics(metaform2.id).averageMonthlyReplies == 20
            }
            val statistics2 = builder.systemAdmin.metaformStatistics.getMetaformStatistics(metaform2.id)
            assertEquals(20, statistics2.averageMonthlyReplies)
        }
    }

    @Test
    fun testMetaformStatisticsPermission() {
        TestBuilder().use { builder ->
            val metaform = builder.systemAdmin.metaforms.createFromJsonFile("simple")

            builder.permissionTestByScopes(
                scope = PermissionScope.METAFORM_MANAGER,
                metaformId = metaform.id,
                apiCaller = { authentication: TestBuilderAuthentication, _: Int ->
                    authentication.metaformStatistics.getMetaformStatistics(metaform.id!!)
                }
            )
        }
    }
}