package fi.metatavu.metaform.server;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import fi.metatavu.metaform.client.ExportTheme;
import fi.metatavu.metaform.client.Metaform;
import fi.metatavu.metaform.client.MetaformsApi;
import fi.metatavu.metaform.client.RepliesApi;
import fi.metatavu.metaform.client.Reply;
import fi.metatavu.metaform.client.ReplyData;
import fi.metatavu.metaform.server.rest.ReplyMode;

@SuppressWarnings ("squid:S1192")
public class ReplyTestsIT extends AbstractIntegrationTest {
  
  private static final ZoneId TIMEZONE = ZoneId.of("Europe/Helsinki");

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
      
      Reply createdReply = repliesApi.createReply(REALM_1, metaform.getId(), reply, null, ReplyMode.REVISION.toString());
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
      
      Reply createdReply1 = repliesApi.createReply(REALM_1, metaform.getId(), reply1, null, ReplyMode.UPDATE.toString());
      try {
        assertNotNull(createdReply1);
        assertNotNull(createdReply1.getId());
        assertEquals("Test text value", createdReply1.getData().get("text"));

        Reply createdReply2 = repliesApi.createReply(REALM_1, metaform.getId(), reply2,  null, ReplyMode.UPDATE.toString());
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
      
      Reply createdReply1 = repliesApi.createReply(REALM_1, metaform.getId(), reply1, null, ReplyMode.REVISION.toString());
      try {
        assertNotNull(createdReply1);
        assertNotNull(createdReply1.getId());
        assertEquals("Test text value", createdReply1.getData().get("text"));

        Reply createdReply2 = repliesApi.createReply(REALM_1, metaform.getId(), reply2, null, ReplyMode.REVISION.toString());
        assertNotNull(createdReply2);
        assertNotEquals(createdReply1.getId(), createdReply2.getId());
        assertEquals("Updated text value", createdReply2.getData().get("text"));
        
        List<Reply> replies = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, null, null, null);
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
  public void createReplyCumulative() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      Metaform metaform = dataBuilder.createMetaform("simple");
      RepliesApi repliesApi = dataBuilder.getRepliesApi();
      assertNotNull(metaform);

      dataBuilder.createSimpleReply(metaform, "val 1", ReplyMode.CUMULATIVE);
      dataBuilder.createSimpleReply(metaform, "val 2", ReplyMode.CUMULATIVE);
      dataBuilder.createSimpleReply(metaform, "val 3", ReplyMode.CUMULATIVE);
      
      List<Reply> replies = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, null, null, null, null);
      
