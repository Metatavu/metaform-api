package fi.metatavu.metaform.test.functional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import fi.metatavu.metaform.client.api.AttachmentsApi;
import fi.metatavu.metaform.client.model.EmailNotification;
import fi.metatavu.metaform.client.api.EmailNotificationsApi;
import fi.metatavu.metaform.client.api.ExportThemeFilesApi;
import fi.metatavu.metaform.client.model.ExportTheme;
import fi.metatavu.metaform.client.model.ExportThemeFile;
import fi.metatavu.metaform.client.model.FieldRule;
import fi.metatavu.metaform.client.api.ExportThemesApi;
import fi.metatavu.metaform.client.model.Metaform;
import fi.metatavu.metaform.client.api.MetaformsApi;
import fi.metatavu.metaform.client.api.RepliesApi;
import fi.metatavu.metaform.client.model.Reply;
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
  private String superToken;
  private String adminToken;
  private String accessToken;
  private Set<EmailNotification> emailNotifications;
  private Set<Metaform> metaforms;
  private Set<ExportTheme> exportThemes;
  private Map<UUID, Set<ExportThemeFile>> exportThemeFiles;
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
    this.exportThemes = new HashSet<>();
    this.childEntityMetaforms = new HashMap<>();
    this.exportThemeFiles = new HashMap<>();
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
   * Returns initialized exportThemes API
   * 
   * @return initialized exportThemes API
   * @throws IOException
   */
  public ExportThemesApi getExportThemesApi() throws IOException {
    return test.getExportThemesApi(getAccessToken());
  }
  
  /**
   * Returns initialized exportThemes API with super rights
   * 
   * @return initialized exportThemes API with super rights
   * @throws IOException
   */
  public ExportThemesApi getSuperExportThemesApi() throws IOException {
    return test.getExportThemesApi(getSuperToken());
  }
  
  /**
   * Returns initialized exportThemeFiles API
   * 
   * @return initialized exportThemeFiles API
   * @throws IOException
   */
  public ExportThemeFilesApi getExportThemeFilesApi() throws IOException {
    return test.getExportThemeFilesApi(getAccessToken());
  }

  /**
   * Returns initialized exportThemeFiles API with super rights
   * 
   * @return initialized exportThemeFiles API with super rights
   * @throws IOException
   */
  public ExportThemeFilesApi getSuperExportThemeFilesApi() throws IOException {
    return test.getExportThemeFilesApi(getSuperToken());
  }
  
  /**
   * Creates new Metaform from JSON template
   * 
   * @param form JSON template file name
   * @return Metaform
   * @throws IOException
   */
  public Metaform createMetaform(String form) throws IOException {
    Metaform metaform = getAdminMetaformsApi().createMetaform(test.readMetaform(form));
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
    return createEmailNotification(metaform, subjectTemplate, contentTemplate, emails, null);
  }
  
  /**
   * Creates new email notification for a Metaform
   * 
   * @param metaform metaform 
   * @param subjectTemplate freemarker template for subject
   * @param contentTemplate freemarker template for content
   * @param emails email addresses
   * @param notifyIf notify if rule
   * @return email notification
   * @throws IOException
   */
  public EmailNotification createEmailNotification(Metaform metaform, String subjectTemplate, String contentTemplate, List<String> emails, FieldRule notifyIf) throws IOException {
    EmailNotification notification = new EmailNotification();
    notification.setContentTemplate(contentTemplate);
    notification.setSubjectTemplate(subjectTemplate);
    notification.setEmails(emails);
    notification.setNotifyIf(notifyIf);
    EmailNotification emailNotification = getAdminEmailNotificationsApi().createEmailNotification(metaform.getId(), notification);
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
    Map<String, Object> replyData = new HashMap<>();
    replyData.put("text", text);
    replyData.put("boolean", bool);
    replyData.put("number", number);
    replyData.put("checklist", checklist);
    Reply reply = createReplyWithData(replyData);
    return addReply(metaform, getRepliesApi().createReply(metaform.getId(), reply, null, ReplyMode.REVISION.toString()));
  }
  
  /**
   * Creates simple permission context reply
   * 
   * @param metaform metaform
   * @param permissionSelectValue permission select value
   * @return reply
   * @throws IOException
   */
  public Reply createSimplePermissionContextReply(Metaform metaform, String permissionSelectValue) throws IOException {
    Map<String, Object> replyData = new HashMap<>();
    replyData.put("permission-select", permissionSelectValue);
    Reply reply = createReplyWithData(replyData);
    return addReply(metaform, getRepliesApi().createReply(metaform.getId(), reply, null, ReplyMode.REVISION.toString()));
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
    Map<String, Object> replyData1 = new HashMap<>();
    replyData1.put("text", value);
    Reply reply = createReplyWithData(replyData1);
    return addReply(metaform, getRepliesApi().createReply(metaform.getId(), reply, null, replyMode.toString()));
  }

  /**
   * Creates a reply
   * 
   * @param metaform metaform
   * @param replyData reply data
   * @param replyMode reply mode
   * @return created reply
   * @throws IOException thrown when creation fails
   */
  public Reply createReply(Metaform metaform, Map<String, Object> replyData, ReplyMode replyMode) throws IOException {
    Reply reply = createReplyWithData(replyData);
    return addReply(metaform, getRepliesApi().createReply(metaform.getId(), reply, null, replyMode.toString()));
  }

  /**
   * Creates simple export theme
   * 
   * @return export theme
   * @throws IOException thrown when request fails
   */
  public ExportTheme createSimpleExportTheme() throws IOException {
    return createSimpleExportTheme("simple");
  }

  /**
   * Creates simple export theme
   * 
   * @param name name
   * @return export theme
   * @throws IOException thrown when request fails
   */
  public ExportTheme createSimpleExportTheme(String name) throws IOException {
    fi.metatavu.metaform.client.model.ExportTheme payload = new fi.metatavu.metaform.client.model.ExportTheme();
    payload.setName(name);
    return createExportTheme(payload);
  }

  /**
   * Creates export theme
   * 
   * @param payload payload
   * @return export theme
   * @throws IOException thrown when request fails
   */
  public ExportTheme createExportTheme(ExportTheme payload) throws IOException {
    return addExportTheme(getSuperExportThemesApi().createExportTheme(payload));
  }

  /**
   * Creates export theme file
   * 
   * @param themeId theme
   * @param path path
   * @param content content
   * @param payload payload
   * @return export theme
   * @throws IOException thrown when request fails
   */
  public ExportThemeFile createSimpleExportThemeFile(UUID themeId, String path, String content) throws IOException {
    ExportThemeFile payload = new ExportThemeFile();
    payload.setContent(content);
    payload.setPath(path);
    payload.setThemeId(themeId);
    
    return createExportThemeFile(payload);
  }

  /**
   * Creates export theme file
   * 
   * @param payload payload
   * @return export theme
   * @throws IOException thrown when request fails
   */
  public ExportThemeFile createExportThemeFile(ExportThemeFile payload) throws IOException {
    return addExportThemeFile(payload.getThemeId(), getSuperExportThemeFilesApi().createExportThemeFile(payload.getThemeId(), payload));
  }

  /**
   * Cleans created test data
   */
  public void clean() {
    replies.stream().forEach((reply) -> {
      UUID metaformId = childEntityMetaforms.get(reply.getId());
      try {
        getAdminRepliesApi().deleteReply(metaformId, reply.getId());
      } catch (IOException e) {
        fail(e.getMessage());
      }
    });
    
    emailNotifications.stream().forEach((emailNotification) -> {
      UUID metaformId = childEntityMetaforms.get(emailNotification.getId());
      try {
        getAdminEmailNotificationsApi().deleteEmailNotification(metaformId, emailNotification.getId());
      } catch (IOException e) {
        fail(e.getMessage());
      }  
    });
    
    metaforms.stream().forEach((metaform) -> {
      try {
        getAdminMetaformsApi().deleteMetaform(metaform.getId());
      } catch (IOException e) {
        fail(e.getMessage());
      }  
    });
    
    exportThemes.stream()
      .sorted((exportTheme1, exportTheme2) -> {
        UUID parent1 = exportTheme1.getParentId();
        UUID parent2 = exportTheme2.getParentId();
        
        if (parent1 == null && parent2 == null) {
          return 0;
        } else if (parent1 == null) {
          return 1;
        } else if (parent2 == null) {
          return -1;
        }
        
        return parent2.compareTo(parent1);
      })
      .forEach((exportTheme) -> {
        try {
          Set<ExportThemeFile> files = exportThemeFiles.get(exportTheme.getId());
          
          if (files != null) {
            files.stream().forEach((exportThemeFile) -> {
              try {
                getSuperExportThemeFilesApi().deleteExportThemeFile(exportTheme.getId(), exportThemeFile.getId());
              } catch (IOException e) {
                fail(e.getMessage());
              }
            });
          }
          
          getSuperExportThemesApi().deleteExportTheme(exportTheme.getId());
        } catch (IOException e) {
          fail(e.getMessage());
        }  
      });
  }

  
  /**
   * Adds an export theme file into clean queue
   * 
   * @param themeId themeId
   * @param themeFile payload
   * @return added theme file
   */
  private ExportThemeFile addExportThemeFile(UUID themeId, ExportThemeFile themeFile) {
    if (!exportThemeFiles.containsKey(themeId)) {
      exportThemeFiles.put(themeId, new HashSet<>());
    }
    
    exportThemeFiles.get(themeId).add(themeFile);
    
    return themeFile;
  }
  
  /**
   * Returns super token
   * 
   * @return super token
   * @throws IOException
   */
  private String getSuperToken() throws IOException {
    if (superToken == null) {
      superToken = test.getSuperToken(realm);
    }

    assertNotNull(superToken);
    
    return superToken;
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
  private Reply createReplyWithData(Map<String, Object> replyData) {
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

  /**
   * Adds exportTheme to clean queue
   * 
   * @param exportTheme exportTheme
   * @return exportTheme
   */
  private ExportTheme addExportTheme(ExportTheme exportTheme) {
    exportThemes.add(exportTheme);
    return exportTheme;
  }
  
}
