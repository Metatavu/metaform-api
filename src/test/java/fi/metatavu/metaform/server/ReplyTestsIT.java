package fi.metatavu.metaform.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import static io.restassured.RestAssured.given;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import fi.metatavu.metaform.client.Metaform;
import fi.metatavu.metaform.client.MetaformsApi;
import fi.metatavu.metaform.client.RepliesApi;
import fi.metatavu.metaform.client.Reply;
import fi.metatavu.metaform.client.ReplyData;

@SuppressWarnings ("squid:S1192")
public class ReplyTestsIT extends AbstractIntegrationTest {
  
  private static final String REALM_1 = "test-1";
  
  @Test
  public void createReplyNotLoggedIn() throws IOException, URISyntaxException {
    String adminToken = getAdminToken(REALM_1);
    MetaformsApi adminMetaformsApi = getMetaformsApi(adminToken);
    
    Metaform metaform = adminMetaformsApi.createMetaform(REALM_1, readMetaform("simple"));
    try {
      given()
        .baseUri(getBasePath())
        .header("Content-Type", "application/json")
        .post("/v1/realms/{realmId}/metaforms/{metaformId}/replies", "test-1", metaform.getId())
        .then()
        .assertThat()
        .statusCode(403);
      
    } finally {
      adminMetaformsApi.deleteMetaform(REALM_1, metaform.getId());
    }
  }
  
  @Test
  public void createReply() throws IOException, URISyntaxException {
    String adminToken = getAdminToken(REALM_1);
    String accessToken = getAccessToken(REALM_1, "test1.realm1", "test");
    MetaformsApi adminMetaformsApi = getMetaformsApi(adminToken);
    RepliesApi repliesApi = getRepliesApi(accessToken);
    RepliesApi adminRepliesApi = getRepliesApi(adminToken);
    
    Metaform metaform = adminMetaformsApi.createMetaform(REALM_1, readMetaform("simple"));
    try {
      ReplyData replyData = new ReplyData();
      replyData.put("text", "Test text value");
      Reply reply = createReplyWithData(replyData);
      
      Reply createdReply = repliesApi.createReply(REALM_1, metaform.getId(), reply, Boolean.FALSE);
      try {
        Reply foundReply = repliesApi.findReply(REALM_1, metaform.getId(), createdReply.getId());
        assertNotNull(foundReply);
        assertNotNull(foundReply.getId());
        assertEquals("Test text value", foundReply.getData().get("text"));
      } finally {
        adminRepliesApi.deleteReply(REALM_1, metaform.getId(), createdReply.getId());
      }
    } finally {
      adminMetaformsApi.deleteMetaform(REALM_1, metaform.getId());
    }
  }
  
  @Test
  public void createReplyUpdateExisting() throws IOException, URISyntaxException {
    String adminToken = getAdminToken(REALM_1);
    String accessToken = getAccessToken(REALM_1, "test1.realm1", "test");
    MetaformsApi adminMetaformsApi = getMetaformsApi(adminToken);
    RepliesApi repliesApi = getRepliesApi(accessToken);
    RepliesApi adminRepliesApi = getRepliesApi(adminToken);
    
    Metaform metaform = adminMetaformsApi.createMetaform(REALM_1, readMetaform("simple"));
    try {
      ReplyData replyData1 = new ReplyData();
      replyData1.put("text", "Test text value");
      Reply reply1 = createReplyWithData(replyData1);

      ReplyData replyData2 = new ReplyData();
      replyData2.put("text", "Updated text value");
      Reply reply2 = createReplyWithData(replyData2);
      
      Reply createdReply1 = repliesApi.createReply(REALM_1, metaform.getId(), reply1, Boolean.TRUE);
      try {
        assertNotNull(createdReply1);
        assertNotNull(createdReply1.getId());
        assertEquals("Test text value", createdReply1.getData().get("text"));

        Reply createdReply2 = repliesApi.createReply(REALM_1, metaform.getId(), reply2, Boolean.TRUE);
        assertNotNull(createdReply2);
        assertEquals(createdReply1.getId(), createdReply2.getId());
        assertEquals("Updated text value", createdReply2.getData().get("text"));
      } finally {
        adminRepliesApi.deleteReply(REALM_1, metaform.getId(), createdReply1.getId());
      }
    } finally {
      adminMetaformsApi.deleteMetaform(REALM_1, metaform.getId());
    }
  }

