package fi.metatavu.metaform.server.test.functional.tests

import fi.metatavu.metaform.api.client.models.Metaform
import fi.metatavu.metaform.api.client.models.Reply
import fi.metatavu.metaform.server.rest.ReplyMode
import fi.metatavu.metaform.server.test.functional.AbstractTest
import fi.metatavu.metaform.server.test.functional.ApiTestSettings
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder
import fi.metatavu.metaform.server.test.functional.builder.resources.MetaformKeycloakResource
import fi.metatavu.metaform.server.test.functional.builder.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import io.restassured.RestAssured
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap

/**
 * Tests for Xlsx exports
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(MysqlResource::class),
    QuarkusTestResource(MetaformKeycloakResource::class)
)
@TestProfile(GeneralTestProfile::class)
class ExportXlsxTestsIT : AbstractTest() {
    @Test
    @Throws(Exception::class)
    fun testExportXlsxTable() {
        TestBuilder().use { builder ->
            val parsedMetaform = builder.systemAdmin.metaforms.readMetaform("simple-table")
            val metaform = builder.systemAdmin.metaforms.create(parsedMetaform!!)
            val tableData: List<Map<String, Any>> = listOf(createSimpleTableRow("Text 1", 10.0), createSimpleTableRow("Text 2", 20.0))
            val replyData: HashMap<String, Any> = HashMap()
            replyData["table"] = tableData
            val replyWithData: Reply = builder.test1.replies.createReplyWithData(replyData)
            builder.test1.replies.create(metaform.id!!, null, ReplyMode.REVISION.toString(), replyWithData)
            val workbook = getXlsxReport(metaform, builder.systemAdmin.token)
            val simpleSheet: Sheet = workbook.getSheet("Simple")
            assertNotNull(simpleSheet)
            assertEquals("Table field", simpleSheet.getRow(0).getCell(0).stringCellValue)
            assertEquals("Table field - 1", simpleSheet.getRow(1).getCell(0).stringCellValue)
            assertEquals("Table field - 1", simpleSheet.getRow(1).getCell(0).hyperlink.address)
            val tableSheet: Sheet = workbook.getSheet("Table Field - 1")
            assertNotNull(tableSheet)
            assertEquals("Text table field", tableSheet.getRow(0).getCell(0).stringCellValue)
            assertNotNull(tableSheet.find { row -> row.getCell(0).stringCellValue == "Text 1" })
            assertNotNull(tableSheet.find { row -> row.getCell(0).stringCellValue == "Text 2" })
            assertEquals("SUM(B2:B3)", tableSheet.getRow(3).getCell(1).cellFormula)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testExportXlsxWithScript() {
        TestBuilder().use { builder ->
            val parsedScript = builder.systemAdmin.scripts.readScript("testscript")
            val scriptId = builder.systemAdmin.scripts.create(parsedScript!!).id!!

            val parsedMetaform = builder.systemAdmin.metaforms.readMetaform("simple-table")
            val metaform = builder.systemAdmin.metaforms.create(parsedMetaform!!.copy(scripts = arrayOf(scriptId)))

            val tableData: List<Map<String, Any>> = listOf(createSimpleTableRow("Text 1", 10.0), createSimpleTableRow("Text 2", 20.0))
            val replyData: HashMap<String, Any> = HashMap()
            replyData["table"] = tableData
            val replyWithData: Reply = builder.test1.replies.createReplyWithData(replyData)
            builder.test1.replies.create(metaform.id!!, null, ReplyMode.REVISION.toString(), replyWithData)
            val workbook = getXlsxReport(metaform, builder.systemAdmin.token)
            val simpleSheet: Sheet = workbook.getSheet("Simple")
            assertNotNull(simpleSheet)
            assertEquals("Table field changed to something else", simpleSheet.getRow(0).getCell(0).stringCellValue)
        }
    }

    /**
     * Downloads XLSX report and returns it as POI Workbook
     *
     * @param metaform metaform
     * @return XLSX report as POI Workbook
     * @throws IOException thrown when reading fails
     */
    @Throws(IOException::class)
    private fun getXlsxReport(metaform: Metaform, token: String): Workbook {
        return WorkbookFactory.create(RestAssured.given()
            .baseUri(ApiTestSettings.apiBasePath)
            .header("Authorization", String.format("Bearer %s", token))["/v1/metaforms/{metaformId}/export?format=XLSX", metaform.id.toString()]
            .asInputStream())
    }
}