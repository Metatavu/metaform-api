package fi.metatavu.metaform.server.test.functional.tests

import fi.metatavu.metaform.server.test.functional.AbstractTest
import fi.metatavu.metaform.server.test.functional.FileUploadMeta
import fi.metatavu.metaform.server.test.functional.FileUploadResponse
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.IOException
import java.net.URISyntaxException

/**
 * Tests to test uploading files
 */
@QuarkusTest
@TestProfile(GeneralTestProfile::class)
class UploadTestsIT : AbstractTest() {
    @Test
    @Throws(IOException::class)
    fun findUploadedTest() {
        val fileUpload: FileUploadResponse = uploadResourceFile("test-image-480-320.jpg")
        Assertions.assertNotNull(fileUpload)
        assertUploadFound(fileUpload.fileRef.toString())
    }

    @Test
    @Throws(IOException::class)
    fun findUploadedMeta() {
        val fileUpload: FileUploadResponse = uploadResourceFile("test-image-480-320.jpg")
        Assertions.assertNotNull(fileUpload)
        val meta: FileUploadMeta = getFileRefMeta(fileUpload.fileRef)
        Assertions.assertNotNull(meta)
        Assertions.assertEquals("test-image-480-320.jpg", meta.fileName)
        Assertions.assertEquals("image/jpg", meta.contentType)
    }

    @Test
    @Throws(IOException::class, URISyntaxException::class)
    fun deleteUploadedTest() {
        val fileUpload: FileUploadResponse = uploadResourceFile("test-image-480-320.jpg")
        Assertions.assertNotNull(fileUpload)
        val fileRef: String = fileUpload.fileRef.toString()
        assertUploadFound(fileRef)
        deleteUpload(fileRef)
        assertUploadNotFound(fileRef)
    }
}