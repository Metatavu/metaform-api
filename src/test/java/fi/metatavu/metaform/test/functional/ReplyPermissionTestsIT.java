package fi.metatavu.metaform.test.functional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Test;

import feign.FeignException;
import fi.metatavu.metaform.client.model.Metaform;
import fi.metatavu.metaform.client.api.MetaformsApi;
import fi.metatavu.metaform.client.api.RepliesApi;
import fi.metatavu.metaform.client.model.Reply;
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
    
    Metaform metaform = adminMetaformsApi.createMetaform(readMetaform("simple-permission-context"));
    try {
      Reply createdReply = repliesApi.createReply(metaform.getId(), createPermisionSelectReply("group-1"), null, ReplyMode.REVISION.toString());
      try {
        Reply foundReply = repliesApi.findReply(metaform.getId(), createdReply.getId(), (String) null);
        assertNotNull(foundReply);
      } finally {
        adminRepliesApi.deleteReply(metaform.getId(), createdReply.getId(), (String) null);
      }
    } finally {
      adminMetaformsApi.deleteMetaform(metaform.getId());
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
    
    Metaform metaform = adminMetaformsApi.createMetaform(readMetaform("simple-permission-context-anon"));
    try {
      Reply createdReply = anonRepliesApi.createReply(metaform.getId(), createPermisionSelectReply("group-1"), null, ReplyMode.REVISION.toString());
      try {
        assertForbiddenToFindReply(anonymousToken, REALM_1, metaform.getId(), createdReply.getId());
      } finally {
        adminRepliesApi.deleteReply(metaform.getId(), createdReply.getId(), (String) null);
      }
    } finally {
      adminMetaformsApi.deleteMetaform(metaform.getId());
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

    Metaform metaform = adminMetaformsApi.createMetaform(readMetaform("simple-permission-context"));
    try {
      Reply createdReply = repliesApi.createReply(metaform.getId(), createPermisionSelectReply("group-1"), null, ReplyMode.REVISION.toString());
      try {
        assertForbiddenToFindReply(accessToken2, REALM_1, metaform.getId(), createdReply.getId());
      } finally {
        adminRepliesApi.deleteReply(metaform.getId(), createdReply.getId(), (String) null);
      }
    } finally {
      adminMetaformsApi.deleteMetaform(metaform.getId());
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
    
    Metaform metaform = adminMetaformsApi.createMetaform(readMetaform("simple-permission-context"));
    try {
      Reply createdReply = repliesApi.createReply(metaform.getId(), createPermisionSelectReply("group-1"), null, ReplyMode.REVISION.toString());
      try {
        Reply foundReply = adminRepliesApi.findReply(metaform.getId(), createdReply.getId(), (String) null);
        assertNotNull(foundReply);
      } finally {
        adminRepliesApi.deleteReply(metaform.getId(), createdReply.getId(), (String) null);
      }
    } finally {
      adminMetaformsApi.deleteMetaform(metaform.getId());
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
    
    Metaform metaform = adminMetaformsApi.createMetaform(readMetaform("simple-permission-context"));
    try {
      Reply reply1 = repliesApi1.createReply(metaform.getId(), createPermisionSelectReply("group-1"), null, ReplyMode.REVISION.toString());
      Reply reply2 = repliesApi2.createReply(metaform.getId(), createPermisionSelectReply("group-2"), null, ReplyMode.REVISION.toString());
      Reply reply3 = repliesApi3.createReply(metaform.getId(), createPermisionSelectReply("group-3"), null, ReplyMode.REVISION.toString());
      
      try {
        List<Reply> replies = repliesApi1.listReplies(metaform.getId(), Collections.emptyMap());
        assertEquals(1, replies.size());
        assertEquals(reply1.getId(), replies.get(0).getId());
      } finally {
        adminRepliesApi.deleteReply(metaform.getId(), reply1.getId(), (String) null);
        adminRepliesApi.deleteReply(metaform.getId(), reply2.getId(), (String) null);
        adminRepliesApi.deleteReply(metaform.getId(), reply3.getId(), (String) null);
      }
      
    } finally {
      adminMetaformsApi.deleteMetaform(metaform.getId());
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
    
    Metaform metaform = adminMetaformsApi.createMetaform(readMetaform("simple-permission-context"));
    try {
      Reply reply1 = repliesApi1.createReply(metaform.getId(), createPermisionSelectReply("group-2"), null, ReplyMode.REVISION.toString());
      Reply reply2 = repliesApi2.createReply(metaform.getId(), createPermisionSelectReply("group-2"), null, ReplyMode.REVISION.toString());
      Reply reply3 = repliesApi3.createReply(metaform.getId(), createPermisionSelectReply("group-2"), null, ReplyMode.REVISION.toString());
      
      try {
        List<Reply> replies1 = repliesApi1.listReplies(metaform.getId(), Collections.emptyMap());
        List<Reply> replies2 = repliesApi2.listReplies(metaform.getId(), Collections.emptyMap());
        List<Reply> replies3 = repliesApi3.listReplies(metaform.getId(), Collections.emptyMap());
        
        assertEquals(1, replies1.size());
        assertEquals(reply1.getId(), replies1.get(0).getId());

        assertEquals(3, replies2.size());
        
        Set<UUID> reply2Ids = replies2.stream().map(Reply::getId).collect(Collectors.toSet());
        assertTrue(reply2Ids.contains(reply1.getId()));
        assertTrue(reply2Ids.contains(reply2.getId()));
        assertTrue(reply2Ids.contains(reply3.getId()));

        assertEquals(1, replies3.size());
        assertEquals(reply3.getId(), replies3.get(0).getId());        
      } finally {
        adminRepliesApi.deleteReply(metaform.getId(), reply1.getId(), (String) null);
        adminRepliesApi.deleteReply(metaform.getId(), reply2.getId(), (String) null);
        adminRepliesApi.deleteReply(metaform.getId(), reply3.getId(), (String) null);
      }
      
    } finally {
      adminMetaformsApi.deleteMetaform(metaform.getId());
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
    
    Metaform metaform = adminMetaformsApi.createMetaform(readMetaform("simple-permission-context"));
    try {
      Reply reply1 = repliesApi1.createReply(metaform.getId(), createPermisionSelectReply("group-2"), null, ReplyMode.REVISION.toString());
      Reply reply2 = repliesApi2.createReply(metaform.getId(), createPermisionSelectReply("group-2"), null, ReplyMode.REVISION.toString());
      Reply reply3 = repliesApi3.createReply(metaform.getId(), createPermisionSelectReply("group-2"), null, ReplyMode.REVISION.toString());
      
      try {
        List<Reply> replies = adminRepliesApi.listReplies(metaform.getId(), Collections.emptyMap());
        assertEquals(3, replies.size());
      } finally {
        adminRepliesApi.deleteReply(metaform.getId(), reply1.getId(), (String) null);
        adminRepliesApi.deleteReply(metaform.getId(), reply2.getId(), (String) null);
        adminRepliesApi.deleteReply(metaform.getId(), reply3.getId(), (String) null);
      }
      
    } finally {
      adminMetaformsApi.deleteMetaform(metaform.getId());
    }
  }

  /**
   * Test that asserts that user in permission context receives an email when notification is posted and
   * another user receives when reply is updated
   */
  @Test
  public void notifyPermissionContextReply() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test3.realm1", "test");
    try {
      MailgunMocker mailgunMocker = startMailgunMocker();
      try {
        Metaform metaform = dataBuilder.createMetaform("simple-permission-context");
        
        dataBuilder.createEmailNotification(metaform, "Permission context subject", "Permission context content", Collections.emptyList());
        
        Reply createdReply = dataBuilder.createReply(metaform, createPermissionSelectReplyData("group-2"), ReplyMode.REVISION);
        dataBuilder.getRepliesApi().updateReply(metaform.getId(), createdReply.getId(), createPermisionSelectReply("group-1"), (String) null);
        dataBuilder.getRepliesApi().updateReply(metaform.getId(), createdReply.getId(), createPermisionSelectReply("group-1"), (String) null);
        
        mailgunMocker.verifyHtmlMessageSent(1, "Metaform Test", "metaform-test@example.com", "user1@example.com", "Permission context subject", "Permission context content");
        mailgunMocker.verifyHtmlMessageSent(1, "Metaform Test", "metaform-test@example.com", "user2@example.com", "Permission context subject", "Permission context content");
      } finally {
        stopMailgunMocker(mailgunMocker);
      }
    } finally {
      dataBuilder.clean();
    }
  }
  
  /**
   * Creates permission select reply with given value
   * 
   * @param value value
   * @return permission select reply with given value
   */
  private Reply createPermisionSelectReply(String value) {
    Map<String, Object> replyData = createPermissionSelectReplyData(value);
    Reply reply = createReplyWithData(replyData);
    return reply;
  }

  /**
   * Creates permission select reply data with given value
   * 
   * @param value value
   * @return permission select reply data with given value
   */
  private Map<String, Object> createPermissionSelectReplyData(String value) {
    Map<String, Object> replyData = new HashMap<>();
    replyData.put("permission-select", value);
    return replyData;
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
      repliesApi.findReply(metaformId, replyId, (String) null);
      fail(String.format("Reply %s should not be accessible", replyId.toString()));
    } catch (FeignException e) {
      assertEquals(403, e.status());
    }
  }
  
}
