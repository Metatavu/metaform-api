package fi.metatavu.metaform.test.functional;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import feign.FeignException;
import fi.metatavu.metaform.client.model.ExportTheme;
import fi.metatavu.metaform.client.model.Metaform;
import fi.metatavu.metaform.client.api.MetaformsApi;
import fi.metatavu.metaform.client.api.RepliesApi;
import fi.metatavu.metaform.client.model.Reply;
import fi.metatavu.metaform.server.rest.ReplyMode;

@SuppressWarnings ("squid:S1192")
public class ReplyTestsIT extends AbstractIntegrationTest {
  
  private static final ZoneId TIMEZONE = ZoneId.of("Europe/Helsinki");

  @Test
  public void createReplyNotLoggedIn() throws IOException, URISyntaxException {
    String adminToken = getAdminToken(REALM_1);
    MetaformsApi adminMetaformsApi = getMetaformsApi(adminToken);
    
    Metaform metaform = adminMetaformsApi.createMetaform(readMetaform("simple"));
    try {
      given()
        .baseUri(getBasePath())
        .header("Content-Type", "application/json")
        .post("/v1/metaforms/{metaformId}/replies", metaform.getId())
        .then()
        .assertThat()
        .statusCode(401);
      
    } finally {
      adminMetaformsApi.deleteMetaform(metaform.getId());
    }
  }
  
  @Test
  public void createReply() throws IOException, URISyntaxException {
    String adminToken = getAdminToken(REALM_1);
    String accessToken = getAccessToken(REALM_1, "test1.realm1", "test");
    MetaformsApi adminMetaformsApi = getMetaformsApi(adminToken);
    RepliesApi repliesApi = getRepliesApi(accessToken);
    RepliesApi adminRepliesApi = getRepliesApi(adminToken);
    
    Metaform metaform = adminMetaformsApi.createMetaform(readMetaform("simple"));
    try {
      Map<String, Object> replyData = new HashMap<>();
      replyData.put("text", "Test text value");
      Reply reply = createReplyWithData(replyData);
      
      Reply createdReply = repliesApi.createReply(metaform.getId(), reply, null, ReplyMode.REVISION.toString());
      try {
        Reply foundReply = repliesApi.findReply(metaform.getId(), createdReply.getId(), (String) null);
        assertNotNull(foundReply);
        assertNotNull(foundReply.getId());
        assertEquals("Test text value", foundReply.getData().get("text"));
      } finally {
        adminRepliesApi.deleteReply(metaform.getId(), createdReply.getId(), (String) null);
      }
    } finally {
      adminMetaformsApi.deleteMetaform(metaform.getId());
    }
  }
  
  @Test
  public void createReplyUpdateExisting() throws IOException, URISyntaxException {
    String adminToken = getAdminToken(REALM_1);
    String accessToken = getAccessToken(REALM_1, "test1.realm1", "test");
    MetaformsApi adminMetaformsApi = getMetaformsApi(adminToken);
    RepliesApi repliesApi = getRepliesApi(accessToken);
    RepliesApi adminRepliesApi = getRepliesApi(adminToken);
    
    Metaform metaform = adminMetaformsApi.createMetaform(readMetaform("simple"));
    try {
      Map<String, Object> replyData1 = new HashMap<>();
      replyData1.put("text", "Test text value");
      Reply reply1 = createReplyWithData(replyData1);

      Map<String, Object> replyData2 = new HashMap<>();
      replyData2.put("text", "Updated text value");
      Reply reply2 = createReplyWithData(replyData2);
      
      Reply createdReply1 = repliesApi.createReply(metaform.getId(), reply1, null, ReplyMode.UPDATE.toString());
      try {
        assertNotNull(createdReply1);
        assertNotNull(createdReply1.getId());
        assertEquals("Test text value", createdReply1.getData().get("text"));

        Reply createdReply2 = repliesApi.createReply(metaform.getId(), reply2,  null, ReplyMode.UPDATE.toString());
        assertNotNull(createdReply2);
        assertEquals(createdReply1.getId(), createdReply2.getId());
        assertEquals("Updated text value", createdReply2.getData().get("text"));
      } finally {
        adminRepliesApi.deleteReply(metaform.getId(), createdReply1.getId(), (String) null);
      }
    } finally {
      adminMetaformsApi.deleteMetaform(metaform.getId());
    }
  }

