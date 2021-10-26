package fi.metatavu.metaform.test.functional.tests;

import fi.metatavu.metaform.api.client.models.Metaform;
import fi.metatavu.metaform.test.TestSettings;
import fi.metatavu.metaform.test.functional.AbstractIntegrationTest;
import fi.metatavu.metaform.test.functional.builder.resources.KeycloakResource;
import fi.metatavu.metaform.test.functional.builder.resources.MysqlResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;

import static io.restassured.RestAssured.given;

@QuarkusTest
@QuarkusTestResource.List(value = {
  @QuarkusTestResource(MysqlResource.class),
  @QuarkusTestResource(KeycloakResource.class)
})
@TestProfile(GeneralTestProfile.class)
public class ExportXlsxTestsIT extends AbstractIntegrationTest {
/*
  @Test
  public void testExportXlsxTable() throws Exception {
    try (TestBuilder builder = new TestBuilder()) {
      Metaform parsedMetaform = builder.metaformAdmin.metaforms().readMetaform("simple-table");
      Metaform metaform = builder.metaformAdmin.metaforms().create(parsedMetaform);

      List<Map<String, Object>> tableData = Arrays.asList(createSimpleTableRow("Text 1", 10d), createSimpleTableRow("Text 2", 20d));
      Map<String, Object> replyData = new HashMap<>();
      replyData.put("table", tableData);

      Reply replyWithData = builder.test1.replies().createReplyWithData(replyData);
      builder.test1.replies().create(metaform.getId(), null, ReplyMode.REVISION.toString(), replyWithData);

      Workbook workbook = getXlsxReport(metaform, builder.metaformAdmin.token());
      Sheet simpleSheet = workbook.getSheet("Simple");

      assertNotNull(simpleSheet);

      assertEquals("Table field", simpleSheet.getRow(0).getCell(0).getStringCellValue());
      assertEquals("Table field - 1", simpleSheet.getRow(1).getCell(0).getStringCellValue());
      assertEquals("Table field - 1", simpleSheet.getRow(1).getCell(0).getHyperlink().getAddress());

      Sheet tableSheet = workbook.getSheet("Table Field - 1");
      assertNotNull(tableSheet);

      assertEquals("Text table field", tableSheet.getRow(0).getCell(0).getStringCellValue());
      assertEquals("Text 1", tableSheet.getRow(1).getCell(0).getStringCellValue());
      assertEquals("Text 2", tableSheet.getRow(2).getCell(0).getStringCellValue());

      assertEquals(10d, tableSheet.getRow(1).getCell(1).getNumericCellValue(), 0);
      assertEquals(20d, tableSheet.getRow(2).getCell(1).getNumericCellValue(), 0);
      assertEquals(30d, tableSheet.getRow(3).getCell(1).getNumericCellValue(), 0);
      assertEquals("SUM(B2:B3)", tableSheet.getRow(3).getCell(1).getCellFormula());
    }
  }

  @Test
  public void testExportXlsxTableScripted() throws Exception {
    try (TestBuilder builder = new TestBuilder()) {
      Metaform parsedMetaform = builder.metaformAdmin.metaforms().readMetaform("scripted-xlsx-table");
      Metaform metaform = builder.metaformAdmin.metaforms().create(parsedMetaform);

      List<Map<String, Object>> tableData = Arrays.asList(createSimpleTableRow("Text 1", 10d), createSimpleTableRow("Text 2", 20d));
      Map<String, Object> replyData = new HashMap<>();
      replyData.put("table", tableData);

      Reply replyWithData = builder.test1.replies().createReplyWithData(replyData);
      builder.test1.replies().create(metaform.getId(), null, ReplyMode.REVISION.toString(), replyWithData);

      Workbook workbook = getXlsxReport(metaform, builder.metaformAdmin.token());


      Sheet simpleSheet = workbook.getSheet("Simple");
      assertNotNull(simpleSheet);
      assertEquals("Sum field", simpleSheet.getRow(0).getCell(1).getStringCellValue());
      assertEquals("SUM('Table field - 1'!B1:'Table field - 1'!B3)", simpleSheet.getRow(1).getCell(1).getCellFormula());
    }
  }*/

  /**
   * Downloads XLSX report and returns it as POI Workbook
   *
   * @param metaform metaform
   * @return XLSX report as POI Workbook
   * @throws IOException thrown when reading fails
   */
  private Workbook getXlsxReport(Metaform metaform, String token) throws IOException {
    return WorkbookFactory.create(given()
      .baseUri(TestSettings.basePath)
      .header("Authorization", String.format("Bearer %s", token))
      .get("/v1/metaforms/{metaformId}/export?format=XLSX", metaform.getId().toString())
      .asInputStream());
  }

}
