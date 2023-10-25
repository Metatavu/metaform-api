package fi.metatavu.metaform.server.test.functional.tests

import fi.metatavu.metaform.api.client.models.*
import fi.metatavu.metaform.server.rest.ReplyMode
import fi.metatavu.metaform.server.test.functional.AbstractTest
import fi.metatavu.metaform.server.test.functional.builder.PermissionScope
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import fi.metatavu.metaform.server.test.functional.builder.auth.TestBuilderAuthentication
import fi.metatavu.metaform.server.test.functional.builder.resources.MetaformKeycloakResource
import fi.metatavu.metaform.server.test.functional.builder.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

/**
 * Tests for Metaforms functionality
 */
@QuarkusTest
@QuarkusTestResource.List(
        QuarkusTestResource(MysqlResource::class),
        QuarkusTestResource(MetaformKeycloakResource::class)
)
@TestProfile(GeneralTestProfile::class)
class MetaformTestsIT : AbstractTest() {

    @Test
    fun testCreateMetaform() {
        TestBuilder().use { builder ->
            val metaform1: Metaform = builder.systemAdmin.metaforms.createFromJsonFile("simple")
            val metaform1CreatedAtDate = OffsetDateTime.parse(metaform1.createdAt).toLocalDate()
            val metaform1ModifiedAtDate = OffsetDateTime.parse(metaform1.modifiedAt).toLocalDate()
            assertNotNull(metaform1)
            assertNotNull(metaform1.id)
            assertNotNull(metaform1.slug)
            assertNotNull(metaform1.creatorId)
            assertNotNull(metaform1.lastModifierId)
            assertEquals("Simple", metaform1.title)
            assertEquals(MetaformVisibility.PUBLIC, metaform1.visibility)
            assertEquals(1, metaform1.sections!!.size)
            assertEquals("Simple form", metaform1.sections[0].title)
            assertEquals(1, metaform1.sections[0].fields!!.size)
            assertEquals("text", metaform1.sections[0].fields!![0].name)
            assertEquals("text", metaform1.sections[0].fields!![0].type.toString())
            assertEquals("Text field", metaform1.sections[0].fields!![0].title)
            assertEquals(true, metaform1.allowDrafts)
            assertEquals(LocalDate.now(), metaform1CreatedAtDate)
            assertEquals(LocalDate.now(), metaform1ModifiedAtDate)
            assertEquals(metaform1.creatorId, metaform1.lastModifierId)
            val metaform2: Metaform = builder.systemAdmin.metaforms.createFromJsonFile("simple-slug")
            val metaform2CreatedAtDate = OffsetDateTime.parse(metaform2.createdAt).toLocalDate()
            val metaform2ModifiedAtDate = OffsetDateTime.parse(metaform2.modifiedAt).toLocalDate()
            assertNotNull(metaform2)
            assertNotNull(metaform2.id)
            assertNotNull(metaform2.creatorId)
            assertNotNull(metaform2.lastModifierId)
            assertEquals("Simple", metaform2.title)
            assertEquals(MetaformVisibility.PRIVATE, metaform2.visibility)
            assertEquals("simple-slug-0", metaform2.slug)
            assertEquals(1, metaform2.sections!!.size)
            assertEquals("Simple form", metaform2.sections[0].title)
            assertEquals(1, metaform2.sections[0].fields!!.size)
            assertEquals("text", metaform2.sections[0].fields!![0].name)
            assertEquals("text", metaform2.sections[0].fields!![0].type.toString())
            assertEquals("Text field", metaform2.sections[0].fields!![0].title)
            assertEquals(true, metaform2.allowDrafts)
            assertEquals(LocalDate.now(), metaform2CreatedAtDate)
            assertEquals(LocalDate.now(), metaform2ModifiedAtDate)
            assertEquals(metaform2.creatorId, metaform2.lastModifierId)

            assertEquals(true, metaform1.sections[0].fields!![0].classifiers!!.contains("a"))
            assertEquals(true, metaform1.sections[0].fields!![0].classifiers!!.contains("b"))
            assertEquals(true, metaform1.sections[0].fields!![0].classifiers!!.contains("c"))
        }
    }