  @Test
  public void createReplyVersionExisting() throws IOException, URISyntaxException {
    String adminToken = getAdminToken(REALM_1);
    String accessToken = getAccessToken(REALM_1, "test1.realm1", "test");
    MetaformsApi adminMetaformsApi = getMetaformsApi(adminToken);
    RepliesApi repliesApi = getRepliesApi(accessToken);
    RepliesApi adminRepliesApi = getRepliesApi(adminToken);
    
    Metaform metaform = adminMetaformsApi.createMetaform(readMetaform("simple"));
    try {
      Map<String, Object> replyData1 = new HashMap<>();
      replyData1.put("text", "Test text value");
      Reply reply1 = createReplyWithData(replyData1);

      Map<String, Object> replyData2 = new HashMap<>();
      replyData2.put("text", "Updated text value");
      Reply reply2 = createReplyWithData(replyData2);
      
      Reply createdReply1 = repliesApi.createReply(metaform.getId(), reply1, null, ReplyMode.REVISION.toString());
      try {
        assertNotNull(createdReply1);
        assertNotNull(createdReply1.getId());
        assertEquals("Test text value", createdReply1.getData().get("text"));

        Reply createdReply2 = repliesApi.createReply(metaform.getId(), reply2, null, ReplyMode.REVISION.toString());
        assertNotNull(createdReply2);
        assertNotEquals(createdReply1.getId(), createdReply2.getId());
        assertEquals("Updated text value", createdReply2.getData().get("text"));
        
        List<Reply> replies = repliesApi.listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, null, null, null);
        assertEquals(2, replies.size());
        assertNotNull(replies.get(0).getRevision());
        assertEquals("Test text value", replies.get(0).getData().get("text"));
        assertNull(replies.get(1).getRevision());
        assertEquals("Updated text value", replies.get(1).getData().get("text"));
      } finally {
        adminRepliesApi.deleteReply(metaform.getId(), createdReply1.getId(), (String) null);
      }
    } finally {
      adminMetaformsApi.deleteMetaform(metaform.getId());
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
      
      List<Reply> replies = repliesApi.listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null, null, null, null, null);
      
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

      Map<String, Object> updateData = new HashMap<>();
      updateData.put("text", "Updated text value");
      reply.setData(updateData);
      
      repliesApi.updateReply(metaform.getId(), reply.getId(), reply, (String) null);
      
      Reply updatedReply = repliesApi.findReply(metaform.getId(), reply.getId(), (String) null);
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
      
