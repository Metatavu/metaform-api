package fi.metatavu.metaform.server.test.functional.tests

import fi.metatavu.metaform.api.client.models.Metaform
import fi.metatavu.metaform.api.client.models.MetaformVisibility
import fi.metatavu.metaform.server.rest.ReplyMode
import fi.metatavu.metaform.server.test.functional.AbstractTest
import fi.metatavu.metaform.server.test.functional.builder.PermissionScope
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import fi.metatavu.metaform.server.test.functional.builder.auth.TestBuilderAuthentication
import fi.metatavu.metaform.server.test.functional.builder.resources.KeycloakResource
import fi.metatavu.metaform.server.test.functional.builder.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * Tests for Metaforms functionality
 */
@QuarkusTest
@QuarkusTestResource.List(
        QuarkusTestResource(MysqlResource::class),
        QuarkusTestResource(KeycloakResource::class)
)
@TestProfile(GeneralTestProfile::class)
class MetaformTestsIT : AbstractTest() {
    @Test
    @Throws(Exception::class)
    fun testCreateMetaform() {
        TestBuilder().use { builder ->
            val metaform1: Metaform = builder.systemAdmin.metaforms.createFromJsonFile("simple")
            assertNotNull(metaform1)
            assertNotNull(metaform1.id)
            assertNotNull(metaform1.slug)
            assertEquals("Simple", metaform1.title)
            assertEquals(MetaformVisibility.pUBLIC, metaform1.visibility)
            assertEquals(1, metaform1.sections!!.size)
            assertEquals("Simple form", metaform1.sections[0].title)
            assertEquals(1, metaform1.sections[0].fields!!.size)
            assertEquals("text", metaform1.sections[0].fields!![0].name)
            assertEquals("text", metaform1.sections[0].fields!![0].type.toString())
            assertEquals("Text field", metaform1.sections[0].fields!![0].title)
            assertEquals(true, metaform1.allowDrafts)
            val metaform2: Metaform = builder.systemAdmin.metaforms.createFromJsonFile("simple-slug")
            assertNotNull(metaform2)
            assertNotNull(metaform2.id)
            assertEquals("Simple", metaform2.title)
            assertEquals(MetaformVisibility.pRIVATE, metaform2.visibility)
            assertEquals("simple-slug-0", metaform2.slug)
            assertEquals(1, metaform2.sections!!.size)
            assertEquals("Simple form", metaform2.sections[0].title)
            assertEquals(1, metaform2.sections[0].fields!!.size)
            assertEquals("text", metaform2.sections[0].fields!![0].name)
            assertEquals("text", metaform2.sections[0].fields!![0].type.toString())
            assertEquals("Text field", metaform2.sections[0].fields!![0].title)
            assertEquals(true, metaform2.allowDrafts)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testCreateInvalidSlugMetaform() {
        TestBuilder().use { builder ->
            val parsedMetaform = builder.systemAdmin.metaforms.readMetaform("simple-slug-invalid")
            assertNotNull(parsedMetaform)
            builder.systemAdmin.metaforms.assertCreateFailStatus(409, parsedMetaform!!)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testCreateDuplicatedSlugMetaform() {
        TestBuilder().use { builder ->
            builder.systemAdmin.metaforms.createFromJsonFile("simple-slug")
            val parsedMetaform2 = builder.systemAdmin.metaforms.readMetaform("simple-slug")
            builder.systemAdmin.metaforms.assertCreateFailStatus(409, parsedMetaform2!!)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testCreateMetaformScript() {
        TestBuilder().use { builder ->
            val metaform: Metaform = builder.systemAdmin.metaforms.createFromJsonFile("simple-script")
            assertNotNull(metaform)
            assertNotNull(metaform.id)
            assertNotNull(metaform.scripts)
            assertNotNull(metaform.scripts!!.afterCreateReply)
            assertEquals(2, metaform.scripts.afterCreateReply!!.size)
            assertEquals("create-test", metaform.scripts.afterCreateReply[0].name)
            assertEquals("js", metaform.scripts.afterCreateReply[0].language)
            assertEquals("form.setVariableValue('postdata', 'Text value: ' + form.getReplyData().get('text'));", metaform.scripts.afterCreateReply!![0].content)
            assertNotNull(metaform.scripts.afterUpdateReply)
            assertEquals("update-test", metaform.scripts.afterUpdateReply!![0].name)
            assertEquals("js", metaform.scripts.afterUpdateReply[0].language)
            assertEquals("const xhr = new XMLHttpRequest(); xhr.open('GET', 'http://test-wiremock:8080/externalmock'); xhr.send();", metaform.scripts.afterUpdateReply!![0].content)
        }
    }

    @Test
    @Throws(Exception::class)
    fun createMetaformPermission() {
        TestBuilder().use { testBuilder ->
            try {
                testBuilder.permissionTestByScopes(
                    scope = PermissionScope.METAFORM_ADMIN,
                    apiCaller = { authentication: TestBuilderAuthentication, _: Int ->
                        authentication.metaforms.createFromJsonFile("simple")
                    }
                )
            } finally {
                val metaforms = testBuilder.systemAdmin.metaforms.list()
                metaforms.forEach { metaform ->
                    testBuilder.systemAdmin.metaformMembers.list(metaform.id!!, null).forEach { metaformMember ->
                        testBuilder.systemAdmin.metaformMembers.delete(metaform.id, metaformMember.id!!)
                    }
                    testBuilder.systemAdmin.metaforms.delete(metaform.id)
                }
            }
        }
    }

    @Test
    @Throws(Exception::class)
    fun testFindMetaform() {
        TestBuilder().use { builder ->
            val metaform: Metaform = builder.systemAdmin.metaforms.createFromJsonFile("simple")
            val foundMetaform: Metaform = builder.systemAdmin.metaforms.findMetaform(metaform.id!!, null, null)
            assertEquals(metaform.toString(), foundMetaform.toString())
        }
    }

    @Test
    @Throws(Exception::class)
    fun findMetaformPublicPermission() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")

            testBuilder.permissionTestByScopes(
                scope = PermissionScope.ANONYMOUS,
                apiCaller = { authentication: TestBuilderAuthentication, _: Int ->
                    authentication.metaforms.findMetaform(metaform.id!!, null, null)
                },
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun findMetaformPrivatePermission() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple-private")

            testBuilder.permissionTestByScopes(
                scope = PermissionScope.METAFORM_MANAGER,
                apiCaller = { authentication: TestBuilderAuthentication, _: Int ->
                    authentication.metaforms.findMetaform(metaform.id!!, null, null)
                },
                metaformId = metaform.id
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun testListMetaform() {
        TestBuilder().use { builder ->
            assertEquals(0, builder.systemAdmin.metaforms.list().size)
            val metaform1: Metaform = builder.systemAdmin.metaforms.createFromJsonFile("simple")
            val metaform2: Metaform = builder.systemAdmin.metaforms.createFromJsonFile("simple")
            val metaform3: Metaform = builder.systemAdmin.metaforms.createFromJsonFile("simple")
            val metaform1Modified = metaform1.copy(
                    title="first",
                    slug="first-slug-0",
                    visibility = MetaformVisibility.pRIVATE
            )
            val metaform2Modified = metaform2.copy(
                    title="second",
                    slug="second-slug-0",
                    visibility = MetaformVisibility.pUBLIC
            )
            val metaform3Modified = metaform3.copy(
                title="third",
                slug="third-slug-0",
                visibility = MetaformVisibility.pUBLIC
            )
            builder.systemAdmin.metaforms.updateMetaform(metaform1.id!!, metaform1Modified)
            builder.systemAdmin.metaforms.updateMetaform(metaform2.id!!, metaform2Modified)
            builder.systemAdmin.metaforms.updateMetaform(metaform3.id!!, metaform3Modified)
            val list: MutableList<Metaform> = builder.systemAdmin.metaforms.list().clone().toMutableList()
            val sortedList = list.sortedBy { it.title }
            assertEquals(metaform1Modified.toString(), sortedList[0].toString())
            assertEquals(metaform2Modified.toString(), sortedList[1].toString())
            assertEquals(metaform3Modified.toString(), sortedList[2].toString())

            builder.systemAdmin.metaforms.assertCount(2, MetaformVisibility.pUBLIC)
            builder.systemAdmin.metaforms.assertCount(1, MetaformVisibility.pRIVATE)
            builder.anon.metaforms.assertCount(2, MetaformVisibility.pUBLIC)
        }
    }

    @Test
    @Throws(Exception::class)
    fun listMetaformPrivatePermission() {
        TestBuilder().use { testBuilder ->
            testBuilder.systemAdmin.metaforms.createFromJsonFile("simple-private")

            testBuilder.permissionTestByScopes(
                scope = PermissionScope.METAFORM_MANAGER,
                apiCaller = { authentication: TestBuilderAuthentication, _: Int ->
                    authentication.metaforms.list(MetaformVisibility.pRIVATE)
                },
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun listMetaformPublicPermission() {
        TestBuilder().use { testBuilder ->
            testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")

            testBuilder.permissionTestByScopes(
                scope = PermissionScope.ANONYMOUS,
                apiCaller = { authentication: TestBuilderAuthentication, _: Int ->
                    authentication.metaforms.list(MetaformVisibility.pUBLIC)
                },
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun testUpdateMetaform() {
        TestBuilder().use { builder ->
            val metaform: Metaform = builder.systemAdmin.metaforms.createFromJsonFile("simple")

            val updatePayload = builder.systemAdmin.metaforms.readMetaform("tbnc")
            val updatedMetaform = builder.systemAdmin.metaforms.updateMetaform(metaform.id!!, updatePayload!!)

            assertEquals(metaform.id, updatedMetaform.id)
            assertEquals(MetaformVisibility.pRIVATE, updatedMetaform.visibility)
            assertEquals(1, updatedMetaform.sections!!.size)
            assertEquals("Text, boolean, number, checklist form", updatedMetaform.sections[0].title)
            assertEquals(4, updatedMetaform.sections[0].fields!!.size)
            assertEquals("text", updatedMetaform.sections[0].fields!![0].name)
            assertEquals("boolean", updatedMetaform.sections[0].fields!![1].name)
            assertEquals("number", updatedMetaform.sections[0].fields!![2].name)
            assertEquals("checklist", updatedMetaform.sections[0].fields!![3].name)

            val updatedMetaformModified = updatedMetaform.copy(visibility = MetaformVisibility.pUBLIC)
            val updatedMetaform2 = builder.systemAdmin.metaforms.updateMetaform(metaform.id, updatedMetaformModified)
            assertEquals(MetaformVisibility.pUBLIC, updatedMetaform2.visibility)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testUpdateMetaformNullSlug() {
        TestBuilder().use { builder ->
            val metaform1: Metaform = builder.systemAdmin.metaforms.createFromJsonFile("simple-slug")
            val updatePayload = builder.systemAdmin.metaforms.readMetaform("simple")
            val metaform2 = builder.systemAdmin.metaforms.updateMetaform(metaform1.id!!, updatePayload!!)
            assertEquals(metaform1.slug, metaform2.slug)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testUpdateMetaformDuplicatedSlug() {
        TestBuilder().use { builder ->
            val metaform1: Metaform = builder.systemAdmin.metaforms.createFromJsonFile("simple-slug")
            builder.systemAdmin.metaforms.createFromJsonFile("simple-slug2")
            val updatePayload = builder.test1.metaforms.readMetaform("simple-slug2")
            builder.systemAdmin.metaforms.assertUpdateFailStatus(409, metaform1.id!!, updatePayload!!)
        }
    }

    @Test
    @Throws(Exception::class)
    fun updateMetaformPublicPermission() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")

            testBuilder.permissionTestByScopes(
                scope = PermissionScope.METAFORM_ADMIN,
                apiCaller = { authentication: TestBuilderAuthentication, _: Int ->
                    authentication.metaforms.updateMetaform(metaform.id!!, metaform)
                },
                metaformId = metaform.id
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun deleteMetaformPermission() {
        TestBuilder().use { testBuilder ->
            try {
                val testMetaformId1 = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple").id!!
                val testMetaformId2 = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple").id!!
                val testMetaformId3 = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple").id!!
                val managerAuthentication = testBuilder.createMetaformManagerAuthentication(testMetaformId1, false)
                val adminAuthentication = testBuilder.createMetaformAdminAuthentication(testMetaformId2, false)

                testBuilder.assertApiCallFailStatus(403) { testBuilder.anon.metaforms.delete(testMetaformId1) }
                testBuilder.assertApiCallFailStatus(403) { testBuilder.test1.metaforms.delete(testMetaformId1) }
                testBuilder.assertApiCallFailStatus(403) { managerAuthentication.metaforms.delete(testMetaformId1) }
                testBuilder.assertApiCallFailStatus(204) { adminAuthentication.metaforms.delete(testMetaformId2) }
                testBuilder.assertApiCallFailStatus(204) { testBuilder.systemAdmin.metaforms.delete(testMetaformId3) }

                val testMetaformForbidden1 = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple").id!!
                val testMetaformForbidden2 = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple").id!!
                val adminAuthenticationForbidden = testBuilder.createMetaformAdminAuthentication(testMetaformForbidden1, false)
                val managerAuthenticationForbidden = testBuilder.createMetaformManagerAuthentication(testMetaformForbidden1, false)
                testBuilder.assertApiCallFailStatus(403) { adminAuthenticationForbidden.metaforms.delete(testMetaformForbidden2) }
                testBuilder.assertApiCallFailStatus(403) { managerAuthenticationForbidden.metaforms.delete(testMetaformForbidden2) }
            } finally {
                val metaforms = testBuilder.systemAdmin.metaforms.list()
                metaforms.forEach { metaform ->
                    testBuilder.systemAdmin.metaforms.delete(metaform.id!!)
                }
            }
        }
    }

    @Test
    @Throws(Exception::class)
    fun testAnonymousFindFormOwnerKey() {
        TestBuilder().use { testBuilder ->
            val metaform1: Metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple-owner-keys")
            val metaform2: Metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple-owner-keys")
            testBuilder.anon.metaforms.assertFindFailStatus(403, metaform1.id!!)
            val testReply1 = testBuilder.systemAdmin.replies.createSimpleReply(metaform1.id, "TEST", ReplyMode.REVISION)
            val testReply2 = testBuilder.systemAdmin.replies.createSimpleReply(metaform1.id, "TEST", ReplyMode.REVISION)
            val testReply3 = testBuilder.systemAdmin.replies.createSimpleReply(metaform2.id!!, "TEST", ReplyMode.REVISION)
            testBuilder.anon.metaforms.assertFindFailStatus(403, metaform1.id, testReply1.id, testReply2.ownerKey)
            testBuilder.anon.metaforms.assertFindFailStatus(403, metaform2.id, testReply1.id, testReply1.ownerKey)
            testBuilder.anon.metaforms.assertFindFailStatus(403, metaform1.id, testReply3.id, testReply3.ownerKey)
            testBuilder.anon.metaforms.assertFindFailStatus(403, metaform1.id, null, testReply1.ownerKey)
            testBuilder.anon.metaforms.assertFindFailStatus(403, metaform1.id, testReply1.id, null)
            Assertions.assertNotNull(testBuilder.anon.metaforms.findMetaform(metaform1.id, testReply1.id!!, testReply1.ownerKey))
        }
    }
}