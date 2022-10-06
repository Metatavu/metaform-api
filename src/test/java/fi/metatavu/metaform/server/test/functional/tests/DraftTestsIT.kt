package fi.metatavu.metaform.server.test.functional.tests

import fi.metatavu.metaform.api.client.models.Draft
import fi.metatavu.metaform.api.client.models.Metaform
import fi.metatavu.metaform.server.test.functional.AbstractTest
import fi.metatavu.metaform.server.test.functional.builder.PermissionScope
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import fi.metatavu.metaform.server.test.functional.builder.auth.TestBuilderAuthentication
import fi.metatavu.metaform.server.test.functional.builder.resources.MetaformKeycloakResource
import fi.metatavu.metaform.server.test.functional.builder.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * Tests for draft system
 */
@QuarkusTest
@QuarkusTestResource.List(
        QuarkusTestResource(MysqlResource::class),
        QuarkusTestResource(MetaformKeycloakResource::class)
)
@TestProfile(GeneralTestProfile::class)
class DraftTestsIT : AbstractTest() {
    @Test
    @Throws(Exception::class)
    fun createDraft() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val draftData: MutableMap<String, Any> = HashMap()
            draftData["text"] = "draft value"
            val createdDraft: Draft = testBuilder.test1.drafts.createDraft(metaform, draftData)
            Assertions.assertNotNull(createdDraft)
            Assertions.assertNotNull(createdDraft.id)
            Assertions.assertEquals("draft value", createdDraft.data["text"])
            val foundDraft: Draft = testBuilder.test1.drafts.findDraft(metaform.id!!, createdDraft.id!!)
            Assertions.assertNotNull(foundDraft)
            Assertions.assertEquals(createdDraft.id, foundDraft.id)
            Assertions.assertEquals(createdDraft.data["text"], foundDraft.data["text"])
        }
    }

    @Test
    @Throws(Exception::class)
    fun updateDraft() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
            val draftData: MutableMap<String, Any> = HashMap()
            draftData["text"] = "draft value"
            val createdDraft: Draft = testBuilder.test1.drafts.createDraft(metaform, draftData)
            Assertions.assertNotNull(createdDraft)
            Assertions.assertNotNull(createdDraft.id)
            Assertions.assertEquals("draft value", createdDraft.data["text"])
            draftData["text"] = "updated value"
            val updatePayload = Draft(draftData, createdDraft.id, createdDraft.createdAt, createdDraft.modifiedAt)
            val updateDraft: Draft = testBuilder.test1.drafts.updateDraft(metaform.id!!, createdDraft.id!!, updatePayload)
            Assertions.assertNotNull(updateDraft)
            Assertions.assertEquals(createdDraft.id, updateDraft.id)
            Assertions.assertEquals("updated value", updateDraft.data["text"])
            val foundDraft: Draft = testBuilder.test1.drafts.findDraft(metaform.id, createdDraft.id)
            Assertions.assertNotNull(foundDraft)
            Assertions.assertEquals(updateDraft.id, foundDraft.id)
            Assertions.assertEquals(updateDraft.data["text"], foundDraft.data["text"])
        }
    }


    @Test
    @Throws(Exception::class)
    fun createDraftPermission() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")

            testBuilder.permissionTestByScopes(
                scope = PermissionScope.USER,
                apiCaller = { authentication: TestBuilderAuthentication, _: Int ->
                    val draftData: MutableMap<String, Any> = HashMap()
                    draftData["text"] = "draft value"
                    authentication.drafts.createDraft(metaform, draftData)
                }
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun findDraftPermission() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")

            testBuilder.permissionTestByScopes(
                scope = PermissionScope.USER,
                apiCaller = { authentication: TestBuilderAuthentication, _: Int ->
                    val draftData: MutableMap<String, Any> = HashMap()
                    draftData["text"] = "draft value"
                    val draft = authentication.drafts.createDraft(metaform, draftData)
                    authentication.drafts.findDraft(metaform.id!!, draft.id!!)
                }
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun updateDraftPermission() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")

            testBuilder.permissionTestByScopes(
                scope = PermissionScope.USER,
                apiCaller = { authentication: TestBuilderAuthentication, _: Int ->
                    val draftData: MutableMap<String, Any> = HashMap()
                    draftData["text"] = "draft value"
                    val draft = authentication.drafts.createDraft(metaform, draftData)
                    authentication.drafts.updateDraft(metaform.id!!, draft.id!!, draft)
                }
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun deleteDraftPermission() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")

            testBuilder.permissionTestByScopes(
                scope = PermissionScope.USER,
                apiCaller = { authentication: TestBuilderAuthentication, _: Int ->
                    val draftData: MutableMap<String, Any> = HashMap()
                    draftData["text"] = "draft value"
                    val draft = authentication.drafts.createDraft(metaform, draftData)
                    authentication.drafts.deleteDraft(metaform.id!!, draft.id!!)
                }
            )
        }
    }
}