      List<Reply> replies1 = repliesApi.listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("text:test 1"), null, null);
      List<Reply> replies2 = repliesApi.listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("text:test 2"), null, null);
      List<Reply> repliesBoth = repliesApi.listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("text:test 1", "text:test 2"), null, null);
      List<Reply> repliesNone = repliesApi.listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("text:non", "text:existing"), null, null);
      List<Reply> notReplies = repliesApi.listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("text^test 1"), null, null);
  
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
      
      List<Reply> replies1 = repliesApi.listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("checklist:option 1"), null, null);
      List<Reply> replies2 = repliesApi.listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("checklist:option 2"), null, null);
      List<Reply> repliesBoth = repliesApi.listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("checklist:option 1", "checklist:option 2"), null, null);
      List<Reply> repliesNone = repliesApi.listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("checklist:non", "checklist:existing"), null, null);
      List<Reply> notReplies = repliesApi.listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("checklist^option 1"), null, null);
      
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
      
      List<Reply> replies1 = repliesApi.listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("number:1"), null, null);
      List<Reply> replies2 = repliesApi.listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("number:2.5"), null, null);
      List<Reply> repliesBoth = repliesApi.listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("number:1", "number:2.5"), null, null);
      List<Reply> repliesNone = repliesApi.listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("number:55", "number:66"), null, null);
      List<Reply> notReplies = repliesApi.listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("number^1"), null, null);
  
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
      
      List<Reply> replies1 = repliesApi.listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("boolean:true"), null, null);
      List<Reply> replies2 = repliesApi.listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("boolean:false"), null, null);
      List<Reply> notReplies = repliesApi.listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("boolean^false"), null, null);
     
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
      
      List<Reply> replies1 = repliesApi.listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("boolean:true", "number:1"), null, null);
      List<Reply> replies2 = repliesApi.listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("boolean:false", "number:1"), null, null);
      List<Reply> replies3 = repliesApi.listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("checklist:option 1", "boolean:true"), null, null);
      List<Reply> replies4 = repliesApi.listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, Arrays.asList("checklist^option 1", "boolean:false"), null, null);
      
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
      
      List<Reply> allReplies = repliesApi.listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.FALSE, null, null, null);
      List<Reply> createdBefore26 = repliesApi.listReplies(metaform.getId(), REALM1_USER_1_ID, getIsoDateTime(2018, 5, 26, TIMEZONE), null, null, null, Boolean.FALSE, null, null, null);
      List<Reply> createdAfter26 = repliesApi.listReplies(metaform.getId(), REALM1_USER_1_ID, null, getIsoDateTime(2018, 5, 26, TIMEZONE), null, null, Boolean.FALSE, null, null, null);

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
      
      List<Reply> allReplies = repliesApi.listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.FALSE, null, null, null);
      List<Reply> modifiedBefore26 = repliesApi.listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, getIsoDateTime(2018, 5, 26, TIMEZONE), null, Boolean.FALSE, null, null, null);
      List<Reply> modifiedAfter26 = repliesApi.listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, getIsoDateTime(2018, 5, 26, TIMEZONE), Boolean.FALSE, null, null, null);
      
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
      
      OffsetDateTime replyCreated = createdReply.getCreatedAt();
      OffsetDateTime replyModified = createdReply.getModifiedAt();

      RepliesApi repliesApi = dataBuilder.getRepliesApi();
      
      Reply reply = repliesApi.findReply(metaform.getId(), createdReply.getId(), (String) null);
      assertEquals(replyCreated.truncatedTo(ChronoUnit.MINUTES).toInstant(), parseOffsetDateTime((String) reply.getData().get("created")).truncatedTo(ChronoUnit.MINUTES).toInstant());
      assertEquals(replyModified.truncatedTo(ChronoUnit.MINUTES).toInstant(), parseOffsetDateTime((String) reply.getData().get("modified")).truncatedTo(ChronoUnit.MINUTES).toInstant());
      assertEquals(REALM1_USER_1_ID.toString(), reply.getData().get("lastEditor"));
    } finally {
      dataBuilder.clean();
    }
  }

  @Test
  public void testFindReplyOwnerKeys() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      Metaform metaform = dataBuilder.createMetaform("simple-owner-keys");
      
      Reply reply1 = dataBuilder.createSimpleReply(metaform, "test 1", ReplyMode.CUMULATIVE);
      Reply reply2 = dataBuilder.createSimpleReply(metaform, "test 2", ReplyMode.CUMULATIVE);
      assertNotNull(reply1.getOwnerKey());
      assertNotNull(reply2.getOwnerKey());
      assertNotEquals(reply1.getOwnerKey(), reply2.getOwnerKey());

      RepliesApi anonymousRepliesApi = getRepliesApi(getAnonymousToken(REALM_1));
      anonymousRepliesApi.findReply(metaform.getId(), reply1.getId(), reply1.getOwnerKey());

      assertReplyOwnerKeyFindForbidden(anonymousRepliesApi, metaform, reply1, null);
      assertReplyOwnerKeyFindForbidden(anonymousRepliesApi, metaform, reply1, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgZEinFR6yjNnV4utVbvU9KQ8lZasbAWKQJXjp2VzcyQfE1WvH5dMJmI-sgmPyaZFcJLk9tgLfZilytGmeopfafkWp7yVa9zWYAfni0eJmCL9EcemRb_rDNw07Mf1Vb1lbLhLth8r6a2SVGk_dK14TXeGQSX9zROmiqjeT_FXe2Yz8EnvUweLQ5TobVE-azzyW0dqzjoSkBNfZ8r2hot4hQ2mQthU5xaAnOfDeCc95E7cci4-Dnx3B8U_UaHOn7Srf6DdsL_ZDvnSh1CvYiGNOYMpk-TLK6ixH-m83rweBjQ1hl1N_5I3cplmuJGCJLBxDauyjfBYIfR9WkBoau1eDQIDAQAB");
      assertReplyOwnerKeyFindForbidden(anonymousRepliesApi, metaform, reply1, reply2.getOwnerKey());
    } finally {
      dataBuilder.clean();
    }
  }

  @Test
  public void testUpdateReplyOwnerKeys() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      Metaform metaform = dataBuilder.createMetaform("simple-owner-keys");
      
      Reply reply1 = dataBuilder.createSimpleReply(metaform, "test 1", ReplyMode.CUMULATIVE);
      Reply reply2 = dataBuilder.createSimpleReply(metaform, "test 2", ReplyMode.CUMULATIVE);
      assertNotNull(reply1.getOwnerKey());
      assertNotNull(reply2.getOwnerKey());
      assertNotEquals(reply1.getOwnerKey(), reply2.getOwnerKey());

      RepliesApi anonymousRepliesApi = getRepliesApi(getAnonymousToken(REALM_1));
      anonymousRepliesApi.updateReply(metaform.getId(), reply1.getId(), reply1, reply1.getOwnerKey());

      assertReplyOwnerKeyUpdateForbidden(anonymousRepliesApi, metaform, reply1, null);
      assertReplyOwnerKeyUpdateForbidden(anonymousRepliesApi, metaform, reply1, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgZEinFR6yjNnV4utVbvU9KQ8lZasbAWKQJXjp2VzcyQfE1WvH5dMJmI-sgmPyaZFcJLk9tgLfZilytGmeopfafkWp7yVa9zWYAfni0eJmCL9EcemRb_rDNw07Mf1Vb1lbLhLth8r6a2SVGk_dK14TXeGQSX9zROmiqjeT_FXe2Yz8EnvUweLQ5TobVE-azzyW0dqzjoSkBNfZ8r2hot4hQ2mQthU5xaAnOfDeCc95E7cci4-Dnx3B8U_UaHOn7Srf6DdsL_ZDvnSh1CvYiGNOYMpk-TLK6ixH-m83rweBjQ1hl1N_5I3cplmuJGCJLBxDauyjfBYIfR9WkBoau1eDQIDAQAB");
      assertReplyOwnerKeyUpdateForbidden(anonymousRepliesApi, metaform, reply1, reply2.getOwnerKey());
    } finally {
      dataBuilder.clean();
    }
  }

  @Test
  public void testDeleteReplyOwnerKeys() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      Metaform metaform = dataBuilder.createMetaform("simple-owner-keys");
      
      Reply reply1 = dataBuilder.createSimpleReply(metaform, "test 1", ReplyMode.CUMULATIVE);
      Reply reply2 = dataBuilder.createSimpleReply(metaform, "test 2", ReplyMode.CUMULATIVE);
      assertNotNull(reply1.getOwnerKey());
      assertNotNull(reply2.getOwnerKey());
      assertNotEquals(reply1.getOwnerKey(), reply2.getOwnerKey());

      RepliesApi anonymousRepliesApi = getRepliesApi(getAnonymousToken(REALM_1));
      
      assertReplyOwnerKeyDeleteForbidden(anonymousRepliesApi, metaform, reply1, null);
      assertReplyOwnerKeyDeleteForbidden(anonymousRepliesApi, metaform, reply1, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgZEinFR6yjNnV4utVbvU9KQ8lZasbAWKQJXjp2VzcyQfE1WvH5dMJmI-sgmPyaZFcJLk9tgLfZilytGmeopfafkWp7yVa9zWYAfni0eJmCL9EcemRb_rDNw07Mf1Vb1lbLhLth8r6a2SVGk_dK14TXeGQSX9zROmiqjeT_FXe2Yz8EnvUweLQ5TobVE-azzyW0dqzjoSkBNfZ8r2hot4hQ2mQthU5xaAnOfDeCc95E7cci4-Dnx3B8U_UaHOn7Srf6DdsL_ZDvnSh1CvYiGNOYMpk-TLK6ixH-m83rweBjQ1hl1N_5I3cplmuJGCJLBxDauyjfBYIfR9WkBoau1eDQIDAQAB");
      assertReplyOwnerKeyDeleteForbidden(anonymousRepliesApi, metaform, reply1, reply2.getOwnerKey());
      
      anonymousRepliesApi.deleteReply(metaform.getId(), reply1.getId(), reply1.getOwnerKey());

      dataBuilder.removeReply(reply1);
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
      adminMetaformsApi.updateMetaform(metaform.getId(), metaform);
      Reply reply = dataBuilder.createSimpleReply(metaform, "test 1", ReplyMode.UPDATE);

      given()
        .baseUri(getBasePath())
        .header("Authorization", String.format("Bearer %s", getAdminToken(REALM_1)))
        .get("/v1/metaforms/{metaformId}/replies/{replyId}/export?format=PDF", metaform.getId().toString(), reply.getId().toString())
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
      adminMetaformsApi.updateMetaform(metaform.getId(), metaform);
      Reply reply = dataBuilder.createSimpleReply(metaform, "test 1", ReplyMode.UPDATE);

      given()
        .baseUri(getBasePath())
        .header("Authorization", String.format("Bearer %s", getAdminToken(REALM_1)))
        .get("/v1/metaforms/{metaformId}/replies/{replyId}/export?format=PDF", metaform.getId().toString(), reply.getId().toString())
        .then()
        .assertThat()
        .statusCode(200)
        .header("Content-Type", "application/pdf");   
    } finally {
      dataBuilder.clean();
    }
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

  /**
   * Asserts reply can not be found with given owner key
   * 
   * @param anonymousRepliesApi replies API instance
   * @param metaform metaform
   * @param reply reply
   * @param ownerKey owner key
   */
  private void assertReplyOwnerKeyFindForbidden(RepliesApi anonymousRepliesApi, Metaform metaform, Reply reply, String ownerKey) {
    try {
      anonymousRepliesApi.findReply(metaform.getId(), reply.getId(), ownerKey);
      fail(String.format("Should not be able to find reply %s", reply.getId().toString()));
    } catch (FeignException e) {
      assertEquals(403, e.status());
    }
  }

  /**
   * Asserts reply can not be updated with given owner key
   * 
   * @param anonymousRepliesApi replies API instance
   * @param metaform metaform
   * @param reply reply
   * @param ownerKey owner key
   */
  private void assertReplyOwnerKeyUpdateForbidden(RepliesApi anonymousRepliesApi, Metaform metaform, Reply reply, String ownerKey) {
    try {
      anonymousRepliesApi.updateReply(metaform.getId(), reply.getId(), reply, ownerKey);
      fail(String.format("Should not be able to update reply %s", reply.getId().toString()));
    } catch (FeignException e) {
      assertEquals(403, e.status());
    }
  }

  /**
   * Asserts reply can not be deleted with given owner key
   * 
   * @param anonymousRepliesApi replies API instance
   * @param metaform metaform
   * @param reply reply
   * @param ownerKey owner key
   */
  private void assertReplyOwnerKeyDeleteForbidden(RepliesApi anonymousRepliesApi, Metaform metaform, Reply reply, String ownerKey) {
    try {
      anonymousRepliesApi.deleteReply(metaform.getId(), reply.getId(), ownerKey);
      fail(String.format("Should not be able to update reply %s", reply.getId().toString()));
    } catch (FeignException e) {
      assertEquals(403, e.status());
    }
  }
  
}
