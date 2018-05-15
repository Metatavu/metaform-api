package fi.metatavu.metaform.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import static io.restassured.RestAssured.given;

import java.io.IOException;
import java.net.URISyntaxException;
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
        
        List<Reply> replies = repliesApi.listReplies(REALM_1, metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE);
        
        assertEquals(2, replies.size());
        assertNotNull(replies.get(1).getRevision());
        assertEquals("Test text value", replies.get(1).getData().get("text"));
        assertNull(replies.get(0).getRevision());
        assertEquals("Updated text value", replies.get(0).getData().get("text"));
      } finally {
        adminRepliesApi.deleteReply(REALM_1, metaform.getId(), createdReply1.getId());
      }
    } finally {
      adminMetaformsApi.deleteMetaform(REALM_1, metaform.getId());
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
