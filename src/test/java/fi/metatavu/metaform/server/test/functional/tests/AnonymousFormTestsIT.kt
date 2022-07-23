package fi.metatavu.metaform.server.test.functional.tests

import fi.metatavu.metaform.api.client.models.Metaform
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
 * Tests for testing anonymous user form functionality
 */
@QuarkusTest
@QuarkusTestResource.List(
        QuarkusTestResource(MysqlResource::class),
        QuarkusTestResource(KeycloakResource::class)
)
@TestProfile(GeneralTestProfile::class)
class AnonymousFormTestsIT : AbstractTest() {
    @Test
    @Throws(Exception::class)
    fun testAnonymousCreateForm() {
        TestBuilder().use { testBuilder ->
            val payload = Metaform()
            testBuilder.anon.metaforms.assertCreateFailStatus(403, payload)
        }
    }

    // TODO rework this, simple form is public
//    @Test
//    @Throws(Exception::class)
//    fun testAnonymousFindForm() {
//        TestBuilder().use { testBuilder ->
//            val metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
//            testBuilder.anon.metaforms.assertFindFailStatus(403, metaform.id!!)
//        }
//    }

    @Test
    @Throws(Exception::class)
    fun testAnonymousFindFormOwnerKey() {
        TestBuilder().use { testBuilder ->
            val metaform1: Metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple-owner-keys")
            val metaform2: Metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple-owner-keys")
            testBuilder.anon.metaforms.assertFindFailStatus(403, metaform1.id!!)
            val testReply1 = testBuilder.metaformAdmin.replies.createSimpleReply(metaform1.id, "TEST", ReplyMode.REVISION)
            val testReply2 = testBuilder.metaformAdmin.replies.createSimpleReply(metaform1.id, "TEST", ReplyMode.REVISION)
            val testReply3 = testBuilder.metaformAdmin.replies.createSimpleReply(metaform2.id!!, "TEST", ReplyMode.REVISION)
            testBuilder.anon.metaforms.assertFindFailStatus(403, metaform1.id, testReply1.id, testReply2.ownerKey)
            testBuilder.anon.metaforms.assertFindFailStatus(403, metaform2.id, testReply1.id, testReply1.ownerKey)
            testBuilder.anon.metaforms.assertFindFailStatus(403, metaform1.id, testReply3.id, testReply3.ownerKey)
            testBuilder.anon.metaforms.assertFindFailStatus(403, metaform1.id, null, testReply1.ownerKey)
            testBuilder.anon.metaforms.assertFindFailStatus(403, metaform1.id, testReply1.id, null)
            Assertions.assertNotNull(testBuilder.anon.metaforms.findMetaform(metaform1.id, testReply1.id!!, testReply1.ownerKey))
        }
    }

    @Test
    @Throws(Exception::class)
    fun testAnonymousUpdateForm() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
            testBuilder.anon.metaforms.assertUpdateFailStatus(403, metaform.id!!, metaform)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testAnonymousDeleteForm() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.metaformAdmin.metaforms.createFromJsonFile("simple")
            testBuilder.anon.metaforms.assertDeleteFailStatus(403, metaform.id!!)
        }
    }
}