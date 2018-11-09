package fi.metatavu.metaform.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import feign.FeignException;
import fi.metatavu.metaform.client.Metaform;
import fi.metatavu.metaform.client.MetaformsApi;
import fi.metatavu.metaform.client.RepliesApi;
import fi.metatavu.metaform.client.Reply;
import fi.metatavu.metaform.client.ReplyData;
import fi.metatavu.metaform.server.rest.ReplyMode;

@SuppressWarnings ("squid:S1192")
public class ReplyPermissionTestsIT extends AbstractIntegrationTest {
  
  /**
   * Test that asserts that user may find his / her own reply
   */
  @Test
  public void findOwnReply() throws IOException, URISyntaxException {
    String adminToken = getAdminToken(REALM_1);
    String accessToken = getAccessToken(REALM_1, "test1.realm1", "test");
    MetaformsApi adminMetaformsApi = getMetaformsApi(adminToken);
    RepliesApi repliesApi = getRepliesApi(accessToken);
    RepliesApi adminRepliesApi = getRepliesApi(adminToken);
    
    Metaform metaform = adminMetaformsApi.createMetaform(REALM_1, readMetaform("simple-permission-context"));
    try {
      Reply createdReply = repliesApi.createReply(REALM_1, metaform.getId(), createPermisionSelectReply("group-1"), null, ReplyMode.REVISION.toString());
      try {
        Reply foundReply = repliesApi.findReply(REALM_1, metaform.getId(), createdReply.getId());
        assertNotNull(foundReply);
      } finally {
        adminRepliesApi.deleteReply(REALM_1, metaform.getId(), createdReply.getId());
      }
    } finally {
      adminMetaformsApi.deleteMetaform(REALM_1, metaform.getId());
    }
  }
  
  /**
   * Test that asserts that anonymous users may not find their "own" replies
   */
  @Test
  public void findOwnReplyAnonymous() throws IOException, URISyntaxException {
    String adminToken = getAdminToken(REALM_1);
    String anonymousToken = getAnonymousToken(REALM_1);
    
    MetaformsApi adminMetaformsApi = getMetaformsApi(adminToken);
    RepliesApi anonRepliesApi = getRepliesApi(anonymousToken);
    RepliesApi adminRepliesApi = getRepliesApi(adminToken);
    
    Metaform metaform = adminMetaformsApi.createMetaform(REALM_1, readMetaform("simple-permission-context-anon"));
    try {
      Reply createdReply = anonRepliesApi.createReply(REALM_1, metaform.getId(), createPermisionSelectReply("group-1"), null, ReplyMode.REVISION.toString());
      try {
        assertForbiddenToFindReply(anonymousToken, REALM_1, metaform.getId(), createdReply.getId());
      } finally {
        adminRepliesApi.deleteReply(REALM_1, metaform.getId(), createdReply.getId());
      }
    } finally {
      adminMetaformsApi.deleteMetaform(REALM_1, metaform.getId());
    }
  }
  
  /**
   * Test that asserts that other users may not find their replies
   */
  @Test
  public void findOthersReplyUser() throws IOException, URISyntaxException {
    String adminToken = getAdminToken(REALM_1);
    String accessToken1 = getAccessToken(REALM_1, "test1.realm1", "test");
    String accessToken2 = getAccessToken(REALM_1, "test2.realm1", "test");

    MetaformsApi adminMetaformsApi = getMetaformsApi(adminToken);
    RepliesApi adminRepliesApi = getRepliesApi(adminToken);

    RepliesApi repliesApi = getRepliesApi(accessToken1);

    Metaform metaform = adminMetaformsApi.createMetaform(REALM_1, readMetaform("simple-permission-context"));
    try {
      Reply createdReply = repliesApi.createReply(REALM_1, metaform.getId(), createPermisionSelectReply("group-1"), null, ReplyMode.REVISION.toString());
      try {
        assertForbiddenToFindReply(accessToken2, REALM_1, metaform.getId(), createdReply.getId());
      } finally {
        adminRepliesApi.deleteReply(REALM_1, metaform.getId(), createdReply.getId());
      }
    } finally {
      adminMetaformsApi.deleteMetaform(REALM_1, metaform.getId());
    }
  }

