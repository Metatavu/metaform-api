package fi.metatavu.metaform.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import feign.FeignException;
import fi.metatavu.metaform.client.Metaform;
import fi.metatavu.metaform.client.MetaformsApi;
import fi.metatavu.metaform.client.RepliesApi;
import fi.metatavu.metaform.client.Reply;
import fi.metatavu.metaform.client.ReplyData;
import fi.metatavu.metaform.server.rest.ReplyMode;

@SuppressWarnings ("squid:S1192")
public class TableFieldTestsIT extends AbstractIntegrationTest {
  
  @Test
  public void createTableReply() throws IOException, URISyntaxException {
    String adminToken = getAdminToken(REALM_1);
    String accessToken = getAccessToken(REALM_1, "test1.realm1", "test");
    MetaformsApi adminMetaformsApi = getMetaformsApi(adminToken);
    RepliesApi repliesApi = getRepliesApi(accessToken);
    RepliesApi adminRepliesApi = getRepliesApi(adminToken);
    
    Metaform metaform = adminMetaformsApi.createMetaform(REALM_1, readMetaform("simple-table"));
    try {
      List<Map<String, Object>> tableData = Arrays.asList(createSimpleTableRow("Text 1", 10d), createSimpleTableRow("Text 2", 20d));
      ReplyData replyData = new ReplyData();
      replyData.put("table", tableData);
      
      Reply reply = createReplyWithData(replyData);
      
      Reply createdReply = repliesApi.createReply(REALM_1, metaform.getId(), reply, null, ReplyMode.REVISION.toString());
      try {
        Reply foundReply = repliesApi.findReply(REALM_1, metaform.getId(), createdReply.getId());
        assertNotNull(foundReply);
        assertNotNull(foundReply.getId());
        assertTableDataEquals(replyData, foundReply.getData());
      } finally {
        adminRepliesApi.deleteReply(REALM_1, metaform.getId(), createdReply.getId());
      }
    } finally {
      adminMetaformsApi.deleteMetaform(REALM_1, metaform.getId());
    }
  }

  @Test
  public void updateTableReply() throws IOException, URISyntaxException {
    String adminToken = getAdminToken(REALM_1);
    String accessToken = getAccessToken(REALM_1, "test1.realm1", "test");
    MetaformsApi adminMetaformsApi = getMetaformsApi(adminToken);
    RepliesApi repliesApi = getRepliesApi(accessToken);
    RepliesApi adminRepliesApi = getRepliesApi(adminToken);
    
    Metaform metaform = adminMetaformsApi.createMetaform(REALM_1, readMetaform("simple-table"));
    try {
      ReplyData createReplyData = new ReplyData();
      createReplyData.put("table", Arrays.asList(createSimpleTableRow("Text 1", 10d), createSimpleTableRow("Text 2", 20d)));
      Reply createReply = createReplyWithData(createReplyData);
      
      Reply createdReply = repliesApi.createReply(REALM_1, metaform.getId(), createReply, null, ReplyMode.REVISION.toString());
      try {
        assertTableDataEquals(createReplyData, createReply.getData());

        Reply foundReply = repliesApi.findReply(REALM_1, metaform.getId(), createdReply.getId());
        assertNotNull(foundReply);
        assertNotNull(foundReply.getId());        
        
        assertTableDataEquals(createReplyData, foundReply.getData());

        ReplyData updateReplyData = new ReplyData();
        updateReplyData.put("table", Arrays.asList(createSimpleTableRow("Added new text", -210d), createSimpleTableRow("Text 1", 10d), createSimpleTableRow("Updated Text 2", 45.5d)));
        Reply updateReply = createReplyWithData(updateReplyData);
        repliesApi.updateReply(REALM_1, metaform.getId(), createdReply.getId(), updateReply);
        
        assertTableDataEquals(updateReplyData, updateReply.getData());
        
        Reply foundUpdatedReply = repliesApi.findReply(REALM_1, metaform.getId(), createdReply.getId());
        assertNotNull(foundUpdatedReply);
        assertNotNull(foundUpdatedReply.getId());        
        assertTableDataEquals(updateReplyData, foundUpdatedReply.getData());
        
      } finally {
        adminRepliesApi.deleteReply(REALM_1, metaform.getId(), createdReply.getId());
      }
    } finally {
      adminMetaformsApi.deleteMetaform(REALM_1, metaform.getId());
    }
  }
  
  @Test
  public void nulledTableReply() throws IOException, URISyntaxException {
    String adminToken = getAdminToken(REALM_1);
    String accessToken = getAccessToken(REALM_1, "test1.realm1", "test");
    MetaformsApi adminMetaformsApi = getMetaformsApi(adminToken);
    RepliesApi repliesApi = getRepliesApi(accessToken);
    RepliesApi adminRepliesApi = getRepliesApi(adminToken);
    
    Metaform metaform = adminMetaformsApi.createMetaform(REALM_1, readMetaform("simple-table"));
    try {
      List<Map<String, Object>> tableData = Arrays.asList(createSimpleTableRow(null, null));
      ReplyData replyData = new ReplyData();
      replyData.put("table", tableData);
      
      Reply reply = createReplyWithData(replyData);
      
      Reply createdReply = repliesApi.createReply(REALM_1, metaform.getId(), reply, null, ReplyMode.REVISION.toString());
      try {
        Reply foundReply = repliesApi.findReply(REALM_1, metaform.getId(), createdReply.getId());
        assertNotNull(foundReply);
        assertNotNull(foundReply.getId());
        assertTableDataEquals(replyData, foundReply.getData());
      } finally {
        adminRepliesApi.deleteReply(REALM_1, metaform.getId(), createdReply.getId());
      }
    } finally {
      adminMetaformsApi.deleteMetaform(REALM_1, metaform.getId());
    }
  }
  
