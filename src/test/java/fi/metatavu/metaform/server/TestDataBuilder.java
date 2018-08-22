package fi.metatavu.metaform.server;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import fi.metatavu.metaform.client.AttachmentsApi;
import fi.metatavu.metaform.client.EmailNotification;
import fi.metatavu.metaform.client.EmailNotificationsApi;
import fi.metatavu.metaform.client.Metaform;
import fi.metatavu.metaform.client.MetaformsApi;
import fi.metatavu.metaform.client.RepliesApi;
import fi.metatavu.metaform.client.Reply;
import fi.metatavu.metaform.client.ReplyData;
import fi.metatavu.metaform.server.rest.ReplyMode;

/**
 * Builder for integration test data
 * 
 * @author Antti Leppä
 */
public class TestDataBuilder {

  private AbstractIntegrationTest test;
  private String realm;
  private String username;
  private String password;
  private String adminToken;
  private String accessToken;
  private Set<EmailNotification> emailNotifications;
  private Set<Metaform> metaforms;
  private Set<Reply> replies;
  private Map<UUID, UUID> childEntityMetaforms;
  
  /**
   * Constructor
   * 
   * @param test test class
   * @param realm realm
   * @param username user name for test user
   * @param password password for test user
   */
  public TestDataBuilder(AbstractIntegrationTest test, String realm, String username, String password) {
    this.test = test;
    this.realm = realm;
    this.username = username;
    this.password = password;
    this.emailNotifications = new HashSet<>();
    this.metaforms = new HashSet<>();
    this.replies = new HashSet<>();
    this.childEntityMetaforms = new HashMap<>();
  }
  
  /**
   * Returns initialized replies API
   * 
   * @return initialized replies API
   * @throws IOException
   */
  public RepliesApi getRepliesApi() throws IOException {
    return test.getRepliesApi(getAccessToken());
  }
  
  /**
   * Returns initialized replies API with administrative rights
   * 
   * @return initialized replies API with administrative rights
   * @throws IOException
   */
  public RepliesApi getAdminRepliesApi() throws IOException {
    return test.getRepliesApi(getAdminToken());
  }
  
  /**
   * Returns initialized metaforms API with administrative rights
   * 
   * @return initialized metaforms API with administrative rights
   * @throws IOException
   */
  public MetaformsApi getAdminMetaformsApi() throws IOException {
    return test.getMetaformsApi(getAdminToken());
  }
  
  /**
   * Returns initialized attachment API with administrative rights
   * 
   * @return initialized attachment API with administrative rights
   * @throws IOException
   */
  public AttachmentsApi getAdminAttachmentsApi() throws IOException {
    return test.getAttachmentsApi(getAdminToken());
  }
  
  /**
   * Returns initialized email notifications API with administrative rights
   * 
   * @return initialized email notifications API with administrative rights
   * @throws IOException
   */
  public EmailNotificationsApi getAdminEmailNotificationsApi() throws IOException {
    return test.getEmailNotificationsApi(getAdminToken());
  }

  /**
   * Creates new Metaform from JSON template
   * 
   * @param form JSON template file name
   * @return Metaform
   * @throws IOException
   */
  public Metaform createMetaform(String form) throws IOException {
    Metaform metaform = getAdminMetaformsApi().createMetaform(realm, test.readMetaform(form));
    return addMetaform(metaform);
  }
  
  /**
   * Creates new email notification for a Metaform
   * 
   * @param metaform metaform 
   * @param subjectTemplate freemarker template for subject
   * @param contentTemplate freemarker template for content
   * @param emails email addresses
   * @return email notification
   * @throws IOException
   */
  public EmailNotification createEmailNotification(Metaform metaform, String subjectTemplate, String contentTemplate, List<String> emails) throws IOException {
    EmailNotification notification = new EmailNotification();
    notification.setContentTemplate(contentTemplate);
    notification.setSubjectTemplate(subjectTemplate);
    notification.setEmails(emails);
    EmailNotification emailNotification = getAdminEmailNotificationsApi().createEmailNotification(realm, metaform.getId(), notification);
    return addEmailNotifications(metaform, emailNotification);
  }
  
  /**
   * Creates new reply for TBNC form
   * 
   * @param metaform metaform
   * @param text text value
   * @param bool boolean value
   * @param number number value
   * @param checklist checklist value
   * @return reply
   * @throws IOException
   */
  public Reply createTBNCReply(Metaform metaform, String text, Boolean bool, double number, String[] checklist) throws IOException {
    ReplyData replyData = new ReplyData();
    replyData.put("text", text);
    replyData.put("boolean", bool);
    replyData.put("number", number);
    replyData.put("checklist", checklist);
    Reply reply = createReplyWithData(replyData);
    return addReply(metaform, getRepliesApi().createReply(realm, metaform.getId(), reply, null, ReplyMode.REVISION.toString()));
  }
  
  /**
   * Creates new reply for the simple form
   * 
   * @param metaform metaform
   * @param value value
   * @return reply
   * @throws IOException
   */
  public Reply createSimpleReply(Metaform metaform, String value) throws IOException {
    return createSimpleReply(metaform, value, ReplyMode.REVISION);
  }

  /**
   * Creates new reply for the simple form
   * 
   * @param metaform metaform
   * @param value value
   * @param replyMode reply model
   * @return reply
   * @throws IOException
   */
  public Reply createSimpleReply(Metaform metaform, String value, ReplyMode replyMode) throws IOException {
    ReplyData replyData1 = new ReplyData();
    replyData1.put("text", value);
    Reply reply = createReplyWithData(replyData1);
    return addReply(metaform, getRepliesApi().createReply(realm, metaform.getId(), reply, null, replyMode.toString()));
  }
  
  /**
   * Cleans created test data
   */
  public void clean() {
    replies.stream().forEach((reply) -> {
      UUID metaformId = childEntityMetaforms.get(reply.getId());
      try {
        getAdminRepliesApi().deleteReply(realm, metaformId, reply.getId());
      } catch (IOException e) {
        fail(e.getMessage());
      }
    });
    
    emailNotifications.stream().forEach((emailNotification) -> {
      UUID metaformId = childEntityMetaforms.get(emailNotification.getId());
      try {
        getAdminEmailNotificationsApi().deleteEmailNotification(realm, metaformId, emailNotification.getId());
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
  
  /**
   * Returns admin token
   * 
   * @return admin token
   * @throws IOException
   */
  private String getAdminToken() throws IOException {
    if (adminToken == null) {
      adminToken = test.getAdminToken(realm);
    }

    assertNotNull(adminToken);
    
    return adminToken;
  }

  /**
   * Returns user token
   * 
   * @return user token
   * @throws IOException
   */
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
  
  /**
   * Adds reply to clean queue
   * 
   * @param metaform metaform
   * @param reply reply
   * @return reply
   */
  private Reply addReply(Metaform metaform, Reply reply) {
    replies.add(reply);
    childEntityMetaforms.put(reply.getId(), metaform.getId());
    return reply;
  }

  /**
   * Adds metaform to clean queue
   * 
   * @param metaform metaform
   * @return metaform
   */
  private Metaform addMetaform(Metaform metaform) {
    metaforms.add(metaform);
    return metaform;
  }
  
  /**
   * Adds email notification to clean queue
   * 
   * @param metaform metaform
   * @param emailNotification email notification
   * @return email notification
   */
  private EmailNotification addEmailNotifications(Metaform metaform, EmailNotification emailNotification) {
    this.emailNotifications.add(emailNotification);
    childEntityMetaforms.put(emailNotification.getId(), metaform.getId());
    return emailNotification;
  }
  
}