  /**
   * Test that asserts that metaform-admin may find replies created by others
   */
  @Test
  public void findOthersReplyAdmin() throws IOException, URISyntaxException {
    String adminToken = getAdminToken(REALM_1);
    String accessToken = getAccessToken(REALM_1, "test1.realm1", "test");
    MetaformsApi adminMetaformsApi = getMetaformsApi(adminToken);
    RepliesApi repliesApi = getRepliesApi(accessToken);
    RepliesApi adminRepliesApi = getRepliesApi(adminToken);
    
    Metaform metaform = adminMetaformsApi.createMetaform(REALM_1, readMetaform("simple-permission-context"));
    try {
      Reply createdReply = repliesApi.createReply(REALM_1, metaform.getId(), createPermisionSelectReply("group-1"), null, ReplyMode.REVISION.toString());
      try {
        Reply foundReply = adminRepliesApi.findReply(REALM_1, metaform.getId(), createdReply.getId());
        assertNotNull(foundReply);
      } finally {
        adminRepliesApi.deleteReply(REALM_1, metaform.getId(), createdReply.getId());
      }
    } finally {
      adminMetaformsApi.deleteMetaform(REALM_1, metaform.getId());
    }
  }

  /**
   * Test that asserts that user may list only his / her own replies
   */
  @Test
  public void listOwnReplies() throws IOException, URISyntaxException {
    String adminToken = getAdminToken(REALM_1);
    
    String accessToken1 = getAccessToken(REALM_1, "test1.realm1", "test");
    String accessToken2 = getAccessToken(REALM_1, "test2.realm1", "test");
    String accessToken3 = getAccessToken(REALM_1, "test3.realm1", "test");
    
    MetaformsApi adminMetaformsApi = getMetaformsApi(adminToken);
    RepliesApi adminRepliesApi = getRepliesApi(adminToken);
    RepliesApi repliesApi1 = getRepliesApi(accessToken1);
    RepliesApi repliesApi2 = getRepliesApi(accessToken2);
    RepliesApi repliesApi3 = getRepliesApi(accessToken3);
    
    Metaform metaform = adminMetaformsApi.createMetaform(REALM_1, readMetaform("simple-permission-context"));
    try {
      Reply reply1 = repliesApi1.createReply(REALM_1, metaform.getId(), createPermisionSelectReply("group-1"), null, ReplyMode.REVISION.toString());
      Reply reply2 = repliesApi2.createReply(REALM_1, metaform.getId(), createPermisionSelectReply("group-2"), null, ReplyMode.REVISION.toString());
      Reply reply3 = repliesApi3.createReply(REALM_1, metaform.getId(), createPermisionSelectReply("group-3"), null, ReplyMode.REVISION.toString());
      
      try {
        List<Reply> replies = repliesApi1.listReplies(REALM_1, metaform.getId(), Collections.emptyMap());
        assertEquals(replies.size(), 1);
        assertEquals(replies.get(0).getId(), reply1.getId());
      } finally {
        adminRepliesApi.deleteReply(REALM_1, metaform.getId(), reply1.getId());
        adminRepliesApi.deleteReply(REALM_1, metaform.getId(), reply2.getId());
        adminRepliesApi.deleteReply(REALM_1, metaform.getId(), reply3.getId());
      }
      
    } finally {
      adminMetaformsApi.deleteMetaform(REALM_1, metaform.getId());
    }
  }
  
