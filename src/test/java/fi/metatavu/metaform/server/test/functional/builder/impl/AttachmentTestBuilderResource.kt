package fi.metatavu.metaform.server.test.functional.builder.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.metaform.api.client.apis.AttachmentsApi
import fi.metatavu.metaform.api.client.infrastructure.ApiClient
import fi.metatavu.metaform.api.client.infrastructure.ApiClient.Companion.accessToken
import fi.metatavu.metaform.api.client.infrastructure.ClientException
import fi.metatavu.metaform.api.client.models.Attachment
import fi.metatavu.metaform.server.test.functional.ApiTestSettings
import fi.metatavu.metaform.server.test.functional.FileUploadResponse
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import org.junit.Assert
import org.junit.jupiter.api.Assertions
import java.io.IOException
import java.util.*

/**
 * Test builder resource for Attachments API
 */
class AttachmentTestBuilderResource(
        testBuilder: TestBuilder,
        private val accessTokenProvider: AccessTokenProvider?,
        apiClient: ApiClient
): ApiTestBuilderResource<Attachment, ApiClient?>(testBuilder, apiClient) {

    override fun getApi(): AttachmentsApi {
        accessToken = accessTokenProvider?.accessToken
        return AttachmentsApi(ApiTestSettings.apiBasePath)
    }

    override fun clean(attachment: Attachment) {}

    /**
     * Asserts that attachment exists
     *
     * @param fileUpload1 response
     */
    @Throws(IOException::class)
    fun assertAttachmentExists(fileUpload1: FileUploadResponse) {
        val attachment1 = api.findAttachment(fileUpload1.fileRef, "")
        Assertions.assertNotNull(attachment1)
        Assertions.assertEquals(fileUpload1.fileRef, attachment1.id)
    }

    /**
     * Assert that attachment search returns 404
     *
     * @param fileRef file ref
     */
    @Throws(IOException::class)
    fun assertAttachmentNotFound(fileRef: UUID) {
        try {
            api.findAttachment(fileRef, "")
            Assert.fail(String.format("Expected find to fail with status %d", 404))
        } catch (e: ClientException) {
            Assert.assertEquals(404, e.statusCode.toLong())
        }
    }
}