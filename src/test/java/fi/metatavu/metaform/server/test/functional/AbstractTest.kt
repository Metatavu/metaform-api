package fi.metatavu.metaform.server.test.functional

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import fi.metatavu.metaform.api.client.models.Metaform
import fi.metatavu.metaform.api.client.models.Reply
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.lang3.StringUtils
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.eclipse.microprofile.config.ConfigProvider
import org.hamcrest.collection.IsIterableContainingInAnyOrder
import org.junit.Assert
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.SQLException
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@QuarkusTest
class AbstractTest {

    /**
     * Flushes JPA cache
     */
    protected fun flushCache() {
        RestAssured.given()
                .baseUri(ApiTestSettings.apiBasePath)["/system/jpa/cache/flush"]
                .then()
    }

    /**
     * Executes an update statement into test database
     *
     * @param sql    sql
     * @param params params
     */
    protected fun executeUpdate(sql: String, vararg params: Any) {
        executeInsert(sql, *params)
    }

    /**
     * Executes an insert statement into test database
     *
     * @param sql    sql
     * @param params params
     */
    protected fun executeInsert(sql: String, vararg params: Any) {
        try {
            connection.use { connection ->
                connection!!.autoCommit = true
                connection.prepareStatement(sql).use { statement ->
                    applyStatementParams(statement, *params)
                    statement.execute()
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to execute insert", e)
            Assert.fail(e.message)
        }
    }

    /**
     * Returns test database connection
     *
     * @return test database connection
     */
    private val connection: Connection?
        get() {
            val username = ConfigProvider.getConfig().getValue("quarkus.datasource.username", String::class.java)
            val password = ConfigProvider.getConfig().getValue("quarkus.datasource.password", String::class.java)
            val url = ConfigProvider.getConfig().getValue("quarkus.datasource.jdbc.url", String::class.java)
            try {
                val driver = ConfigProvider.getConfig().getValue("jdbc.driver", String::class.java)
                Class.forName(driver).newInstance()
            } catch (e: InstantiationException) {
                logger.error("Failed to load JDBC driver", e)
                Assert.fail(e.message)
            } catch (e: IllegalAccessException) {
                logger.error("Failed to load JDBC driver", e)
                Assert.fail(e.message)
            } catch (e: ClassNotFoundException) {
                logger.error("Failed to load JDBC driver", e)
                Assert.fail(e.message)
            }
            try {
                return DriverManager.getConnection(url, username, password)
            } catch (e: SQLException) {
                logger.error("Failed to get connection", e)
                Assert.fail(e.message)
            }
            return null
        }

    /**
     * Uploads resource into file store
     *
     * @param resourceName resource name
     * @return upload response
     * @throws IOException thrown on upload failure
     */
    @Throws(IOException::class)
    protected fun uploadResourceFile(resourceName: String?): FileUploadResponse {
        val classLoader = javaClass.classLoader
        classLoader.getResourceAsStream(resourceName).use { fileStream ->
            val clientBuilder = HttpClientBuilder.create()
            clientBuilder.build().use { client ->
                val post = HttpPost(String.format("%s/fileUpload", ApiTestSettings.apiBasePath))
                val multipartEntityBuilder = MultipartEntityBuilder.create()
                multipartEntityBuilder.addBinaryBody("file", fileStream, ContentType.create("image/jpg"), resourceName)
                post.entity = multipartEntityBuilder.build()
                val response: HttpResponse = client.execute(post)
                Assert.assertEquals(200, response.statusLine.statusCode.toLong())
                val httpEntity = response.entity
                val objectMapper = ObjectMapper()
                val result = objectMapper.readValue(httpEntity.content, FileUploadResponse::class.java)
                Assert.assertNotNull(result)
                Assert.assertNotNull(result.fileRef)
                return result
            }
        }
    }

    /**
     * Asserts that given file upload exists
     *
     * @param fileRef fileRef
     * @throws IOException throw then request fails
     */
    @Throws(IOException::class)
    protected fun assertUploadFound(fileRef: String) {
        assertUploadStatus(fileRef, 200)
    }

    /**
     * Asserts that given file upload does not exist
     *
     * @param fileRef        fileRef
     * @param expectedStatus expected status code
     * @throws IOException throw then request fails
     */
    @Throws(IOException::class)
    private fun assertUploadStatus(fileRef: String, expectedStatus: Int) {
        val clientBuilder = HttpClientBuilder.create()
        clientBuilder.build().use { client ->
            val get = HttpGet(String.format("%s/fileUpload?fileRef=%s", ApiTestSettings.apiBasePath, fileRef))
            val response: HttpResponse = client.execute(get)
            Assert.assertEquals(expectedStatus.toLong(), response.statusLine.statusCode.toLong())
        }
    }

    /**
     * Returns file meta for a uploaded file
     *
     * @param fileRef file ref
     * @return meta
     * @throws IOException thrown on io exception
     */
    @Throws(IOException::class)
    protected fun getFileRefMeta(fileRef: UUID?): FileUploadMeta {
        val clientBuilder = HttpClientBuilder.create()
        clientBuilder.build().use { client ->
            val get = HttpGet(String.format("%s/fileUpload?fileRef=%s&meta=true", ApiTestSettings.apiBasePath, fileRef))
            val response: HttpResponse = client.execute(get)
            response.entity.content.use { contentStream -> return objectMapper.readValue(contentStream, FileUploadMeta::class.java) }
        }
    }

    /**
     * Returns object mapper with default modules and settings
     *
     * @return object mapper
     */
    protected val objectMapper: ObjectMapper
        get() {
            val objectMapper = ObjectMapper()
            objectMapper.registerModule(JavaTimeModule())
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            return objectMapper
        }

    /**
     * Asserts that given file upload does not exist
     *
     * @param fileRef fileRef
     * @throws IOException throw then request fails
     */
    @Throws(IOException::class)
    protected fun assertUploadNotFound(fileRef: String) {
        assertUploadStatus(fileRef, 404)
    }

    /**
     * Delete uploaded file from the store
     *
     * @param fileRef fileRef
     * @throws IOException thrown on delete failure
     */
    @Throws(IOException::class)
    protected fun deleteUpload(fileRef: String?) {
        val clientBuilder = HttpClientBuilder.create()
        clientBuilder.build().use { client ->
            val delete = HttpDelete(String.format("%s/fileUpload?fileRef=%s", ApiTestSettings.apiBasePath, fileRef))
            val response: HttpResponse = client.execute(delete)
            Assert.assertEquals(204, response.statusLine.statusCode.toLong())
        }
    }

    /**
     * Returns offset date time
     *
     * @param year       year
     * @param month      month
     * @param dayOfMonth day
     * @param zone       zone
     * @return offset date time
     */
    protected fun getOffsetDateTime(year: Int, month: Int, dayOfMonth: Int, zone: ZoneId?): OffsetDateTime {
        return getZonedDateTime(year, month, dayOfMonth, 0, 0, 0, zone).toOffsetDateTime()
    }

    /**
     * Parses offset date time from string
     *
     * @param string string
     * @return parsed offset date time
     */
    protected fun parseOffsetDateTime(string: String?): OffsetDateTime {
        return OffsetDateTime.parse(string)
    }

    /**
     * Returns ISO formatted date string
     *
     * @param year       year
     * @param month      month
     * @param dayOfMonth day
     * @param zone       zone
     * @return ISO formatted date string
     */
    protected fun getIsoDateTime(year: Int, month: Int, dayOfMonth: Int, zone: ZoneId?): String {
        return DateTimeFormatter.ISO_DATE_TIME.format(getOffsetDateTime(year, month, dayOfMonth, zone))
    }

    /**
     * Returns zoned date time
     *
     * @param year       year
     * @param month      month
     * @param dayOfMonth day
     * @param hour       hour
     * @param minute     minute
     * @param second     second
     * @param zone       zone
     * @return zoned date time
     */
    protected fun getZonedDateTime(year: Int, month: Int, dayOfMonth: Int, hour: Int, minute: Int, second: Int, zone: ZoneId?): ZonedDateTime {
        return ZonedDateTime.of(year, month, dayOfMonth, hour, minute, second, 0, zone)
    }

    /**
     * Applies params into sql statement
     *
     * @param statement statement
     * @param params    params
     * @throws SQLException
     */
    @Throws(SQLException::class)
    private fun applyStatementParams(statement: PreparedStatement, vararg params: Any) {
        var i = 0
        val l = params.size
        while (i < l) {
            val param = params[i]
            if (param is List<*>) {
                statement.setObject(i + 1, param.toTypedArray())
            } else if (param is UUID) {
                statement.setBytes(i + 1, getUUIDBytes(param))
            } else {
                statement.setObject(i + 1, params[i])
            }
            i++
        }
    }

    /**
     * Converts UUID into bytes
     *
     * @param uuid UUID
     * @return bytes
     */
    private fun getUUIDBytes(uuid: UUID): ByteArray {
        val result = ByteArray(16)
        ByteBuffer.wrap(result).order(ByteOrder.BIG_ENDIAN).putLong(uuid.mostSignificantBits).putLong(uuid.leastSignificantBits)
        return result
    }

    /**
     * Assert PDF download status code
     *
     * @param expected    expected status code
     * @param accessToken access token
     * @param metaform    metaform
     * @param reply       reply
     */
    protected fun assertPdfDownloadStatus(expected: Int, accessToken: String?, metaform: Metaform, reply: Reply) {
        val response = RestAssured.given()
                .baseUri(ApiTestSettings.apiBasePath)
                .header("Content-Type", "application/json")
                .header("Authorization", String.format("Bearer %s", accessToken))["/v1/metaforms/{metaformId}/replies/{replyId}/export?format=PDF", metaform.id.toString(), reply.id.toString()]
                .then()
                .assertThat()
                .statusCode(expected)

        if (expected == 200) {
            response.header("Content-Type", "application/pdf")
        }
    }

    /**
     * Assert PDF download status code
     *
     * @param expected    expected status code
     * @param accessToken access token
     * @param metaform    metaform
     * @param reply       reply
     */
    protected fun assertPdfDownloadContents(expected: String, accessToken: String?, metaform: Metaform, reply: Reply) {
        val response = RestAssured.given()
            .baseUri(ApiTestSettings.apiBasePath)
            .header("Content-Type", "application/json")
            .header("Authorization", String.format("Bearer %s", accessToken))["/v1/metaforms/{metaformId}/replies/{replyId}/export?format=PDF", metaform.id.toString(), reply.id.toString()]

        val data = response.body.asByteArray()
        Assert.assertNotNull(data)
        response.then().assertThat().statusCode(200)

        assertPdfContains(
            expected = expected,
            data = data
        )
    }


    /**
     * Asserts that given PDF data contains expected string
     *
     * @param expected expected string
     * @param data     PDF data
     * @throws IOException thrown on PDF read failure
     */
    @Throws(IOException::class)
    protected fun assertPdfContains(expected: String?, data: ByteArray?) {
        val document = PDDocument.load(ByteArrayInputStream(data))
        val pdfText = PDFTextStripper().getText(document)
        document.close()
        Assert.assertTrue(String.format("PDF text (%s) does not contain expected text %s", pdfText, expected), StringUtils.contains(pdfText, expected))
    }

    /**
     * Reads JSON src into Map
     *
     * @param src input
     * @return map
     * @throws IOException throws IOException when there is error when reading the input
     */
    @Throws(IOException::class)
    protected fun readJsonMap(src: String?): Map<String, Any> {
        return objectMapper.readValue(src, object : TypeReference<Map<String, Any>>() {})
    }

    /**
     * Calculates contents md5 from a resource
     *
     * @param resourceName resource name
     * @return resource contents md5
     * @throws IOException thrown when file reading fails
     */
    @Throws(IOException::class)
    protected fun getResourceMd5(resourceName: String?): String {
        val classLoader = javaClass.classLoader
        classLoader.getResourceAsStream(resourceName).use { fileStream ->
            assert(fileStream != null)
            return DigestUtils.md5Hex(fileStream)
        }
    }

    /**
     * Asserts that given object is list and contains same items as the expected list (in any order)
     *
     * @param expected expected list
     * @param actual   actual object
     */
    protected fun assertListsEqualInAnyOrder(expected: List<*>, actual: Any?) {
        Assert.assertTrue(actual is List<*>)
        Assert.assertThat(actual as List<*>?, IsIterableContainingInAnyOrder.containsInAnyOrder(*expected.toTypedArray()))
    }

    /**
     * Creates test table row data
     *
     * @param tableText   text
     * @param tableNumber number
     * @return created test data row
     */
    protected fun createSimpleTableRow(tableText: String?, tableNumber: Double?): Map<String, Any> {
        val result: MutableMap<String, Any> = HashMap()
        if (tableText != null) {
            result["tabletext"] = tableText
        }
        if (tableNumber != null) {
            result["tablenumber"] = tableNumber
        }
        return result
    }

    /**
     * Starts a mailgun mocker
     *
     * @return mailgun mocker
     */
    protected fun startMailgunMocker(): MailgunMocker {
        val domain = "domain.example.com"
        val path = "mgapi"
        val apiKey = "fakekey"
        val mailgunMocker = MailgunMocker(String.format("/%s", path), domain, apiKey)
        mailgunMocker.startMock()
        return mailgunMocker
    }

    /**
     * Stops a mailgun mocker
     *
     * @param mailgunMocker mocker
     */
    protected fun stopMailgunMocker(mailgunMocker: MailgunMocker) {
        mailgunMocker.stopMock()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AbstractTest::class.java.name)
        val USER_1_ID = UUID.fromString("b6039e55-3758-4252-9858-a973b0988b63")
        val USER_2_ID = UUID.fromString("5ec6c56a-f618-4038-ab62-098b0db50cd5")
        val USER_3_ID = UUID.fromString("3c497a19-a235-465a-9d83-8efdc851c273")
        val SYSTEM_ADMIN_ID = UUID.fromString("83543c6b-1324-42c6-a4ca-c164a3ade516")

        val USER_WITHOUT_IDP_ID = UUID.fromString("f88927f4-4b79-4f81-b507-d4adf8ac1f69")
        val USER_WITH_IDP_ID = UUID.fromString("ea61792a-b546-44bf-b64b-5e745e404bd7")


    }
}