  @Test
  public void nullTableReply() throws IOException, URISyntaxException {
    String adminToken = getAdminToken(REALM_1);
    String accessToken = getAccessToken(REALM_1, "test1.realm1", "test");
    MetaformsApi adminMetaformsApi = getMetaformsApi(adminToken);
    RepliesApi repliesApi = getRepliesApi(accessToken);
    RepliesApi adminRepliesApi = getRepliesApi(adminToken);
    
    Metaform metaform = adminMetaformsApi.createMetaform(REALM_1, readMetaform("simple-table"));
    try {
      ReplyData replyData = new ReplyData();
      replyData.put("table", null);
      
      Reply reply = createReplyWithData(replyData);
      
      Reply createdReply = repliesApi.createReply(REALM_1, metaform.getId(), reply, null, ReplyMode.REVISION.toString());
      try {
        Reply foundReply = repliesApi.findReply(REALM_1, metaform.getId(), createdReply.getId());
        assertNotNull(foundReply);
        assertNotNull(foundReply.getId());
        assertNull(foundReply.getData().get("table"));
      } finally {
        adminRepliesApi.deleteReply(REALM_1, metaform.getId(), createdReply.getId());
      }
    } finally {
      adminMetaformsApi.deleteMetaform(REALM_1, metaform.getId());
    }
  }
  
  @Test
  public void invalidTableReply() throws IOException, URISyntaxException {
    String adminToken = getAdminToken(REALM_1);
    String accessToken = getAccessToken(REALM_1, "test1.realm1", "test");
    MetaformsApi adminMetaformsApi = getMetaformsApi(adminToken);
    RepliesApi repliesApi = getRepliesApi(accessToken);
    
    Metaform metaform = adminMetaformsApi.createMetaform(REALM_1, readMetaform("simple-table"));
    try {
      ReplyData replyData = new ReplyData();
      replyData.put("table", "table data");
      
      try {
        Reply reply = createReplyWithData(replyData);
        repliesApi.createReply(REALM_1, metaform.getId(), reply, null, ReplyMode.REVISION.toString());
        fail("Bad request should have been returned");
      } catch (FeignException e) {
        assertEquals(401, e.status());
      }
    } finally {
      adminMetaformsApi.deleteMetaform(REALM_1, metaform.getId());
    }
  }
  
  @Test
  public void deleteTableReply() throws IOException, URISyntaxException {
    String adminToken = getAdminToken(REALM_1);
    String accessToken = getAccessToken(REALM_1, "test1.realm1", "test");
    MetaformsApi adminMetaformsApi = getMetaformsApi(adminToken);
    RepliesApi repliesApi = getRepliesApi(accessToken);
    RepliesApi adminRepliesApi = getRepliesApi(adminToken);
    
    Metaform metaform = adminMetaformsApi.createMetaform(REALM_1, readMetaform("simple-table"));
    try {
      List<Map<String, Object>> tableData = Arrays.asList(createSimpleTableRow("Text 1", 10d), createSimpleTableRow("Text 2", 20d));
      ReplyData replyData = new ReplyData();
      replyData.put("table", tableData);
      
      Reply reply = createReplyWithData(replyData);
      
      Reply createdReply = repliesApi.createReply(REALM_1, metaform.getId(), reply, null, ReplyMode.REVISION.toString());
      try {
        Reply foundReply = repliesApi.findReply(REALM_1, metaform.getId(), createdReply.getId());
        assertNotNull(foundReply);
        repliesApi.deleteReply(REALM_1, metaform.getId(), createdReply.getId());
        try {
          repliesApi.findReply(REALM_1, metaform.getId(), createdReply.getId());
          fail(String.format("Reply %s should not be present", createdReply.getId().toString()));
        } catch (FeignException e) {
          assertEquals(404, e.status());
        }
      } finally {
        adminRepliesApi.deleteReply(REALM_1, metaform.getId(), createdReply.getId());
      }
    } finally {
      adminMetaformsApi.deleteMetaform(REALM_1, metaform.getId());
    }
  }
  
  /**
   * Asserts that table datas equal
   * 
   * @param expected expected table data
   * @param actual actual table data
   */
  private void assertTableDataEquals(ReplyData expected, ReplyData actual) {
    assertNotNull(actual.get("table"));
    
    @SuppressWarnings("unchecked") List<Map<String, Object>> expectedTableData = (List<Map<String, Object>>) expected.get("table");
    @SuppressWarnings("unchecked") List<Map<String, Object>> actualTableData = (List<Map<String, Object>>) actual.get("table");
    
    assertEquals(expectedTableData.size(), actualTableData.size());
    
    for (Map<String, Object> expectedRow : expectedTableData) {
      for (Entry<String, Object> expectedCell : expectedRow.entrySet()) {
        assertEquals(expectedCell.getValue(), expectedRow.get(expectedCell.getKey()));        
      }
    }
  }
  
  /**
   * Creates test table row data
   * 
   * @param tableText text
   * @param tableNumber number
   * @return created test data row
   */
  private Map<String, Object> createSimpleTableRow(String tableText, Double tableNumber) {
    Map<String, Object> result = new HashMap<>();
    
    if (tableText != null) {
      result.put("tabletext", tableText);
    }
    
    if (tableNumber != null) {
      result.put("tablenumber", tableNumber);
    }
    
    return result;
  }

}
