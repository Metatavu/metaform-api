package fi.metatavu.metaform.test.functional;

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
import fi.metatavu.metaform.client.model.Metaform;
import fi.metatavu.metaform.client.api.MetaformsApi;
import fi.metatavu.metaform.client.api.RepliesApi;
import fi.metatavu.metaform.client.model.Reply;
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
    
    Metaform metaform = adminMetaformsApi.createMetaform(readMetaform("simple-table"));
    try {
      List<Map<String, Object>> tableData = Arrays.asList(createSimpleTableRow("Text 1", 10d), createSimpleTableRow("Text 2", 20d));
      Map<String, Object> replyData = new HashMap<>();
      replyData.put("table", tableData);
      
      Reply reply = createReplyWithData(replyData);
      
      Reply createdReply = repliesApi.createReply(metaform.getId(), reply, null, ReplyMode.REVISION.toString());
      try {
        Reply foundReply = repliesApi.findReply(metaform.getId(), createdReply.getId(), (String) null);
        assertNotNull(foundReply);
        assertNotNull(foundReply.getId());
        assertTableDataEquals(replyData, foundReply.getData());
      } finally {
        adminRepliesApi.deleteReply(metaform.getId(), createdReply.getId(), (String) null);
      }
    } finally {
      adminMetaformsApi.deleteMetaform(metaform.getId());
    }
  }

  @Test
  public void updateTableReply() throws IOException, URISyntaxException {
    String adminToken = getAdminToken(REALM_1);
    String accessToken = getAccessToken(REALM_1, "test1.realm1", "test");
    MetaformsApi adminMetaformsApi = getMetaformsApi(adminToken);
    RepliesApi repliesApi = getRepliesApi(accessToken);
    RepliesApi adminRepliesApi = getRepliesApi(adminToken);
    
    Metaform metaform = adminMetaformsApi.createMetaform(readMetaform("simple-table"));
    try {
      Map<String, Object> createReplyData = new HashMap<>();
      createReplyData.put("table", Arrays.asList(createSimpleTableRow("Text 1", 10d), createSimpleTableRow("Text 2", 20d)));
      Reply createReply = createReplyWithData(createReplyData);
      
      Reply createdReply = repliesApi.createReply(metaform.getId(), createReply, null, ReplyMode.REVISION.toString());
      try {
        assertTableDataEquals(createReplyData, createReply.getData());

        Reply foundReply = repliesApi.findReply(metaform.getId(), createdReply.getId(), (String) null);
        assertNotNull(foundReply);
        assertNotNull(foundReply.getId());        
        
        assertTableDataEquals(createReplyData, foundReply.getData());

        Map<String, Object> updateReplyData = new HashMap<>();
        updateReplyData.put("table", Arrays.asList(createSimpleTableRow("Added new text", -210d), createSimpleTableRow("Text 1", 10d), createSimpleTableRow("Updated Text 2", 45.5d)));
        Reply updateReply = createReplyWithData(updateReplyData);
        repliesApi.updateReply(metaform.getId(), createdReply.getId(), updateReply, (String) null);
        
        assertTableDataEquals(updateReplyData, updateReply.getData());
        
        Reply foundUpdatedReply = repliesApi.findReply(metaform.getId(), createdReply.getId(), (String) null);
        assertNotNull(foundUpdatedReply);
        assertNotNull(foundUpdatedReply.getId());        
        assertTableDataEquals(updateReplyData, foundUpdatedReply.getData());
        
      } finally {
        adminRepliesApi.deleteReply(metaform.getId(), createdReply.getId(), (String) null);
      }
    } finally {
      adminMetaformsApi.deleteMetaform(metaform.getId());
    }
  }
  
  @Test
  public void nulledTableReply() throws IOException, URISyntaxException {
    String adminToken = getAdminToken(REALM_1);
    String accessToken = getAccessToken(REALM_1, "test1.realm1", "test");
    MetaformsApi adminMetaformsApi = getMetaformsApi(adminToken);
    RepliesApi repliesApi = getRepliesApi(accessToken);
    RepliesApi adminRepliesApi = getRepliesApi(adminToken);
    
    Metaform metaform = adminMetaformsApi.createMetaform(readMetaform("simple-table"));
    try {
      List<Map<String, Object>> tableData = Arrays.asList(createSimpleTableRow(null, null));
      Map<String, Object> replyData = new HashMap<>();
      replyData.put("table", tableData);
      
      Reply reply = createReplyWithData(replyData);
      
      Reply createdReply = repliesApi.createReply(metaform.getId(), reply, null, ReplyMode.REVISION.toString());
      try {
        Reply foundReply = repliesApi.findReply(metaform.getId(), createdReply.getId(), (String) null);
        assertNotNull(foundReply);
        assertNotNull(foundReply.getId());
        assertTableDataEquals(replyData, foundReply.getData());
      } finally {
        adminRepliesApi.deleteReply(metaform.getId(), createdReply.getId(), (String) null);
      }
    } finally {
      adminMetaformsApi.deleteMetaform(metaform.getId());
    }
  }
  
  @Test
  public void nullTableReply() throws IOException, URISyntaxException {
    String adminToken = getAdminToken(REALM_1);
    String accessToken = getAccessToken(REALM_1, "test1.realm1", "test");
    MetaformsApi adminMetaformsApi = getMetaformsApi(adminToken);
    RepliesApi repliesApi = getRepliesApi(accessToken);
    RepliesApi adminRepliesApi = getRepliesApi(adminToken);
    
    Metaform metaform = adminMetaformsApi.createMetaform(readMetaform("simple-table"));
    try {
      Map<String, Object> replyData = new HashMap<>();
      replyData.put("table", null);
      
      Reply reply = createReplyWithData(replyData);
      
      Reply createdReply = repliesApi.createReply(metaform.getId(), reply, null, ReplyMode.REVISION.toString());
      try {
        Reply foundReply = repliesApi.findReply(metaform.getId(), createdReply.getId(), (String) null);
        assertNotNull(foundReply);
        assertNotNull(foundReply.getId());
        assertNull(foundReply.getData().get("table"));
      } finally {
        adminRepliesApi.deleteReply(metaform.getId(), createdReply.getId(), (String) null);
      }
    } finally {
      adminMetaformsApi.deleteMetaform(metaform.getId());
    }
  }
  
  @Test
  public void invalidTableReply() throws IOException, URISyntaxException {
    String adminToken = getAdminToken(REALM_1);
    String accessToken = getAccessToken(REALM_1, "test1.realm1", "test");
    MetaformsApi adminMetaformsApi = getMetaformsApi(adminToken);
    RepliesApi repliesApi = getRepliesApi(accessToken);
    
    Metaform metaform = adminMetaformsApi.createMetaform(readMetaform("simple-table"));
    try {
      Map<String, Object> replyData = new HashMap<>();
      replyData.put("table", "table data");
      
      try {
        Reply reply = createReplyWithData(replyData);
        repliesApi.createReply(metaform.getId(), reply, null, ReplyMode.REVISION.toString());
        fail("Bad request should have been returned");
      } catch (FeignException e) {
        assertEquals(400, e.status());
      }
    } finally {
      adminMetaformsApi.deleteMetaform(metaform.getId());
    }
  }
  
  @Test
  public void deleteTableReply() throws IOException, URISyntaxException {
    String adminToken = getAdminToken(REALM_1);
    String accessToken = getAccessToken(REALM_1, "test1.realm1", "test");
    MetaformsApi adminMetaformsApi = getMetaformsApi(adminToken);
    RepliesApi repliesApi = getRepliesApi(accessToken);
    RepliesApi adminRepliesApi = getRepliesApi(adminToken);
    
    Metaform metaform = adminMetaformsApi.createMetaform(readMetaform("simple-table"));
    try {
      List<Map<String, Object>> tableData = Arrays.asList(createSimpleTableRow("Text 1", 10d), createSimpleTableRow("Text 2", 20d));
      Map<String, Object> replyData = new HashMap<>();
      replyData.put("table", tableData);
      
      Reply reply = createReplyWithData(replyData);
      
      Reply createdReply = repliesApi.createReply(metaform.getId(), reply, null, ReplyMode.REVISION.toString());

      Reply foundReply = repliesApi.findReply(metaform.getId(), createdReply.getId(), (String) null);
      assertNotNull(foundReply);
      adminRepliesApi.deleteReply(metaform.getId(), createdReply.getId(), (String) null);
      try {
        repliesApi.findReply(metaform.getId(), createdReply.getId(), (String) null);
        fail(String.format("Reply %s should not be present", createdReply.getId().toString()));
      } catch (FeignException e) {
        assertEquals(404, e.status());
      }
    } finally {
      adminMetaformsApi.deleteMetaform(metaform.getId());
    }
  }
  
  /**
   * Asserts that table datas equal
   * 
   * @param expected expected table data
   * @param actual actual table data
   */
  private void assertTableDataEquals(Map<String, Object> expected, Map<String, Object> actual) {
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

}
