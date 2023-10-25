package fi.metatavu.metaform.server.test.functional.tests

import fi.metatavu.metaform.api.client.models.*
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

        val testMetaform: Metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
        assertNotNull(testMetaform)
        assertNotNull(testMetaform.id)

        val testTemplate: Template = createTemplateFromMetaform(
            metaform = testMetaform,
            templateVisibility = TemplateVisibility.PUBLIC
        )

        val createdTemplate = testBuilder.systemAdmin.templates.createTemplate(template = testTemplate)

        assertNotNull(createdTemplate.id)
        assertEquals(createdTemplate.visibility, TemplateVisibility.PUBLIC)
        assertNotNull(createdTemplate.creatorId)
        assertNotNull(createdTemplate.createdAt)
        assertNotNull(createdTemplate.lastModifierId)
        assertNotNull(createdTemplate.modifiedAt)

        assertNotNull(createdTemplate.data)
        assertNotNull(createdTemplate.data.sections)
        assertEquals(1,createdTemplate.data.sections?.size)

        assertNotNull(testTemplate.data.sections?.get(0))

        testBuilder.systemAdmin.templates.assertSectionEqual(
            expected = testTemplate.data.sections?.get(0),
            actual = createdTemplate.data.sections?.get(0)
        )
    }

    @Test
    @Throws(Exception::class)
    fun testFindTemplate() = TestBuilder().use { testBuilder ->

        val testMetaform: Metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
        val testTemplate: Template = createTemplateFromMetaform(
            metaform = testMetaform,
            templateVisibility = TemplateVisibility.PUBLIC
        )

        val createdTemplate = testBuilder.systemAdmin.templates.createTemplate(template = testTemplate)

        val foundTemplate = testBuilder.systemAdmin.templates.findTemplate(templateId = createdTemplate.id!!)

        assertEquals(createdTemplate.id, foundTemplate.id)
        assertEquals(createdTemplate.visibility, foundTemplate.visibility)
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

        testBuilder.systemAdmin.templates.createTemplate(template = createdTemplate1)
        testBuilder.systemAdmin.templates.createTemplate(template = createdTemplate2)
        testBuilder.systemAdmin.templates.createTemplate(template = createdTemplate3)

        assertEquals(3, testBuilder.systemAdmin.templates.list().size)

        for (template in testBuilder.systemAdmin.templates.list()) {
            assertNotNull(template.id)
        }
    }
    
    @Test
    @Throws(Exception::class)
    fun testTemplateVisibility() = TestBuilder().use { testBuilder ->

        val createdMetaform1 = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
        val createdMetaform2 = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
        val createdMetaform3 = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")

        assertNotNull(createdMetaform1)
        assertNotNull(createdMetaform2)
        assertNotNull(createdMetaform3)

        val template1 = createTemplateFromMetaform(metaform = createdMetaform1, templateVisibility = TemplateVisibility.PUBLIC)
        val template2 = createTemplateFromMetaform(metaform = createdMetaform2, templateVisibility = TemplateVisibility.PUBLIC)
        val template3 = createTemplateFromMetaform(metaform = createdMetaform3, templateVisibility = TemplateVisibility.PUBLIC)

        assertNotNull(template1)
        assertNotNull(template1.data)
        assertNotNull(template1.visibility)

        val createdTemplate = testBuilder.systemAdmin.templates.createTemplate(template = template1)
        testBuilder.systemAdmin.templates.createTemplate(template = template2)
        testBuilder.systemAdmin.templates.createTemplate(template = template3)

        assertEquals(3, testBuilder.systemAdmin.templates.list(TemplateVisibility.PUBLIC).size)

        val updatedTemplate = createdTemplate.copy(visibility = TemplateVisibility.PRIVATE)

        testBuilder.systemAdmin.templates.updateTemplate(
                id = createdTemplate.id!!,
                template = updatedTemplate
        )

        assertEquals(2, testBuilder.systemAdmin.templates.list(TemplateVisibility.PUBLIC).size)

    }

    @Test
    @Throws(Exception::class)
    fun testDeleteTemplate() = TestBuilder().use { testBuilder ->

        val createdMetaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
        assertNotNull(createdMetaform)

        val template1 = createTemplateFromMetaform(
                metaform = createdMetaform,
                templateVisibility = TemplateVisibility.PUBLIC
        )

        val createdTemplate = testBuilder.systemAdmin.templates.createTemplate(template = template1)

        assertNotNull(createdTemplate)
        assertNotNull(createdTemplate.id)

        val foundTemplate = testBuilder.systemAdmin.templates.findTemplate(templateId = createdTemplate.id!!)

        assertNotNull(foundTemplate)
        assertNotNull(foundTemplate.id)
        testBuilder.systemAdmin.templates.delete(templateId = foundTemplate.id!!)
        testBuilder.systemAdmin.templates.assertFindFailStatus(404, templateId = foundTemplate.id)

    }

    @Test
    @Throws(Exception::class)
    fun testUpdateTemplate() = TestBuilder().use { testBuilder ->

        val simpleMetaform: Metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
        assertNotNull(simpleMetaform)
        assertNotNull(simpleMetaform.id)
        val tbncMetaform: Metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("tbnc")
        assertNotNull(tbncMetaform)
        assertNotNull(tbncMetaform.id)

        val createdTemplate = testBuilder.systemAdmin.templates.createTemplate(
                template = createTemplateFromMetaform(
                        metaform = simpleMetaform,
                        templateVisibility = TemplateVisibility.PRIVATE
                )
        )

        assertNotNull(createdTemplate)
        assertNotNull(createdTemplate.id)
        assertEquals(TemplateVisibility.PRIVATE, createdTemplate.visibility)

        val changedTemplate: Template = createdTemplate.copy(
                visibility = TemplateVisibility.PUBLIC,
                data = TemplateData(
                        title = tbncMetaform.title,
                        exportThemeId = tbncMetaform.exportThemeId,
                        sections = tbncMetaform.sections
                )
        )

        val updatedTemplate: Template = testBuilder.systemAdmin.templates.updateTemplate(
            id = createdTemplate.id!!,
            template = changedTemplate
        )

        assertNotNull(updatedTemplate)
        assertNotNull(updatedTemplate.id)
        assertNotNull(updatedTemplate.visibility)

        testBuilder.systemAdmin.templates.assertSectionEqual(
                expected = updatedTemplate.data.sections?.get(0),
                actual = changedTemplate.data.sections?.get(0)
        )

        assertEquals(updatedTemplate.id, changedTemplate.id)
        assertEquals(updatedTemplate.visibility, changedTemplate.visibility)

        assertEquals(updatedTemplate.id, createdTemplate.id)
        assertEquals(updatedTemplate.createdAt, createdTemplate.createdAt)
        assertEquals(updatedTemplate.visibility, TemplateVisibility.PUBLIC)

        val foundTemplate = testBuilder.systemAdmin.templates.findTemplate(
            templateId = updatedTemplate.id!!
        )

        assertNotNull(foundTemplate)
        assertNotNull(foundTemplate.id)
        assertNotNull(foundTemplate.visibility)
        assertEquals(updatedTemplate.visibility, foundTemplate.visibility)
    }

    @Test
    @Throws(Exception::class)
    fun testCreateTemplateByRole() = TestBuilder().use { testBuilder ->

        val metaform1 = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
        assertNotNull(metaform1)
        assertNotNull(metaform1.id)

        val template1 = createTemplateFromMetaform(metaform1, TemplateVisibility.PRIVATE)
        assertNotNull(template1)

        val managerAuthentication = testBuilder.createMetaformManagerAuthentication(metaform1.id!!, false)

        //with anon, user and manager roles should fail trying to create template
        testBuilder.assertApiCallFailStatus(403) { testBuilder.anon.templates.createTemplate(template1) }
        testBuilder.assertApiCallFailStatus(403) { testBuilder.test1.templates.createTemplate(template1) }
        testBuilder.assertApiCallFailStatus(403) { managerAuthentication.templates.createTemplate(template1) }

        //with admin roles should succeed create template
        testBuilder.assertApiCallFailStatus(200) { testBuilder.systemAdmin.templates.createTemplate(template1) }
    }

    @Test
    @Throws(Exception::class)
    fun testListTemplateByRole() = TestBuilder().use { testBuilder ->

        val metaform1 = testBuilder.systemAdmin.metaforms.createFromJsonFile("simple")
        assertNotNull(metaform1)
        assertNotNull(metaform1.id)

        val template1 = createTemplateFromMetaform(metaform = metaform1, templateVisibility = TemplateVisibility.PUBLIC)
        assertNotNull(template1)

        val createdTemplate = testBuilder.systemAdmin.templates.createTemplate(template = template1)

        val managerAuthentication = testBuilder.createMetaformManagerAuthentication(metaform1.id!!, false)

        //with anon, user and manager roles should fail trying to list templates
        testBuilder.assertApiCallFailStatus(403) {
            testBuilder.anon.templates.findTemplate(templateId = createdTemplate.id!!)
        }
        testBuilder.assertApiCallFailStatus(403) {
            testBuilder.test1.templates.findTemplate(templateId = createdTemplate.id!!)
        }
        testBuilder.assertApiCallFailStatus(403) {
            managerAuthentication.templates.findTemplate(templateId = createdTemplate.id!!)
        }

        //with admin roles should succeed list templates
        testBuilder.assertApiCallFailStatus(200) {
            testBuilder.systemAdmin.templates.findTemplate(templateId = createdTemplate.id!!)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testDeleteTemplateByRole() = TestBuilder().use { testBuilder ->

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

        val foundTemplate = testBuilder.systemAdmin.templates.findTemplate(templateId = createdTemplate.id!!)

        assertNotNull(foundTemplate)
        assertNotNull(foundTemplate.id)

        val managerAuthentication = testBuilder.createMetaformManagerAuthentication(createdMetaform.id!!, false)

        //with anon, user and manager roles should fail trying to delete template
        testBuilder.assertApiCallFailStatus(403) { testBuilder.anon.templates.delete(foundTemplate.id!!) }
        testBuilder.assertApiCallFailStatus(403) { testBuilder.test1.templates.delete(foundTemplate.id!!) }
        testBuilder.assertApiCallFailStatus(403) { managerAuthentication.templates.delete(foundTemplate.id!!) }

        //with admin roles should succeed delete template
        testBuilder.assertApiCallFailStatus(200) { testBuilder.systemAdmin.templates.delete(foundTemplate.id!!) }
    }

    @Test
    @Throws(Exception::class)
    fun testUpdateTemplateByRole() = TestBuilder().use { testBuilder ->

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

        val managerAuthentication = testBuilder.createMetaformManagerAuthentication(createdMetaform.id!!, false)

        //with anon, user and manager roles should fail trying to update template
        testBuilder.assertApiCallFailStatus(403) {
            testBuilder.anon.templates.updateTemplate(
                id = createdTemplate.id!!,
                template = changedTemplate
            )
        }
        testBuilder.assertApiCallFailStatus(403) {
            testBuilder.test1.templates.updateTemplate(
                id = createdTemplate.id!!,
                template = changedTemplate
            )
        }
        testBuilder.assertApiCallFailStatus(403) {
            managerAuthentication.templates.updateTemplate(
                id = createdTemplate.id!!,
                template = changedTemplate
            )
        }

        //with admin roles should succeed update template
        testBuilder.assertApiCallFailStatus(200) {
            testBuilder.systemAdmin.templates.updateTemplate(
                id = createdTemplate.id!!,
                template = changedTemplate
            )
        }
    }

    /**
     * Creates a template from a metaform
     *
     * @param metaform the metaform to create the template from
     * @param templateVisibility the visibility of the template
     * @return the created template
     */
    private fun createTemplateFromMetaform(
            metaform: Metaform,
            templateVisibility: TemplateVisibility?
    ): Template {

        val testTemplateData = TemplateData(
            title = metaform.title,
            exportThemeId = metaform.exportThemeId,
            sections = metaform.sections
        )

        return Template(
            data = testTemplateData,
            visibility = templateVisibility!!,
            createdAt = metaform.createdAt,
            modifiedAt = metaform.modifiedAt,
            creatorId = metaform.creatorId,
            lastModifierId = metaform.lastModifierId
        )
    }
}