  /**
   * Test that asserts that user in permission context group may see replies targeted to that group
   */
  @Test
  public void listPermissionContextReplies() throws IOException, URISyntaxException {
    String adminToken = getAdminToken(REALM_1);
    
    String accessToken1 = getAccessToken(REALM_1, "test1.realm1", "test");
    String accessToken2 = getAccessToken(REALM_1, "test2.realm1", "test");
    String accessToken3 = getAccessToken(REALM_1, "test3.realm1", "test");
    
    MetaformsApi adminMetaformsApi = getMetaformsApi(adminToken);
    RepliesApi adminRepliesApi = getRepliesApi(adminToken);
    RepliesApi repliesApi1 = getRepliesApi(accessToken1);
    RepliesApi repliesApi2 = getRepliesApi(accessToken2);
    RepliesApi repliesApi3 = getRepliesApi(accessToken3);
    
    Metaform metaform = adminMetaformsApi.createMetaform(REALM_1, readMetaform("simple-permission-context"));
    try {
      Reply reply1 = repliesApi1.createReply(REALM_1, metaform.getId(), createPermisionSelectReply("group-2"), null, ReplyMode.REVISION.toString());
      Reply reply2 = repliesApi2.createReply(REALM_1, metaform.getId(), createPermisionSelectReply("group-2"), null, ReplyMode.REVISION.toString());
      Reply reply3 = repliesApi3.createReply(REALM_1, metaform.getId(), createPermisionSelectReply("group-2"), null, ReplyMode.REVISION.toString());
      
      try {
        List<Reply> replies1 = repliesApi3.listReplies(REALM_1, metaform.getId(), Collections.emptyMap());
        List<Reply> replies2 = repliesApi3.listReplies(REALM_1, metaform.getId(), Collections.emptyMap());
        List<Reply> replies3 = repliesApi3.listReplies(REALM_1, metaform.getId(), Collections.emptyMap());
        
        assertEquals(replies1.size(), 1);
        assertEquals(replies1.get(0).getId(), reply1.getId());

        assertEquals(replies2.size(), 3);
        assertEquals(replies2.get(0).getId(), reply1.getId());
        assertEquals(replies2.get(1).getId(), reply1.getId());
        assertEquals(replies2.get(2).getId(), reply1.getId());

        assertEquals(replies3.size(), 0);
      } finally {
        adminRepliesApi.deleteReply(REALM_1, metaform.getId(), reply1.getId());
        adminRepliesApi.deleteReply(REALM_1, metaform.getId(), reply2.getId());
        adminRepliesApi.deleteReply(REALM_1, metaform.getId(), reply3.getId());
      }
      
    } finally {
      adminMetaformsApi.deleteMetaform(REALM_1, metaform.getId());
    }
  }
  
  /**
   * Test that asserts that admin may list all replies
   */
  @Test
  public void listRepliesAdmin() throws IOException, URISyntaxException {
    String adminToken = getAdminToken(REALM_1);
    
    String accessToken1 = getAccessToken(REALM_1, "test1.realm1", "test");
    String accessToken2 = getAccessToken(REALM_1, "test2.realm1", "test");
    String accessToken3 = getAccessToken(REALM_1, "test3.realm1", "test");
    
    MetaformsApi adminMetaformsApi = getMetaformsApi(adminToken);
    RepliesApi adminRepliesApi = getRepliesApi(adminToken);
    RepliesApi repliesApi1 = getRepliesApi(accessToken1);
    RepliesApi repliesApi2 = getRepliesApi(accessToken2);
    RepliesApi repliesApi3 = getRepliesApi(accessToken3);
    
    Metaform metaform = adminMetaformsApi.createMetaform(REALM_1, readMetaform("simple-permission-context"));
    try {
      Reply reply1 = repliesApi1.createReply(REALM_1, metaform.getId(), createPermisionSelectReply("group-2"), null, ReplyMode.REVISION.toString());
      Reply reply2 = repliesApi2.createReply(REALM_1, metaform.getId(), createPermisionSelectReply("group-2"), null, ReplyMode.REVISION.toString());
      Reply reply3 = repliesApi3.createReply(REALM_1, metaform.getId(), createPermisionSelectReply("group-2"), null, ReplyMode.REVISION.toString());
      
      try {
        List<Reply> replies = adminRepliesApi.listReplies(REALM_1, metaform.getId(), Collections.emptyMap());
        assertEquals(replies.size(), 3);
      } finally {
        adminRepliesApi.deleteReply(REALM_1, metaform.getId(), reply1.getId());
        adminRepliesApi.deleteReply(REALM_1, metaform.getId(), reply2.getId());
        adminRepliesApi.deleteReply(REALM_1, metaform.getId(), reply3.getId());
      }
      
    } finally {
      adminMetaformsApi.deleteMetaform(REALM_1, metaform.getId());
    }
  }
  
  /**
   * Creates permission select reply with given value
   * 
   * @param value value
   * @return permission select reply with given value
   */
  private Reply createPermisionSelectReply(String value) {
    ReplyData replyData = new ReplyData();
    replyData.put("permission-select", value);
    Reply reply = createReplyWithData(replyData);
    return reply;
  }
  
  /**
   * Asserts that reply is forbidden to find
   * 
   * @param token token
   * @param realmId realm
   * @param metaformId metaform
   * @param replyId replay
   */
  private void assertForbiddenToFindReply(String token, String realmId, UUID metaformId, UUID replyId) {
    RepliesApi repliesApi = getRepliesApi(token);
    try {
      repliesApi.findReply(realmId, metaformId, replyId);
      fail(String.format("Reply %s should not be accessible", replyId.toString()));
    } catch (FeignException e) {
      assertEquals(403, e.status());
    }
  }
  
}
