package fi.metatavu.metaform.server.test.functional.tests

import fi.metatavu.metaform.api.client.models.Metaform
import fi.metatavu.metaform.server.test.functional.AbstractTest
import fi.metatavu.metaform.server.test.functional.ApiTestSettings
import fi.metatavu.metaform.server.test.functional.builder.resources.MetaformKeycloakResource
import fi.metatavu.metaform.server.test.functional.builder.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import io.restassured.RestAssured
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.IOException

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
//    @Test
//    @Throws(Exception::class)
//    fun testExportXlsxTable() {
//        TestBuilder().use { builder ->
//            val parsedMetaform: Metaform = builder.metaformAdmin.metaforms().readMetaform("simple-table")
//            val metaform: Metaform = builder.metaformAdmin.metaforms().create(parsedMetaform)
//            val tableData: List<Map<String, Any>> = Arrays.asList(createSimpleTableRow("Text 1", 10.0), createSimpleTableRow("Text 2", 20.0))
//            val replyData: Map<String, Any> = HashMap()
//            replyData.put("table", tableData)
//            val replyWithData: Reply = builder.test1.replies().createReplyWithData(replyData)
//            builder.test1.replies().create(metaform.id, null, ReplyMode.REVISION.toString(), replyWithData)
//            val workbook = getXlsxReport(metaform, builder.metaformAdmin.token())
//            val simpleSheet: Sheet = workbook.getSheet("Simple")
//            assertNotNull(simpleSheet)
//            assertEquals("Table field", simpleSheet.getRow(0).getCell(0).getStringCellValue())
//            assertEquals("Table field - 1", simpleSheet.getRow(1).getCell(0).getStringCellValue())
//            assertEquals("Table field - 1", simpleSheet.getRow(1).getCell(0).getHyperlink().getAddress())
//            val tableSheet: Sheet = workbook.getSheet("Table Field - 1")
//            assertNotNull(tableSheet)
//            assertEquals("Text table field", tableSheet.getRow(0).getCell(0).getStringCellValue())
//            assertEquals("Text 1", tableSheet.getRow(1).getCell(0).getStringCellValue())
//            assertEquals("Text 2", tableSheet.getRow(2).getCell(0).getStringCellValue())
//            assertEquals(10.0, tableSheet.getRow(1).getCell(1).getNumericCellValue(), 0)
//            assertEquals(20.0, tableSheet.getRow(2).getCell(1).getNumericCellValue(), 0)
//            assertEquals(30.0, tableSheet.getRow(3).getCell(1).getNumericCellValue(), 0)
//            assertEquals("SUM(B2:B3)", tableSheet.getRow(3).getCell(1).getCellFormula())
//        }
//    }
//
//    @Test
//    @Throws(Exception::class)
//    fun testExportXlsxTableScripted() {
//        TestBuilder().use { builder ->
//            val parsedMetaform: Metaform = builder.metaformAdmin.metaforms().readMetaform("scripted-xlsx-table")
//            val metaform: Metaform = builder.metaformAdmin.metaforms().create(parsedMetaform)
//            val tableData: List<Map<String, Any>> = Arrays.asList(createSimpleTableRow("Text 1", 10.0), createSimpleTableRow("Text 2", 20.0))
//            val replyData: Map<String, Any> = HashMap()
//            replyData.put("table", tableData)
//            val replyWithData: Reply = builder.test1.replies().createReplyWithData(replyData)
//            builder.test1.replies().create(metaform.id, null, ReplyMode.REVISION.toString(), replyWithData)
//            val workbook = getXlsxReport(metaform, builder.metaformAdmin.token())
//            val simpleSheet: Sheet = workbook.getSheet("Simple")
//            assertNotNull(simpleSheet)
//            assertEquals("Sum field", simpleSheet.getRow(0).getCell(1).getStringCellValue())
//            assertEquals("SUM('Table field - 1'!B1:'Table field - 1'!B3)", simpleSheet.getRow(1).getCell(1).getCellFormula())
//        }
//    }

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