    @Test
    fun testCreateMetaformInvalidPermissionGroups() {
        TestBuilder().use { builder ->
            val form = builder.systemAdmin.metaforms.readMetaform("simple-invalid-permission-groups")
            assertNotNull(form)
            builder.systemAdmin.metaforms.assertCreateFailStatus(400, form!!)
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
    fun createMetaformPermission() {
        TestBuilder().use { testBuilder ->
            try {
                testBuilder.permissionTestByScopes(
                    scope = PermissionScope.SYSTEM_ADMIN,
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
            val foundMetaformById: Metaform = builder.systemAdmin.metaforms.findMetaform(
                metaformSlug = null,
                metaformId = metaform.id!!,
                replyId = null,
                ownerKey = null
            )
            val foundMetaformBySlug: Metaform = builder.systemAdmin.metaforms.findMetaform(
                metaformSlug = metaform.slug!!,
                metaformId = null,
                replyId = null,
                ownerKey = null
            )

            assertEquals(metaform.toString(), foundMetaformById.toString())
            assertEquals(metaform.toString(), foundMetaformBySlug.toString())
        }
    }

    @Test
    @Throws(Exception::class)
    fun testFindMetaformNotFound() {
        TestBuilder().use { builder ->
            builder.systemAdmin.metaforms.assertFindFailStatus(
                expectedStatus = 404,
                metaformId = UUID.randomUUID()
            )
            builder.systemAdmin.metaforms.assertFindFailStatus(
                expectedStatus = 404,
                metaformSlug = ""
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun testFindMetaformBadRequest() {
        TestBuilder().use { builder ->
            builder.systemAdmin.metaforms.assertFindFailStatus(
                expectedStatus = 400
            )
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
                    authentication.metaforms.findMetaform(
                        metaformSlug = null,
                        metaformId = metaform.id!!,
                        replyId = null,
                        ownerKey = null
                    )
                },
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun findMetaformBySlugPublicPermission() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")

            testBuilder.permissionTestByScopes(
                scope = PermissionScope.ANONYMOUS,
                apiCaller = { authentication: TestBuilderAuthentication, _: Int ->
                    authentication.metaforms.findMetaform(
                        metaformSlug = metaform.slug!!,
                        metaformId = null,
                        replyId = null,
                        ownerKey = null
                    )
                }
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun findMetaformBySlugPrivatePermission() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple-private")

            testBuilder.permissionTestByScopes(
                scope = PermissionScope.USER,
                apiCaller = { authentication: TestBuilderAuthentication, _: Int ->
                    authentication.metaforms.findMetaform(
                        metaformSlug = metaform.slug!!,
                        metaformId = null,
                        replyId = null,
                        ownerKey = null
                    )
                },
                metaformId = metaform.id
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun findMetaformPrivatePermission() {
        TestBuilder().use { testBuilder ->
            val metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple-private")

            testBuilder.permissionTestByScopes(
                scope = PermissionScope.USER,
                apiCaller = { authentication: TestBuilderAuthentication, _: Int ->
                    authentication.metaforms.findMetaform(
                        metaformSlug = null,
                        metaformId = metaform.id!!,
                        replyId = null,
                        ownerKey = null
                    )
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
                    visibility = MetaformVisibility.PRIVATE
            )
            val metaform2Modified = metaform2.copy(
                    title="second",
                    slug="second-slug-0",
                    visibility = MetaformVisibility.PUBLIC
            )
            val metaform3Modified = metaform3.copy(
                title="third",
                slug="third-slug-0",
                visibility = MetaformVisibility.PUBLIC
            )
            builder.systemAdmin.metaforms.updateMetaform(metaform1.id!!, metaform1Modified)
            builder.systemAdmin.metaforms.updateMetaform(metaform2.id!!, metaform2Modified)
            builder.systemAdmin.metaforms.updateMetaform(metaform3.id!!, metaform3Modified)
            val list: MutableList<Metaform> = builder.systemAdmin.metaforms.list().clone().toMutableList()
            val sortedList = list.sortedBy { it.title }

            assertEquals(metaform1Modified.title, sortedList[0].title)
            assertEquals(metaform1Modified.slug, sortedList[0].slug)
            assertEquals(metaform1Modified.visibility, sortedList[0].visibility)
            assertEquals(metaform2Modified.title, sortedList[1].title)
            assertEquals(metaform2Modified.slug, sortedList[1].slug)
            assertEquals(metaform2Modified.visibility, sortedList[1].visibility)
            assertEquals(metaform3Modified.title, sortedList[2].title)
            assertEquals(metaform3Modified.slug, sortedList[2].slug)
            assertEquals(metaform3Modified.visibility, sortedList[2].visibility)

            builder.systemAdmin.metaforms.assertCount(2, MetaformVisibility.PUBLIC)
            builder.systemAdmin.metaforms.assertCount(1, MetaformVisibility.PRIVATE)
            builder.anon.metaforms.assertCount(2, MetaformVisibility.PUBLIC)
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
                    authentication.metaforms.list(MetaformVisibility.PRIVATE)
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
                    authentication.metaforms.list(MetaformVisibility.PUBLIC)
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
            builder.systemAdmin.metaforms.updateMetaform(metaform.id!!, updatePayload!!)
            val updatedMetaform = builder.systemAdmin.metaforms.findMetaform(null, metaform.id, null, null)

            assertEquals(metaform.id, updatedMetaform.id)
            assertEquals(MetaformVisibility.PRIVATE, updatedMetaform.visibility)
            assertEquals(1, updatedMetaform.sections!!.size)
            assertEquals("Text, boolean, number, checklist form", updatedMetaform.sections[0].title)
            assertEquals(4, updatedMetaform.sections[0].fields!!.size)
            assertEquals("text", updatedMetaform.sections[0].fields!![0].name)
            assertEquals("boolean", updatedMetaform.sections[0].fields!![1].name)
            assertEquals("number", updatedMetaform.sections[0].fields!![2].name)
            assertEquals("checklist", updatedMetaform.sections[0].fields!![3].name)
            assertEquals(metaform.createdAt, updatedMetaform.createdAt)
            assertEquals(metaform.createdAt, updatedMetaform.createdAt)
            assertNotEquals(metaform.modifiedAt, updatedMetaform.modifiedAt)
            assertNotEquals(updatedMetaform.createdAt, updatedMetaform.modifiedAt)

            val updatedMetaformModified = updatedMetaform.copy(visibility = MetaformVisibility.PUBLIC)
            builder.systemAdmin.metaforms.updateMetaform(metaform.id, updatedMetaformModified)
            val updatedMetaform2 = builder.systemAdmin.metaforms.findMetaform(null, metaform.id, null, null)

            assertEquals(MetaformVisibility.PUBLIC, updatedMetaform2.visibility)
            assertEquals(updatedMetaform2.createdAt, updatedMetaform.createdAt)
            assertNotEquals(updatedMetaform2.modifiedAt, updatedMetaform.modifiedAt)
        }
    }

    @Test
    fun testUpdateMetaformInvalidPermissionGroups() {
        TestBuilder().use { builder ->
            val metaform: Metaform = builder.systemAdmin.metaforms.createFromJsonFile("simple")
            val invalidForm = builder.systemAdmin.metaforms.readMetaform("simple-invalid-permission-groups")

            builder.systemAdmin.metaforms.assertUpdateFailStatus(400, metaform.id!!, metaform.copy(
                sections = invalidForm?.sections
            ))
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
    fun testDeleteMetaform() {
        TestBuilder().use { testBuilder ->
            try {
                val testMetaform1 = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
                testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")

                val draftData: MutableMap<String, Any> = HashMap()
                draftData["text"] = "draft value"
                val createdDraft: Draft = testBuilder.systemAdmin.drafts.createDraft(testMetaform1, draftData, false)
                val createdEmailNotification = testBuilder.systemAdmin.emailNotifications.createEmailNotification(
                    metaformId = testMetaform1.id!!,
                    subjectTemplate = "Simple subject",
                    contentTemplate = "Simple content",
                    emails = emptyList(),
                    notifyIf = null,
                    addClosable = false
                )
                val foundMetaform = testBuilder.systemAdmin.metaforms.findMetaform(
                    metaformId=testMetaform1.id,
                    replyId = null,
                    ownerKey = null,
                    metaformSlug = null
                )
                val foundDraft = testBuilder.systemAdmin.drafts.findDraft(testMetaform1.id, createdDraft.id!!)
                assertNotNull(foundMetaform)
                assertNotNull(foundMetaform.id)
                assertNotNull(foundDraft)
                assertNotNull(foundDraft.id)
                assertNotNull(createdEmailNotification.id)
                testBuilder.systemAdmin.metaforms.delete(foundMetaform.id!!)
                testBuilder.systemAdmin.metaforms.assertFindFailStatus(404, metaformId = foundMetaform.id)
                testBuilder.systemAdmin.drafts.assertFindFailStatus(404, metaformId = foundMetaform.id, draftId = createdDraft.id)
                testBuilder.systemAdmin.emailNotifications.assertFindFailStatus(404, createdEmailNotification.id!!, foundMetaform.id)
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
            testBuilder.anon.metaforms.assertFindFailStatus(
                expectedStatus = 403,
                metaformId = metaform1.id!!
            )
            val testReply1 = testBuilder.systemAdmin.replies.createSimpleReply(metaform1.id, "TEST", ReplyMode.REVISION)
            val testReply2 = testBuilder.systemAdmin.replies.createSimpleReply(metaform1.id, "TEST", ReplyMode.REVISION)
            val testReply3 = testBuilder.systemAdmin.replies.createSimpleReply(metaform2.id!!, "TEST", ReplyMode.REVISION)
            testBuilder.anon.metaforms.assertFindFailStatus(
                expectedStatus = 403,
                metaformId = metaform1.id,
                replyId = testReply1.id,
                ownerKey = testReply2.ownerKey
            )
            testBuilder.anon.metaforms.assertFindFailStatus(
                expectedStatus = 403,
                metaformId = metaform2.id,
                replyId = testReply1.id,
                ownerKey = testReply1.ownerKey
            )
            testBuilder.anon.metaforms.assertFindFailStatus(
                expectedStatus = 403,
                metaformId = metaform1.id,
                replyId = testReply3.id,
                ownerKey = testReply3.ownerKey
            )
            testBuilder.anon.metaforms.assertFindFailStatus(
                expectedStatus = 403,
                metaformId = metaform1.id,
                replyId = null,
                ownerKey = testReply1.ownerKey
            )
            testBuilder.anon.metaforms.assertFindFailStatus(
                expectedStatus = 403,
                metaformId = metaform1.id,
                replyId = testReply1.id,
                ownerKey = null
            )
            assertNotNull(testBuilder.anon.metaforms.findMetaform(
                metaformSlug = null,
                metaformId = metaform1.id,
                replyId = testReply1.id!!,
                ownerKey = testReply1.ownerKey
            ))
        }
    }

    @Test
    fun testListMetaformByRole() {
        TestBuilder().use { builder ->
            val forms = (0..3).map {
                builder.systemAdmin.metaforms.createFromJsonFile("simple")
            }

            val publicForms = forms.subList(0, 1).map {
                builder.systemAdmin.metaforms.updateMetaform(it.id!!, it.copy(visibility = MetaformVisibility.PUBLIC))
            }

            val privateForms = forms.subList(2, 3).map {
                builder.systemAdmin.metaforms.updateMetaform(it.id!!, it.copy(visibility = MetaformVisibility.PRIVATE))
            }

            builder.test1.metaforms.assertCount(0, visibility = null, memberRole = MetaformMemberRole.ADMINISTRATOR)
            builder.test1.metaforms.assertCount(0, visibility = null, memberRole = MetaformMemberRole.MANAGER)
            builder.test2.metaforms.assertCount(0, visibility = null, memberRole = MetaformMemberRole.ADMINISTRATOR)
            builder.test2.metaforms.assertCount(0, visibility = null, memberRole = MetaformMemberRole.MANAGER)

            val member1 = builder.systemAdmin.metaformMembers.create(publicForms[0].id!!, MetaformMember(
                email = "user1@example.com",
                firstName = "Test",
                lastName = "User",
                role = MetaformMemberRole.ADMINISTRATOR
            )
            )

            val member2 = builder.systemAdmin.metaformMembers.create(privateForms[0].id!!, MetaformMember(
                email = "user2@example.com",
                firstName = "Test",
                lastName = "User",
                role = MetaformMemberRole.MANAGER
            )
            )

            builder.test1.metaforms.assertCount(1, visibility = null, memberRole = MetaformMemberRole.ADMINISTRATOR)
            builder.test1.metaforms.assertCount(1, visibility = null, memberRole = MetaformMemberRole.MANAGER)
            builder.test2.metaforms.assertCount(0, visibility = null, memberRole = MetaformMemberRole.ADMINISTRATOR)
            builder.test2.metaforms.assertCount(1, visibility = null, memberRole = MetaformMemberRole.MANAGER)

            builder.systemAdmin.metaformMembers.delete(metaformId = publicForms[0].id!!, metaformMemberId = member1.id!!)
            builder.systemAdmin.metaformMembers.delete(metaformId = privateForms[0].id!!, metaformMemberId = member2.id!!)

            builder.test1.metaforms.assertCount(0, visibility = null, memberRole = MetaformMemberRole.ADMINISTRATOR)
            builder.test1.metaforms.assertCount(0, visibility = null, memberRole = MetaformMemberRole.MANAGER)
            builder.test2.metaforms.assertCount(0, visibility = null, memberRole = MetaformMemberRole.ADMINISTRATOR)
            builder.test2.metaforms.assertCount(0, visibility = null, memberRole = MetaformMemberRole.MANAGER)
        }
    }

    @Test
    fun testScheduledFields() {
        TestBuilder().use { builder ->
            val testMetaform = builder.systemAdmin.metaforms.createFromJsonFile("simple-scheduled-field")

            assertNotNull(testMetaform)

            assertNotNull(testMetaform.sections!![0].fields!![0].schedule!!.startTime)
            assertNotNull(testMetaform.sections[0].fields!![0].schedule!!.endTime)
            assertNull(testMetaform.sections[0].fields!![1].schedule!!.startTime)
            assertNull(testMetaform.sections[0].fields!![1].schedule!!.endTime)
            assertNotNull(testMetaform.sections!![0].fields!![2].schedule!!.startTime)
            assertNull(testMetaform.sections[0].fields!![2].schedule!!.endTime)

            val scheduleStartDateTime = OffsetDateTime.parse(testMetaform.sections!![0].fields!![0].schedule!!.startTime)
            val expectedStartDateTime = OffsetDateTime.parse("2023-01-01T10:33:19.51Z")
            val scheduleEndDateTime = OffsetDateTime.parse(testMetaform.sections!![0].fields!![0].schedule!!.endTime)
            val expectedEndDateTime = OffsetDateTime.parse("2023-12-31T11:33:19.51Z")

            assertEquals(scheduleStartDateTime, expectedStartDateTime)
            assertEquals(scheduleEndDateTime, expectedEndDateTime)

            val scheduleStartDateTime2 = OffsetDateTime.parse(testMetaform.sections!![0].fields!![2].schedule!!.startTime)
            val expectedStartDateTime2 = OffsetDateTime.parse("2023-02-01T10:33:19.51Z")

            assertEquals(scheduleStartDateTime2, expectedStartDateTime2)
        }
    }
}