      assertEquals(3, replies.size());
      assertEquals("val 1", replies.get(0).getData().get("text"));
      assertEquals("val 2", replies.get(1).getData().get("text"));
      assertEquals("val 3", replies.get(2).getData().get("text"));
    } finally {
      dataBuilder.clean();
    }
  }
  
  @Test
  public void testUpdateReply() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      RepliesApi repliesApi = dataBuilder.getRepliesApi();
      Metaform metaform = dataBuilder.createMetaform("simple");      
      Reply reply = dataBuilder.createSimpleReply(metaform, "test 1", ReplyMode.UPDATE);

      ReplyData updateData = new ReplyData();
      updateData.put("text", "Updated text value");
      reply.setData(updateData);
      
      repliesApi.updateReply(REALM_1, metaform.getId(), reply.getId(), reply);
      
      Reply updatedReply = repliesApi.findReply(REALM_1, metaform.getId(), reply.getId());
      assertEquals("Updated text value", updatedReply.getData().get("text"));
    } finally {
      dataBuilder.clean();
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
      
      List<Reply> replies1 = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("text:test 1"), null, null);
      List<Reply> replies2 = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("text:test 2"), null, null);
      List<Reply> repliesBoth = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("text:test 1", "text:test 2"), null, null);
      List<Reply> repliesNone = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("text:non", "text:existing"), null, null);
      List<Reply> notReplies = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("text^test 1"), null, null);
  
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
      
      List<Reply> replies1 = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("checklist:option 1"), null, null);
      List<Reply> replies2 = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("checklist:option 2"), null, null);
      List<Reply> repliesBoth = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("checklist:option 1", "checklist:option 2"), null, null);
      List<Reply> repliesNone = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("checklist:non", "checklist:existing"), null, null);
      List<Reply> notReplies = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("checklist^option 1"), null, null);
      
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
      
      List<Reply> replies1 = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("number:1"), null, null);
      List<Reply> replies2 = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("number:2.5"), null, null);
      List<Reply> repliesBoth = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("number:1", "number:2.5"), null, null);
      List<Reply> repliesNone = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("number:55", "number:66"), null, null);
      List<Reply> notReplies = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("number^1"), null, null);
  
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
      
      List<Reply> replies1 = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("boolean:true"), null, null);
      List<Reply> replies2 = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("boolean:false"), null, null);
      List<Reply> notReplies = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("boolean^false"), null, null);
     
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
      
      List<Reply> replies1 = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("boolean:true", "number:1"), null, null);
      List<Reply> replies2 = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("boolean:false", "number:1"), null, null);
      List<Reply> replies3 = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("checklist:option 1", "boolean:true"), null, null);
      List<Reply> replies4 = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("checklist^option 1", "boolean:false"), null, null);
      
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
  
  @Test
  public void listRepliesByCreatedBefore() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      Metaform metaform = dataBuilder.createMetaform("simple");      

      Reply reply1 = dataBuilder.createSimpleReply(metaform, "test 1", ReplyMode.CUMULATIVE);
      Reply reply2 = dataBuilder.createSimpleReply(metaform, "test 2", ReplyMode.CUMULATIVE);
      Reply reply3 = dataBuilder.createSimpleReply(metaform, "test 3", ReplyMode.CUMULATIVE);
      
      updateReplyCreated(reply1, getOffsetDateTime(2018, 5, 25, TIMEZONE));
      updateReplyCreated(reply2, getOffsetDateTime(2018, 5, 27, TIMEZONE));
      updateReplyCreated(reply3, getOffsetDateTime(2018, 5, 29, TIMEZONE));
      
      RepliesApi repliesApi = dataBuilder.getRepliesApi();
      
      List<Reply> allReplies = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.FALSE, null, null, null);
      List<Reply> createdBefore26 = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, getIsoDateTime(2018, 5, 26, TIMEZONE), null, null, null, Boolean.FALSE, null, null, null);
      List<Reply> createdAfter26 = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, getIsoDateTime(2018, 5, 26, TIMEZONE), null, null, Boolean.FALSE, null, null, null);

      assertEquals(3, allReplies.size());
      assertEquals("test 1", allReplies.get(0).getData().get("text"));
      assertEquals("test 2", allReplies.get(1).getData().get("text"));
      assertEquals("test 3", allReplies.get(2).getData().get("text"));

      assertEquals(1, createdBefore26.size());
      assertEquals("test 1", createdBefore26.get(0).getData().get("text"));

      assertEquals(2, createdAfter26.size());
      assertEquals("test 2", createdAfter26.get(0).getData().get("text"));
      assertEquals("test 3", createdAfter26.get(1).getData().get("text"));
    } finally {
      dataBuilder.clean();
    }
  }
  
  @Test
  public void listRepliesByModifiedBefore() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      Metaform metaform = dataBuilder.createMetaform("simple");      

      Reply reply1 = dataBuilder.createSimpleReply(metaform, "test 1", ReplyMode.CUMULATIVE);
      Reply reply2 = dataBuilder.createSimpleReply(metaform, "test 2", ReplyMode.CUMULATIVE);
      Reply reply3 = dataBuilder.createSimpleReply(metaform, "test 3", ReplyMode.CUMULATIVE);
      
      updateReplyModified(reply1, getOffsetDateTime(2018, 5, 25, TIMEZONE));
      updateReplyModified(reply2, getOffsetDateTime(2018, 5, 27, TIMEZONE));
      updateReplyModified(reply3, getOffsetDateTime(2018, 5, 29, TIMEZONE));
      
      RepliesApi repliesApi = dataBuilder.getRepliesApi();
      
      List<Reply> allReplies = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.FALSE, null, null, null);
      List<Reply> modifiedBefore26 = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, getIsoDateTime(2018, 5, 26, TIMEZONE), null, Boolean.FALSE, null, null, null);
      List<Reply> modifiedAfter26 = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, getIsoDateTime(2018, 5, 26, TIMEZONE), Boolean.FALSE, null, null, null);
      
      assertEquals(3, allReplies.size());
      assertEquals("test 1", allReplies.get(0).getData().get("text"));
      assertEquals("test 2", allReplies.get(1).getData().get("text"));
      assertEquals("test 3", allReplies.get(2).getData().get("text"));

      assertEquals(1, modifiedBefore26.size());
      assertEquals("test 1", modifiedBefore26.get(0).getData().get("text"));

      assertEquals(2, modifiedAfter26.size());
      assertEquals("test 2", modifiedAfter26.get(0).getData().get("text"));
      assertEquals("test 3", modifiedAfter26.get(1).getData().get("text"));
    } finally {
      dataBuilder.clean();
    }
  }
  
  @Test
  public void testMetafields() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      Metaform metaform = dataBuilder.createMetaform("simple-meta");      

      Reply createdReply = dataBuilder.createSimpleReply(metaform, "test 1", ReplyMode.CUMULATIVE);
      
      OffsetDateTime replyCreated = getReplyCreated(createdReply, TIMEZONE);
      OffsetDateTime replyModified = getReplyModified(createdReply, TIMEZONE);

      RepliesApi repliesApi = dataBuilder.getRepliesApi();
      
      Reply reply = repliesApi.findReply(REALM_1, metaform.getId(), createdReply.getId());
      assertEquals(replyCreated.truncatedTo(ChronoUnit.MINUTES).toInstant(), parseOffsetDateTime((String) reply.getData().get("created")).truncatedTo(ChronoUnit.MINUTES).toInstant());
      assertEquals(replyModified.truncatedTo(ChronoUnit.MINUTES).toInstant(), parseOffsetDateTime((String) reply.getData().get("modified")).truncatedTo(ChronoUnit.MINUTES).toInstant());
      assertEquals(REALM1_USER_1_ID.toString(), reply.getData().get("lastEditor"));
    } finally {
      dataBuilder.clean();
    }
  }
  
  @Test
  public void testExportReplyPdf() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      MetaformsApi adminMetaformsApi = dataBuilder.getAdminMetaformsApi();

      ExportTheme theme = dataBuilder.createSimpleExportTheme();
      dataBuilder.createSimpleExportThemeFile(theme.getId(), "reply/pdf.ftl", "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></meta><title>title</title></head><body>content</body></html>");
      Metaform metaform = dataBuilder.createMetaform("simple");
      metaform.setExportThemeId(theme.getId());
      adminMetaformsApi.updateMetaform(REALM_1, metaform.getId(), metaform);
      Reply reply = dataBuilder.createSimpleReply(metaform, "test 1", ReplyMode.UPDATE);

      given()
        .baseUri(getBasePath())
        .header("Authorization", String.format("Bearer %s", getAdminToken(REALM_1)))
        .get("/v1/realms/{realmId}/metaforms/{metaformId}/replies/{replyId}/export?format=PDF", REALM_1, metaform.getId().toString(), reply.getId().toString())
        .then()
        .assertThat()
        .statusCode(200)
        .header("Content-Type", "application/pdf");   
    } finally {
      dataBuilder.clean();
    }
  }
  
  @Test
  public void testExportReplyPdfFilesEmpty() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      MetaformsApi adminMetaformsApi = dataBuilder.getAdminMetaformsApi();

      ExportTheme theme = dataBuilder.createSimpleExportTheme();
      dataBuilder.createSimpleExportThemeFile(theme.getId(), "reply/pdf.ftl", "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></meta><title>title</title></head><body>content</body></html>");
      Metaform metaform = dataBuilder.createMetaform("simple-files");
      metaform.setExportThemeId(theme.getId());
      adminMetaformsApi.updateMetaform(REALM_1, metaform.getId(), metaform);
      Reply reply = dataBuilder.createSimpleReply(metaform, "test 1", ReplyMode.UPDATE);

      given()
        .baseUri(getBasePath())
        .header("Authorization", String.format("Bearer %s", getAdminToken(REALM_1)))
        .get("/v1/realms/{realmId}/metaforms/{metaformId}/replies/{replyId}/export?format=PDF", REALM_1, metaform.getId().toString(), reply.getId().toString())
        .then()
        .assertThat()
        .statusCode(200)
        .header("Content-Type", "application/pdf");   
    } finally {
      dataBuilder.clean();
    }
  }

  /**
   * Returns when reply is created from the database
   * 
   * @param reply reply 
   * @param zone zone
   * @return when reply is created from the database
   */
  private OffsetDateTime getReplyCreated(Reply reply, ZoneId zone) {
    Timestamp createdAt = executeSelectSingle("SELECT createdAt FROM Reply WHERE id = ?", (resultSet) -> {
      try {
        return resultSet.getTimestamp("createdAt");
      } catch (SQLException e) {
        fail(e.getMessage());
        return null;
      }
    }, reply.getId());
    
    return OffsetDateTime.ofInstant(createdAt.toInstant(), zone);
  }
  
  /**
   * Returns when reply is last modified from the database
   * 
   * @param reply reply 
   * @param zone zone
   * @return when reply is last modified from the database
   */
  private OffsetDateTime getReplyModified(Reply reply, ZoneId zone) {
    Timestamp modifiedAt = executeSelectSingle("SELECT modifiedAt FROM Reply WHERE id = ?", (resultSet) -> {
      try {
        return resultSet.getTimestamp("modifiedAt");
      } catch (SQLException e) {
        fail(e.getMessage());
        return null;
      }
    }, reply.getId());
    
    return OffsetDateTime.ofInstant(modifiedAt.toInstant(), zone);
  }

  /**
   * Updates reply to be created at specific time
   * 
   * @param reply reply
   * @param created created
   */
  private void updateReplyCreated(Reply reply, OffsetDateTime created) {
    executeUpdate("UPDATE Reply SET createdAt = ? WHERE id = ?", created, reply.getId());
    flushCache();
  }
  
  /**
   * Updates reply to be modified at specific time
   * 
   * @param reply reply
   * @param modified created
   */
  private void updateReplyModified(Reply reply, OffsetDateTime modified) {
    executeUpdate("UPDATE Reply SET modifiedAt = ? WHERE id = ?", modified, reply.getId());
    flushCache();
  }
  
}
