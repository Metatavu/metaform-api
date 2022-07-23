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
 * Tests for testing answerer user form functionality
 */
@QuarkusTest
@QuarkusTestResource.List(
        QuarkusTestResource(MysqlResource::class),
        QuarkusTestResource(KeycloakResource::class)
)
@TestProfile(GeneralTestProfile::class)
class AnswererTestsIT : AbstractTest() {
//    @Test
//    @Throws(Exception::class)
//    fun testAnswererCreateForm() {
//        TestBuilder().use { testBuilder ->
//            val payload = Metaform()
//            testBuilder.answerer1.metaforms.assertCreateFailStatus(403, payload)
//        }
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun testAnswererFindForm() {
//        TestBuilder().use { testBuilder ->
//            val metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
//            val foundMetaform: Metaform = testBuilder.answerer1.metaforms.findMetaform(metaform.id!!, null, null)
//            Assertions.assertNotNull(foundMetaform)
//            Assertions.assertEquals("Simple", foundMetaform.title)
//        }
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun testAnswererUpdateForm() {
//        TestBuilder().use { testBuilder ->
//            val metaform: Metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
//            testBuilder.answerer1.metaforms.assertUpdateFailStatus(403, metaform.id!!, metaform)
//        }
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun testAnswererDeleteForm() {
//        TestBuilder().use { testBuilder ->
//            val metaform: Metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
//            testBuilder.answerer1.metaforms.assertDeleteFailStatus(403, metaform.id!!)
//        }
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun testAllowAnonymousReply() {
//        TestBuilder().use { testBuilder ->
//            val metaform: Metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple-allow-anonymous")
//            val reply: Reply = testBuilder.anon.replies.createSimpleReply(metaform.id!!, "TEST", ReplyMode.REVISION)
//            Assertions.assertNotNull(reply)
//            testBuilder.metaformAdmin.replies.delete(metaform.id, reply.id!!, null)
//        }
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun testDisallowAnonymousReply() {
//        TestBuilder().use { testBuilder ->
//            val metaform: Metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
//            val reply: Reply = testBuilder.test1.replies.createSimpleReply(metaform.id!!, "TEST", ReplyMode.REVISION)
//            Assertions.assertNotNull(reply)
//            testBuilder.metaformAdmin.replies.delete(metaform.id, reply.id!!, null)
//        }
//    }
}