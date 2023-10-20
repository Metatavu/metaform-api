package fi.metatavu.metaform.server.test.functional.builder.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.metaform.api.client.apis.TemplatesApi
import fi.metatavu.metaform.api.client.infrastructure.ApiClient
import fi.metatavu.metaform.api.client.infrastructure.ClientException
import fi.metatavu.metaform.api.client.models.*
import fi.metatavu.metaform.api.client.models.TemplateVisibility
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import fi.metatavu.metaform.server.test.functional.ApiTestSettings
import org.json.JSONException
import org.junit.Assert
import java.io.IOException
import java.util.UUID

/**
 * Test builder resource for Template API
 */
class TemplateTestBuilderResource (
        testBuilder: TestBuilder,
        private val accessTokenProvider: AccessTokenProvider?,
        apiClient: ApiClient
): ApiTestBuilderResource<Template, ApiClient?>(testBuilder, apiClient) {

    override fun getApi(): TemplatesApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return TemplatesApi(ApiTestSettings.apiBasePath)
    }
    @Throws(IOException::class)
    override fun clean(template: Template) {
        api.deleteTemplate(template.id!!)
    }

    /**
     * Creates new template
     *
     * @param template Template
     * @return created template
     */
    @Throws(IOException::class)
    fun createTemplate(template: Template): Template {
        return addClosable(api.createTemplate(template = template))
    }

    /**
     * Deletes a template from the API
     *
     * @param templateId id of template to be deleted
     */
    @Throws(IOException::class)
    fun delete(templateId: UUID) {
        api.deleteTemplate(templateId = templateId)
        removeCloseable { closable ->
            if (closable is Template) {
                return@removeCloseable templateId == closable.id
            }
            false
        }
    }

    /**
     * Finds a template by id
     *
     * @param templateId template Id
     */
    @Throws(IOException::class)
    fun findTemplate(templateId: UUID): Template {
        return api.findTemplate(templateId = templateId)
    }

    /**
     * Lists templates
     *
     * @param visibility filter by form visibility
     * @return templates
     */
    fun list(visibility: TemplateVisibility? = null): Array<Template> {
        return api.listTemplates(visibility = visibility)
    }

    @Throws(IOException::class)
    fun updateTemplate(id: UUID, template: Template): Template {
        return api.updateTemplate(templateId = id, template = template)
    }

    /**
     * Asserts create status fails with given status code
     *
     * @param expectedStatus expected status code
     * @param template       Template
     */
    @Throws(IOException::class)
    fun assertCreateFailStatus(expectedStatus: Int, template: Template) {
        try {
            api.createTemplate(template)
            Assert.fail(String.format("Expected create to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assert.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Asserts delete status fails with given status code
     *
     * @param expectedStatus expected status code
     * @param templateId template id
     */
    @Throws(IOException::class)
    fun assertDeleteFailStatus(expectedStatus: Int, templateId: UUID) {
        try {
            api.deleteTemplate(templateId = templateId)
            Assert.fail(String.format("Expected delete to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assert.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Asserts update status fails with given status code
     *
     * @param expectedStatus expected status code
     * @param templateId     templateId
     * @param template       template
     */
    @Throws(IOException::class)
    fun assertUpdateFailStatus(expectedStatus: Int, templateId: UUID, template: Template) {
        try {
            api.updateTemplate(templateId = templateId, template = template)
            Assert.fail(String.format("Expected update to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assert.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Asserts find status fails with given status code
     *
     * @param expectedStatus expected status code
     * @param templateId     template id
     */
    @Throws(IOException::class)
    fun assertFindFailStatus(expectedStatus: Int, templateId: UUID) {
        try {
            api.findTemplate(templateId = templateId)

            Assert.fail(String.format("Expected find to fail with status %d", expectedStatus))
        } catch (e: ClientException) {
            Assert.assertEquals(expectedStatus.toLong(), e.statusCode.toLong())
        }
    }

    /**
     * Asserts that actual template equals expected template when both are serialized into JSON
     *
     * @param expected expected template
     * @param actual   actual template
     * @throws JSONException thrown when JSON serialization error occurs
     * @throws IOException   thrown when IO Exception occurs
     */
    @Throws(IOException::class, JSONException::class)
    fun assertSectionEqual(expected: MetaformSection?, actual: MetaformSection?) {
        assertJsonsEqual(expected, actual)
    }


}