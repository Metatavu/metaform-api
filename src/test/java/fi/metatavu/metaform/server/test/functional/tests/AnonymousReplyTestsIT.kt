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

/**
 * Tests for testing anonymous user reply functionality
 */
@QuarkusTest
@QuarkusTestResource.List(
        QuarkusTestResource(MysqlResource::class),
        QuarkusTestResource(KeycloakResource::class)
)
@TestProfile(GeneralTestProfile::class)
class AnonymousReplyTestsIT : AbstractTest() {
    @Test
    @Throws(Exception::class)
    fun testAnonymousUpdateReplyOwnerKey() {
        TestBuilder().use { testBuilder ->
            val metaform1: Metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple-owner-keys")
            val metaform2: Metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple-owner-keys")
            val reply1: Reply = testBuilder.metaformAdmin.replies.createSimpleReply(metaform1.id!!, "TEST", ReplyMode.REVISION)
            val reply2: Reply = testBuilder.metaformAdmin.replies.createSimpleReply(metaform1.id, "TEST", ReplyMode.REVISION)
            testBuilder.anon.replies.updateReply(metaform1.id, reply1.id!!, reply1, reply1.ownerKey)
            testBuilder.anon.replies.assertUpdateFailStatus(404, metaform2.id!!, reply1, reply1.ownerKey)
            testBuilder.anon.replies.assertUpdateFailStatus(404, metaform2.id, reply1, null)
            testBuilder.anon.replies.assertUpdateFailStatus(403, metaform1.id, reply2, reply1.ownerKey)
            testBuilder.anon.replies.assertUpdateFailStatus(403, metaform1.id, reply1, reply2.ownerKey)
            testBuilder.anon.replies.assertUpdateFailStatus(403, metaform1.id, reply1, null)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testAllowAnonymousReply() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple-allow-anonymous")
            val reply: Reply = testBuilder.anon.replies.createSimpleReply(metaform.id!!, "TEST", ReplyMode.REVISION)
            Assertions.assertNotNull(reply)
            testBuilder.metaformAdmin.replies.delete(metaform.id, reply.id!!, null)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testDisallowAnonymousReply() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
            testBuilder.anon.replies.assertCreateSimpleReplyFail(403, metaform.id!!, "TEST", ReplyMode.REVISION)
        }
    }
}