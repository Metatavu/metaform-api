package fi.metatavu.metaform.server.test.functional.tests

import fi.metatavu.metaform.api.client.models.Metaform
import fi.metatavu.metaform.server.test.functional.AbstractTest
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import fi.metatavu.metaform.server.test.functional.builder.resources.KeycloakResource
import fi.metatavu.metaform.server.test.functional.builder.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
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
            val metaform1: Metaform = builder.metaformAdmin.metaforms.createFromJsonFile("simple")
            assertNotNull(metaform1)
            assertNotNull(metaform1.id)
            assertNotNull(metaform1.slug)
            assertEquals("Simple", metaform1.title)
            assertEquals(1, metaform1.sections!!.size)
            assertEquals("Simple form", metaform1.sections[0].title)
            assertEquals(1, metaform1.sections[0].fields!!.size)
            assertEquals("text", metaform1.sections[0].fields!![0].name)
            assertEquals("text", metaform1.sections[0].fields!![0].type.toString())
            assertEquals("Text field", metaform1.sections[0].fields!![0].title)
            assertEquals(true, metaform1.allowDrafts)
            val metaform2: Metaform = builder.metaformAdmin.metaforms.createFromJsonFile("simple-slug")
            assertNotNull(metaform2)
            assertNotNull(metaform2.id)
            assertEquals("Simple", metaform2.title)
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
            val parsedMetaform = builder.metaformAdmin.metaforms.readMetaform("simple-slug-invalid")
            assertNotNull(parsedMetaform)
            builder.metaformAdmin.metaforms.assertCreateFailStatus(409, parsedMetaform!!)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testCreateDuplicatedSlugMetaform() {
        TestBuilder().use { builder ->
            builder.metaformAdmin.metaforms.createFromJsonFile("simple-slug")
            val parsedMetaform2 = builder.metaformAdmin.metaforms.readMetaform("simple-slug")
            builder.metaformAdmin.metaforms.assertCreateFailStatus(409, parsedMetaform2!!)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testCreateMetaformScript() {
        TestBuilder().use { builder ->
            val metaform: Metaform = builder.metaformAdmin.metaforms.createFromJsonFile("simple-script")
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
    fun testFindMetaform() {
        TestBuilder().use { builder ->
            val metaform: Metaform = builder.metaformAdmin.metaforms.createFromJsonFile("simple")
            val foundMetaform: Metaform = builder.metaformAdmin.metaforms.findMetaform(metaform.id!!, null, null)
            assertEquals(metaform.toString(), foundMetaform.toString())
        }
    }

    @Test
    @Throws(Exception::class)
    fun testListMetaform() {
        TestBuilder().use { builder ->
            assertEquals(0, builder.metaformAdmin.metaforms.list().size)
            val metaform1: Metaform = builder.metaformAdmin.metaforms.createFromJsonFile("simple")
            val metaform2: Metaform = builder.metaformAdmin.metaforms.createFromJsonFile("simple")
            val metaform1Modified = metaform1.copy(
                    title="first",
                    slug="first-slug-0"
            )
            val metaform2Modified = metaform2.copy(
                    title="second",
                    slug="second-slug-0"
            )
            builder.metaformAdmin.metaforms.updateMetaform(metaform1.id!!, metaform1Modified)
            builder.metaformAdmin.metaforms.updateMetaform(metaform2.id!!, metaform2Modified)
            val list: MutableList<Metaform> = builder.metaformAdmin.metaforms.list().clone().toMutableList()
            val sortedList = list.sortedBy { it.title }
            assertEquals(metaform1Modified.toString(), sortedList[0].toString())
            assertEquals(metaform2Modified.toString(), sortedList[1].toString())
        }
    }

    @Test
    @Throws(Exception::class)
    fun testUpdateMetaform() {
        TestBuilder().use { builder ->
            val metaform: Metaform = builder.metaformAdmin.metaforms.createFromJsonFile("simple")

            val updatePayload = builder.metaformAdmin.metaforms.readMetaform("tbnc")
            val updatedMetaform = builder.metaformAdmin.metaforms.updateMetaform(metaform.id!!, updatePayload!!)

            assertEquals(metaform.id, updatedMetaform.id)
            assertEquals(1, updatedMetaform.sections!!.size)
            assertEquals("Text, boolean, number, checklist form", updatedMetaform.sections[0].title)
            assertEquals(4, updatedMetaform.sections[0].fields!!.size)
            assertEquals("text", updatedMetaform.sections[0].fields!![0].name)
            assertEquals("boolean", updatedMetaform.sections[0].fields!![1].name)
            assertEquals("number", updatedMetaform.sections[0].fields!![2].name)
            assertEquals("checklist", updatedMetaform.sections[0].fields!![3].name)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testUpdateMetaformNullSlug() {
        TestBuilder().use { builder ->
            val metaform1: Metaform = builder.metaformAdmin.metaforms.createFromJsonFile("simple-slug")
            val updatePayload = builder.metaformAdmin.metaforms.readMetaform("simple")
            val metaform2 = builder.metaformAdmin.metaforms.updateMetaform(metaform1.id!!, updatePayload!!)
            assertEquals(metaform1.slug, metaform2.slug)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testUpdateMetaformDuplicatedSlug() {
        TestBuilder().use { builder ->
            val metaform1: Metaform = builder.metaformAdmin.metaforms.createFromJsonFile("simple-slug")
            builder.metaformAdmin.metaforms.createFromJsonFile("simple-slug2")
            val updatePayload = builder.test1.metaforms.readMetaform("simple-slug2")
            builder.metaformAdmin.metaforms.assertUpdateFailStatus(409, metaform1.id!!, updatePayload!!)
        }
    }
}