package fi.metatavu.metaform.server.test.functional.tests

import fi.metatavu.metaform.api.client.models.Metaform
import fi.metatavu.metaform.api.client.models.Template
import fi.metatavu.metaform.api.client.models.TemplateData
import fi.metatavu.metaform.api.client.models.TemplateVisibility
import fi.metatavu.metaform.server.test.functional.AbstractTest
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import fi.metatavu.metaform.server.test.functional.builder.resources.MetaformKeycloakResource
import fi.metatavu.metaform.server.test.functional.builder.resources.MysqlResource
import fi.metatavu.metaform.server.test.functional.builder.resources.PdfRendererResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.UUID

/**
 * Tests for draft system
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(MysqlResource::class),
    QuarkusTestResource(MetaformKeycloakResource::class),
    QuarkusTestResource(PdfRendererResource::class)
)
@TestProfile(GeneralTestProfile::class)
class TemplateTestsIT : AbstractTest() {
    @Test
    @Throws(Exception::class)
    fun createTemplate() = TestBuilder().use { testBuilder ->

        //First create a simple metaform from JSON in order to be able to crate a new template
        val testMetaform: Metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")

        //create template from given metaform
        val testTemplate: Template = createTemplateFromMetaform(
                metaform = testMetaform,
                templateVisibility = TemplateVisibility.PUBLIC
        )

        val createdTemplate = testBuilder.systemAdmin.templates.createTemplate(testTemplate)

        assertNotNull(createdTemplate.id)
        assertNotNull(createdTemplate.creatorId)
        assertNotNull(createdTemplate.createdAt)
        assertNotNull(createdTemplate.lastModifierId)
        assertNotNull(createdTemplate.modifiedAt)

        assertNotNull(createdTemplate.data)
        assertNotNull(createdTemplate.data?.sections)
        assertEquals(1,createdTemplate.data?.sections?.size)

        assertNotNull(testTemplate.data?.sections?.get(0))

        testBuilder.systemAdmin.templates.assertSectionEqual(
                expected = testTemplate.data?.sections?.get(0),
                actual = createdTemplate.data?.sections?.get(0)
        )

        //Puutteet???
    }

    @Test
    @Throws(Exception::class)
    fun testFindTemplate() = TestBuilder().use { testBuilder ->

        val testMetaform: Metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")

        val testTemplate: Template = createTemplateFromMetaform(
                metaform = testMetaform,
                templateVisibility = TemplateVisibility.PUBLIC
        )

        val createdTemplate = testBuilder.systemAdmin.templates.createTemplate(testTemplate)
        val foundTemplate = testBuilder.systemAdmin.templates.findTemplate(templateId = createdTemplate.id!!)

        assertEquals(createdTemplate.id, foundTemplate.id)
    }

    @Test
    @Throws(Exception::class)
    fun testListTemplates() = TestBuilder().use { testBuilder ->

        assertEquals(0, testBuilder.systemAdmin.templates.list().size)

        val createdMetaform1 = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
        val createdMetaform2 = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
        val createdMetaform3 = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")

        assertNotNull(createdMetaform1)
        assertNotNull(createdMetaform2)
        assertNotNull(createdMetaform3)

        val createdTemplate1 = createTemplateFromMetaform(
                metaform = createdMetaform1,
                templateVisibility = TemplateVisibility.PUBLIC
        )
        val createdTemplate2 = createTemplateFromMetaform(
                metaform = createdMetaform2,
                templateVisibility = TemplateVisibility.PUBLIC
        )
        val createdTemplate3 = createTemplateFromMetaform(
                metaform = createdMetaform3,
                templateVisibility = TemplateVisibility.PUBLIC
        )

        assertNotNull(createdTemplate1)
        assertNotNull(createdTemplate2)
        assertNotNull(createdTemplate3)

        testBuilder.systemAdmin.templates.createTemplate(createdTemplate1)
        testBuilder.systemAdmin.templates.createTemplate(createdTemplate2)
        testBuilder.systemAdmin.templates.createTemplate(createdTemplate3)

        assertEquals(3, testBuilder.systemAdmin.templates.list().size)
    }

    @Test
    @Throws(Exception::class)
    fun testDeleteTemplate() = TestBuilder().use { testBuilder ->

        val createdMetaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
        assertNotNull(createdMetaform)

        val createdTemplate = testBuilder.systemAdmin.templates.createTemplate(
            template = createTemplateFromMetaform(
                    metaform = createdMetaform,
                    templateVisibility = TemplateVisibility.PUBLIC
            )
        )

        assertNotNull(createdTemplate)
        assertNotNull(createdTemplate.id)

        val foundTemplate =
            testBuilder.systemAdmin.templates.findTemplate(
                    templateId = createdTemplate.id!!
            )

        assertNotNull(foundTemplate)
        assertNotNull(foundTemplate.id)
        testBuilder.systemAdmin.templates.delete(templateId = foundTemplate.id!!)
        testBuilder.systemAdmin.templates.assertFindFailStatus(404, templateId = foundTemplate.id)

    }

    @Test
    @Throws(Exception::class)
    fun testUpdateTemplate() = TestBuilder().use { testBuilder ->

        val createdMetaform: Metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
        assertNotNull(createdMetaform)
        assertNotNull(createdMetaform.id)

        val createdTemplate = testBuilder.systemAdmin.templates.createTemplate(
                createTemplateFromMetaform(
                        metaform = createdMetaform,
                        templateVisibility = TemplateVisibility.PRIVATE)
        )
        assertNotNull(createdTemplate)
        assertNotNull(createdTemplate.id)
        assertEquals(TemplateVisibility.PRIVATE, createdTemplate.visibility)

        val changedTemplate = Template(
                id = createdTemplate.id,
                data = createdTemplate.data,
                visibility = TemplateVisibility.PUBLIC,
                creatorId = createdTemplate.creatorId,
                createdAt = createdTemplate.createdAt
        )

        val updatedTemplate = testBuilder.systemAdmin.templates.updateTemplate(
                id = createdTemplate.id!!,
                template = changedTemplate
        )
        assertNotNull(updatedTemplate)
        assertNotNull(updatedTemplate.id)
        assertNotNull(updatedTemplate.visibility)
        assertEquals(updatedTemplate.id, createdTemplate.id)
        assertEquals(updatedTemplate.createdAt, createdTemplate.createdAt)
        assertEquals(TemplateVisibility.PUBLIC, updatedTemplate.visibility)

        val foundTemplate = testBuilder.systemAdmin.templates.findTemplate(
                templateId = updatedTemplate.id!!
        )

        assertNotNull(foundTemplate)
        assertNotNull(foundTemplate.id)
        assertNotNull(foundTemplate.visibility)
        assertEquals(updatedTemplate.visibility, foundTemplate.visibility)
        assertEquals(updatedTemplate.createdAt, foundTemplate.createdAt)
        assertNotEquals(updatedTemplate.modifiedAt, foundTemplate.modifiedAt)
    }

    private fun createTemplateFromMetaform(metaform: Metaform, templateVisibility: TemplateVisibility): Template {
        val testTemplateData = TemplateData(
                title = metaform.title,
                allowAnonymous = metaform.allowAnonymous,
                defaultPermissionGroups = metaform.defaultPermissionGroups,
                exportThemeId = metaform.exportThemeId,
                sections = metaform.sections
        )

        return Template(
                data = testTemplateData,
                visibility = templateVisibility,
                createdAt = metaform.createdAt,
                modifiedAt = metaform.modifiedAt,
                creatorId = metaform.creatorId,
                lastModifierId = metaform.lastModifierId
        )
    }
}