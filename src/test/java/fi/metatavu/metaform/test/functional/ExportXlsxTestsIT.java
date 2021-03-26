package fi.metatavu.metaform.test.functional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

@SuppressWarnings ("squid:S1192")
public class ExportXlsxTestsIT {
 /**
  @Test
  public void testExportXlsxTable() throws Exception {
    String adminToken = getAdminToken(REALM_1);
    String accessToken = getAccessToken(REALM_1, "test1.realm1", "test");
    MetaformsApi adminMetaformsApi = getMetaformsApi(adminToken);
    RepliesApi repliesApi = getRepliesApi(accessToken);
    
    Metaform metaform = adminMetaformsApi.createMetaform(readMetaform("simple-table"));
    try {
      List<Map<String, Object>> tableData = Arrays.asList(createSimpleTableRow("Text 1", 10d), createSimpleTableRow("Text 2", 20d));
      Map<String, Object> replyData = new HashMap<>();
      replyData.put("table", tableData);
      
      repliesApi.createReply(metaform.getId(), createReplyWithData(replyData), null, ReplyMode.REVISION.toString());
      
      try (Workbook workbook = getXlsxReport(metaform)) {
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
    } finally {
      adminMetaformsApi.deleteMetaform(metaform.getId());
    }
  }

  @Test
  public void testExportXlsxTableScripted() throws Exception {
    String adminToken = getAdminToken(REALM_1);
    String accessToken = getAccessToken(REALM_1, "test1.realm1", "test");
    MetaformsApi adminMetaformsApi = getMetaformsApi(adminToken);
    RepliesApi repliesApi = getRepliesApi(accessToken);
    
    Metaform metaform = adminMetaformsApi.createMetaform(readMetaform("scripted-xlsx-table"));
    try {
      List<Map<String, Object>> tableData = Arrays.asList(createSimpleTableRow("Text 1", 10d), createSimpleTableRow("Text 2", 20d));
      Map<String, Object> replyData = new HashMap<>();
      replyData.put("table", tableData);
      
      repliesApi.createReply(metaform.getId(), createReplyWithData(replyData), null, ReplyMode.REVISION.toString());
      
      try (Workbook workbook = getXlsxReport(metaform)) {
        Sheet simpleSheet = workbook.getSheet("Simple");
        assertNotNull(simpleSheet);
        assertEquals("Sum field", simpleSheet.getRow(0).getCell(1).getStringCellValue());
        assertEquals("SUM('Table field - 1'!B1:'Table field - 1'!B3)", simpleSheet.getRow(1).getCell(1).getCellFormula());        
      }
    } finally {
      adminMetaformsApi.deleteMetaform(metaform.getId());
    }
  }
 */
  /**
   * Downloads XLSX report and returns it as POI Workbook
   * 
   * @param metaform metaform
   * @return XLSX report as POI Workbook
   * @throws IOException thrown when reading fails

  private Workbook getXlsxReport(Metaform metaform) throws IOException {
    return WorkbookFactory.create(given()
      .baseUri(getBasePath())
      .header("Authorization", String.format("Bearer %s", getAdminToken(REALM_1)))
      .get("/v1/metaforms/{metaformId}/export?format=XLSX", metaform.getId().toString())
      .asInputStream());
  }
*/
}