  @Test
  public void createReplyVersionExisting() throws IOException, URISyntaxException {
    String adminToken = getAdminToken(REALM_1);
    String accessToken = getAccessToken(REALM_1, "test1.realm1", "test");
    MetaformsApi adminMetaformsApi = getMetaformsApi(adminToken);
    RepliesApi repliesApi = getRepliesApi(accessToken);
    RepliesApi adminRepliesApi = getRepliesApi(adminToken);
    
    Metaform metaform = adminMetaformsApi.createMetaform(REALM_1, readMetaform("simple"));
    try {
      ReplyData replyData1 = new ReplyData();
      replyData1.put("text", "Test text value");
      Reply reply1 = createReplyWithData(replyData1);

      ReplyData replyData2 = new ReplyData();
      replyData2.put("text", "Updated text value");
      Reply reply2 = createReplyWithData(replyData2);
      
      Reply createdReply1 = repliesApi.createReply(REALM_1, metaform.getId(), reply1, Boolean.FALSE);
      try {
        assertNotNull(createdReply1);
        assertNotNull(createdReply1.getId());
        assertEquals("Test text value", createdReply1.getData().get("text"));

        Reply createdReply2 = repliesApi.createReply(REALM_1, metaform.getId(), reply2, Boolean.FALSE);
        assertNotNull(createdReply2);
        assertNotEquals(createdReply1.getId(), createdReply2.getId());
        assertEquals("Updated text value", createdReply2.getData().get("text"));
        
        List<Reply> replies = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, null);
        
        assertEquals(2, replies.size());
        assertNotNull(replies.get(0).getRevision());
        assertEquals("Test text value", replies.get(0).getData().get("text"));
        assertNull(replies.get(1).getRevision());
        assertEquals("Updated text value", replies.get(1).getData().get("text"));
      } finally {
        adminRepliesApi.deleteReply(REALM_1, metaform.getId(), createdReply1.getId());
      }
    } finally {
      adminMetaformsApi.deleteMetaform(REALM_1, metaform.getId());
    }
  }
  
  @Test
  public void listRepliesByTextFields() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      Metaform metaform = dataBuilder.createMetaform("tbnc");
      
      assertNotNull(metaform);
      
      dataBuilder.createTBNCReply(metaform, "test 1", Boolean.TRUE, 1.0d, new String[] { "option 1" });
      dataBuilder.createTBNCReply(metaform, "test 2", Boolean.FALSE, 2.5d, new String[] { "option 2" });
      dataBuilder.createTBNCReply(metaform, "test 3", null, 0d, new String[] { });
      
      RepliesApi repliesApi = dataBuilder.getRepliesApi();
      
      List<Reply> replies1 = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("text:test 1"));
      List<Reply> replies2 = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("text:test 2"));
      List<Reply> repliesBoth = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("text:test 1", "text:test 2"));
      List<Reply> repliesNone = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("text:non", "text:existing"));
      List<Reply> notReplies = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("text^test 1"));
  
      assertEquals(1, replies1.size());
      assertEquals("test 1", replies1.get(0).getData().get("text"));
      
      assertEquals(1, replies2.size());
      assertEquals("test 2", replies2.get(0).getData().get("text"));
          
      assertEquals(0, repliesBoth.size());
          
      assertEquals(2, notReplies.size());
      assertEquals("test 2", notReplies.get(0).getData().get("text"));
      assertEquals("test 3", notReplies.get(1).getData().get("text"));

      assertEquals(0, repliesNone.size());
    } finally {
      dataBuilder.clean();
    }
  }
  
  @Test
  public void listRepliesByListFields() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      Metaform metaform = dataBuilder.createMetaform("tbnc");
      
      assertNotNull(metaform);
      
      dataBuilder.createTBNCReply(metaform, "test 1", Boolean.TRUE, 1.0d, new String[] { "option 1" });
      dataBuilder.createTBNCReply(metaform, "test 2", Boolean.FALSE, 2.5d, new String[] { "option 2" });
      dataBuilder.createTBNCReply(metaform, "test 3", null, 0d, new String[] { });
      
      RepliesApi repliesApi = dataBuilder.getRepliesApi();
      
      List<Reply> replies1 = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("checklist:option 1"));
      List<Reply> replies2 = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("checklist:option 2"));
      List<Reply> repliesBoth = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("checklist:option 1", "checklist:option 2"));
      List<Reply> repliesNone = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("checklist:non", "checklist:existing"));
      List<Reply> notReplies = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("checklist^option 1"));

      assertEquals(1, replies1.size());
      assertEquals("test 1", replies1.get(0).getData().get("text"));
      
      assertEquals(1, replies2.size());
      assertEquals("test 2", replies2.get(0).getData().get("text"));
          
      assertEquals(0, repliesBoth.size());
      assertEquals(0, repliesNone.size());
      
      assertEquals(2, notReplies.size());
      assertEquals("test 2", notReplies.get(0).getData().get("text"));
      assertEquals("test 3", notReplies.get(1).getData().get("text"));
    } finally {
      dataBuilder.clean();
    }
  }
  
  @Test
  public void listRepliesByNumberFields() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      Metaform metaform = dataBuilder.createMetaform("tbnc");
      
      assertNotNull(metaform);
      
      dataBuilder.createTBNCReply(metaform, "test 1", Boolean.TRUE, 1.0d, new String[] { "option 1" });
      dataBuilder.createTBNCReply(metaform, "test 2", Boolean.FALSE, 2.5d, new String[] { "option 2" });
      dataBuilder.createTBNCReply(metaform, "test 3", null, 0d, new String[] { });
      
      RepliesApi repliesApi = dataBuilder.getRepliesApi();
      
      List<Reply> replies1 = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("number:1"));
      List<Reply> replies2 = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("number:2.5"));
      List<Reply> repliesBoth = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("number:1", "number:2.5"));
      List<Reply> repliesNone = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("number:55", "number:66"));
      List<Reply> notReplies = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("number^1"));
  
      assertEquals(1, replies1.size());
      assertEquals("test 1", replies1.get(0).getData().get("text"));
      
      assertEquals(1, replies2.size());
      assertEquals("test 2", replies2.get(0).getData().get("text"));
          
      assertEquals(0, repliesBoth.size());
      
      assertEquals(2, notReplies.size());
      assertEquals("test 2", notReplies.get(0).getData().get("text"));
      assertEquals("test 3", notReplies.get(1).getData().get("text"));

      assertEquals(0, repliesNone.size()); 
    } finally {
      dataBuilder.clean();
    }
  }
  
  @Test
  public void listRepliesByBooleanFields() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      Metaform metaform = dataBuilder.createMetaform("tbnc");
      
      assertNotNull(metaform);
      
      dataBuilder.createTBNCReply(metaform, "test 1", Boolean.TRUE, 1.0d, new String[] { "option 1" });
      dataBuilder.createTBNCReply(metaform, "test 2", Boolean.FALSE, 2.5d, new String[] { "option 2" });
      dataBuilder.createTBNCReply(metaform, "test 3", null, 0d, new String[] { });
      
      RepliesApi repliesApi = dataBuilder.getRepliesApi();
      
      List<Reply> replies1 = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("boolean:true"));
      List<Reply> replies2 = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("boolean:false"));
      List<Reply> notReplies = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("boolean^false"));
     
      assertEquals(1, replies1.size());
      assertEquals("test 1", replies1.get(0).getData().get("text"));
      
      assertEquals(1, replies2.size());
      assertEquals("test 2", replies2.get(0).getData().get("text"));
      
      assertEquals(2, notReplies.size());
      assertEquals("test 1", notReplies.get(0).getData().get("text"));
      assertEquals("test 3", notReplies.get(1).getData().get("text"));
    } finally {
      dataBuilder.clean();
    }
  }
  
  @Test
  public void listRepliesByMultiFields() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      Metaform metaform = dataBuilder.createMetaform("tbnc");
      
      assertNotNull(metaform);
      
      dataBuilder.createTBNCReply(metaform, "test 1", Boolean.TRUE, 1.0d, new String[] { "option 1" });
      dataBuilder.createTBNCReply(metaform, "test 2", Boolean.FALSE, 2.5d, new String[] { "option 2" });
      dataBuilder.createTBNCReply(metaform, "test 3", null, 0d, new String[] { });
      
      RepliesApi repliesApi = dataBuilder.getRepliesApi();
      
      List<Reply> replies1 = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("boolean:true", "number:1"));
      List<Reply> replies2 = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("boolean:false", "number:1"));
      List<Reply> replies3 = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("checklist:option 1", "boolean:true"));
      List<Reply> replies4 = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("checklist^option 1", "boolean:false"));
      
      assertEquals(1, replies1.size());
      assertEquals("test 1", replies1.get(0).getData().get("text"));
      
      assertEquals(0, replies2.size());
      
      assertEquals(1, replies3.size());
      assertEquals("test 1", replies3.get(0).getData().get("text"));
      
      assertEquals(1, replies4.size());
      assertEquals("test 2", replies4.get(0).getData().get("text"));
    } finally {
      dataBuilder.clean();
    }
  }

  /**
   * Creates a reply object with given data
   * 
   * @param replyData reply data
   * @return reply object with given data
   */
  private Reply createReplyWithData(ReplyData replyData) {
    Reply reply = new Reply();
    reply.setData(replyData);
    return reply;
  }
  
}
