package fi.metatavu.metaform.server;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import fi.metatavu.metaform.client.Metaform;
import fi.metatavu.metaform.client.MetaformsApi;
import fi.metatavu.metaform.client.RepliesApi;
import fi.metatavu.metaform.client.Reply;
import fi.metatavu.metaform.client.ReplyData;
import fi.metatavu.metaform.server.rest.ReplyMode;

public class TestDataBuilder {

  private AbstractIntegrationTest test;
  private String realm;
  private String username;
  private String password;
  private String adminToken;
  private String accessToken;
  private Set<Metaform> metaforms;
  private Set<Reply> replies;
  private Map<UUID, UUID> replyMetaforms;
  
  public TestDataBuilder(AbstractIntegrationTest test, String realm, String username, String password) {
    this.test = test;
    this.realm = realm;
    this.username = username;
    this.password = password;
    this.metaforms = new HashSet<>();
    this.replies = new HashSet<>();
    this.replyMetaforms = new HashMap<>();
  }
  
  public RepliesApi getRepliesApi() throws IOException {
    return test.getRepliesApi(getAccessToken());
  }
  
  public RepliesApi getAdminRepliesApi() throws IOException {
    return test.getRepliesApi(getAdminToken());
  }
  
  public MetaformsApi getAdminMetaformsApi() throws IOException {
    return test.getMetaformsApi(getAdminToken());
  }

  public Metaform createMetaform(String form) throws IOException {
    Metaform metaform = getAdminMetaformsApi().createMetaform(realm, test.readMetaform(form));
    return addMetaform(metaform);
  }
  
  public Reply createTBNCReply(Metaform metaform, String text, Boolean bool, double number, String[] checklist) throws IOException {
    ReplyData replyData = new ReplyData();
    replyData.put("text", text);
    replyData.put("boolean", bool);
    replyData.put("number", number);
    replyData.put("checklist", checklist);
    Reply reply = createReplyWithData(replyData);
    return addReply(metaform, getRepliesApi().createReply(realm, metaform.getId(), reply, null, ReplyMode.REVISION.toString()));
  }
  
  public Reply createSimpleReply(Metaform metaform, String value) throws IOException {
    return createSimpleReply(metaform, value, ReplyMode.REVISION);
  }

  public Reply createSimpleReply(Metaform metaform, String value, ReplyMode replyMode) throws IOException {
    ReplyData replyData1 = new ReplyData();
    replyData1.put("text", value);
    Reply reply = createReplyWithData(replyData1);
    return addReply(metaform, getRepliesApi().createReply(realm, metaform.getId(), reply, null, replyMode.toString()));
  }
  
  public void clean() {
    replies.stream().forEach((reply) -> {
      UUID metaformId = replyMetaforms.get(reply.getId());
      try {
        getAdminRepliesApi().deleteReply(realm, metaformId, reply.getId());
      } catch (IOException e) {
        fail(e.getMessage());
      }
    });
    
    metaforms.stream().forEach((metaform) -> {
      try {
        getAdminMetaformsApi().deleteMetaform(realm, metaform.getId());
      } catch (IOException e) {
        fail(e.getMessage());
      }  
    });
  }
  
  private String getAdminToken() throws IOException {
    if (adminToken == null) {
      adminToken = test.getAdminToken(realm);
    }
    
    return adminToken;
  }

  private String getAccessToken() throws IOException {
    if (accessToken == null) {
      accessToken = test.getAccessToken(realm, username, password);
    }
    
    return accessToken;
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
  
  private Reply addReply(Metaform metaform, Reply reply) {
    replies.add(reply);
    replyMetaforms.put(reply.getId(), metaform.getId());
    return reply;
  }

  private Metaform addMetaform(Metaform metaform) {
    metaforms.add(metaform);
    return metaform;
  }
  
}
