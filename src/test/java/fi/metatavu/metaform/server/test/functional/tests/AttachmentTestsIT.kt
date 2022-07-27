package fi.metatavu.metaform.server.test.functional.tests

import fi.metatavu.metaform.api.client.models.Metaform
import fi.metatavu.metaform.api.client.models.Reply
import fi.metatavu.metaform.server.rest.ReplyMode
import fi.metatavu.metaform.server.test.functional.AbstractTest
import fi.metatavu.metaform.server.test.functional.ApiTestSettings.Companion.apiBasePath
import fi.metatavu.metaform.server.test.functional.FileUploadResponse
import fi.metatavu.metaform.server.test.functional.builder.PermissionScope
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import fi.metatavu.metaform.server.test.functional.builder.auth.TestBuilderAuthentication
import fi.metatavu.metaform.server.test.functional.builder.resources.KeycloakResource
import fi.metatavu.metaform.server.test.functional.builder.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

/**
 * Tests for attachments
 */
@QuarkusTest
@QuarkusTestResource.List(
        QuarkusTestResource(MysqlResource::class),
        QuarkusTestResource(KeycloakResource::class)
)
@TestProfile(GeneralTestProfile::class)
class AttachmentTestsIT : AbstractTest() {
    @Test
    @Throws(Exception::class)
    fun findAttachment() {
        TestBuilder().use { builder ->
            val metaform: Metaform = builder.systemAdmin.metaforms.createFromJsonFile("files")
            val fileUpload: FileUploadResponse = uploadResourceFile("test-image-480-320.jpg")
            val replyData: MutableMap<String, Any> = HashMap()
            replyData["files"] = fileUpload.fileRef
            val replyWithData: Reply = builder.test1.replies.createReplyWithData(replyData)
            val reply: Reply = builder.test1.replies.create(metaform.id!!, ReplyMode.REVISION.toString(), replyWithData)
            Assertions.assertNotNull(reply)
            Assertions.assertNotNull(reply.data)
            Assertions.assertEquals(listOf(fileUpload.fileRef.toString()), reply.data!!["files"])
            val foundReply: Reply = builder.test1.replies.findReply(metaform.id, reply.id!!, null)
            Assertions.assertNotNull(foundReply)
            Assertions.assertNotNull(foundReply.data)
            Assertions.assertEquals(listOf(fileUpload.fileRef.toString()), foundReply.data!!["files"])
            builder.systemAdmin.attachments.assertAttachmentExists(fileUpload)
            Assertions.assertEquals(
                getResourceMd5("test-image-480-320.jpg"),
                DigestUtils.md5Hex(getAttachmentData(builder.systemAdmin.token, fileUpload.fileRef))
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun findMultipleAttachments() {
        TestBuilder().use { builder ->
            val metaform: Metaform = builder.systemAdmin.metaforms.createFromJsonFile("files")
            val fileUpload1: FileUploadResponse = uploadResourceFile("test-image-480-320.jpg")
            val fileUpload2: FileUploadResponse = uploadResourceFile("test-image-667-1000.jpg")
            val fileRef1: String = fileUpload1.fileRef.toString()
            val fileRef2: String = fileUpload2.fileRef.toString()
            val fileRefs = listOf(fileRef1, fileRef2)
            val replyData: MutableMap<String, Any> = HashMap()
            replyData["files"] = fileRefs
            val replyWithData: Reply = builder.test1.replies.createReplyWithData(replyData)
            val reply1 = builder.test1.replies.create(metaform.id!!, ReplyMode.REVISION.toString(), replyWithData)
            assertListsEqualInAnyOrder(fileRefs, reply1.data!!["files"])
            builder.systemAdmin.attachments.assertAttachmentExists(fileUpload1)
            builder.systemAdmin.attachments.assertAttachmentExists(fileUpload2)
            Assertions.assertEquals(getResourceMd5("test-image-480-320.jpg"), DigestUtils.md5Hex(getAttachmentData(builder.systemAdmin.token, fileUpload1.fileRef)))
            Assertions.assertEquals(getResourceMd5("test-image-667-1000.jpg"), DigestUtils.md5Hex(getAttachmentData(builder.systemAdmin.token, fileUpload2.fileRef)))
        }
    }

    @Test
    @Throws(Exception::class)
    fun updateAttachments() {
        TestBuilder().use { builder ->
            val metaform: Metaform = builder.systemAdmin.metaforms.createFromJsonFile("files")
            val fileUpload1: FileUploadResponse = uploadResourceFile("test-image-480-320.jpg")
            val fileUpload2: FileUploadResponse = uploadResourceFile("test-image-667-1000.jpg")
            val fileRef1: String = fileUpload1.fileRef.toString()
            val fileRef2: String = fileUpload2.fileRef.toString()
            val fileRefs = listOf(fileRef1, fileRef2)
            val replyData: MutableMap<String, Any> = HashMap()
            replyData["files"] = fileRefs
            val replyWithData: Reply = builder.test1.replies.createReplyWithData(replyData)
            val reply1 = builder.test1.replies.create(metaform.id!!, ReplyMode.REVISION.toString(), replyWithData)
            assertListsEqualInAnyOrder(fileRefs, reply1.data!!["files"])
            val foundReply1 = builder.test1.replies.findReply(metaform.id, reply1.id!!, null)
            assertListsEqualInAnyOrder(fileRefs, foundReply1.data!!["files"])
            builder.systemAdmin.attachments.assertAttachmentExists(fileUpload1)
            builder.systemAdmin.attachments.assertAttachmentExists(fileUpload2)
            val updateData: MutableMap<String, Any> = HashMap()
            updateData["files"] = listOf(fileRef2)
            val newReplyWithData: Reply = builder.test1.replies.createReplyWithData(updateData)
            builder.test1.replies.updateReply(metaform.id, reply1.id, newReplyWithData, null)
            val foundReply2 = builder.test1.replies.findReply(metaform.id, reply1.id, null)
            Assertions.assertEquals(listOf(fileRef2), foundReply2.data!!["files"])
            builder.systemAdmin.attachments.assertAttachmentNotFound(fileUpload1.fileRef)
            builder.systemAdmin.attachments.assertAttachmentExists(fileUpload2)
        }
    }

    @Test
    @Throws(Exception::class)
    fun deleteAttachments() {
        TestBuilder().use { builder ->
            val metaform: Metaform = builder.systemAdmin.metaforms.createFromJsonFile("files")
            val fileUpload1: FileUploadResponse = uploadResourceFile("test-image-480-320.jpg")
            val fileUpload2: FileUploadResponse = uploadResourceFile("test-image-667-1000.jpg")
            val fileRef1: String = fileUpload1.fileRef.toString()
            val fileRef2: String = fileUpload2.fileRef.toString()
            val fileRefs = listOf(fileRef1, fileRef2)
            val replyData: MutableMap<String, Any> = HashMap()
            replyData["files"] = fileRefs
            val replyWithData: Reply = builder.test1.replies.createReplyWithData(replyData)
            val reply: Reply = builder.test1.replies.create(metaform.id!!, ReplyMode.REVISION.toString(), replyWithData)
            assertListsEqualInAnyOrder(fileRefs, reply.data!!["files"])
            val foundReply = builder.test1.replies.findReply(metaform.id, reply.id!!, null)
            assertListsEqualInAnyOrder(fileRefs, foundReply.data!!["files"])
            builder.systemAdmin.attachments.assertAttachmentExists(fileUpload1)
            builder.systemAdmin.attachments.assertAttachmentExists(fileUpload2)
            builder.systemAdmin.replies.delete(metaform.id, reply.id, null)
            builder.systemAdmin.attachments.assertAttachmentNotFound(fileUpload1.fileRef)
            builder.systemAdmin.attachments.assertAttachmentNotFound(fileUpload2.fileRef)
        }
    }

    @Throws(IOException::class)
    private fun getAttachmentData(accessToken: String, id: UUID): ByteArray {
        val url = URL(String.format("%sv1/attachments/%s/data", apiBasePath, id.toString()))
        val connection = url.openConnection() as HttpURLConnection
        connection.setRequestProperty("Authorization", String.format("Bearer %s", accessToken))
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true
        return IOUtils.toByteArray(connection.inputStream)
    }

    @Test
    @Throws(Exception::class)
    fun findAttachmentPermission() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("files")

            val fileUpload: FileUploadResponse = uploadResourceFile("test-image-480-320.jpg")
            val replyData: MutableMap<String, Any> = HashMap()
            replyData["files"] = fileUpload.fileRef
            val replyWithData: Reply = testBuilder.test1.replies.createReplyWithData(replyData)
            testBuilder.test1.replies.create(metaform.id!!, ReplyMode.REVISION.toString(), replyWithData)

            testBuilder.permissionTestByScopes(
                scope = PermissionScope.METAFORM_ADMIN,
                apiCaller = { authentication: TestBuilderAuthentication, _: Int ->
                    authentication.attachments.find(fileUpload.fileRef)
                }
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun findAttachmentDataPermission() {
        TestBuilder().use { testBuilder ->
            val metaform: Metaform = testBuilder.systemAdmin.metaforms.createFromJsonFile("files")

            val fileUpload: FileUploadResponse = uploadResourceFile("test-image-480-320.jpg")
            val replyData: MutableMap<String, Any> = HashMap()
            replyData["files"] = fileUpload.fileRef
            val replyWithData: Reply = testBuilder.test1.replies.createReplyWithData(replyData)
            testBuilder.test1.replies.create(metaform.id!!, ReplyMode.REVISION.toString(), replyWithData)

            testBuilder.permissionTestByScopes(
                scope = PermissionScope.METAFORM_ADMIN,
                apiCaller = { authentication: TestBuilderAuthentication, _: Int ->
                    authentication.attachments.findData(fileUpload.fileRef)
                }
            )
        }
    }
}