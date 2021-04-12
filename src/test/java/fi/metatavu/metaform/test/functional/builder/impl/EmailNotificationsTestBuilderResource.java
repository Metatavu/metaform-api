package fi.metatavu.metaform.test.functional.builder.impl;

import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder;
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider;
import fi.metatavu.metaform.api.client.apis.EmailNotificationsApi;
import fi.metatavu.metaform.api.client.infrastructure.ApiClient;
import fi.metatavu.metaform.api.client.models.EmailNotification;
import fi.metatavu.metaform.api.client.models.FieldRule;
import fi.metatavu.metaform.api.client.models.Metaform;
import fi.metatavu.metaform.test.TestSettings;

import java.io.IOException;
import java.util.*;

/**
 * Test builder resource for EmailNotifications API
 */
public class EmailNotificationsTestBuilderResource extends ApiTestBuilderResource<EmailNotification, EmailNotificationsApi> {

  private final Map<UUID, UUID> emailNotificationMetaforms = new HashMap<UUID, UUID>();
  private final AccessTokenProvider accessTokenProvider;
  /**
   * Constructor
   *
   * @param testBuilder test builder
   * @param apiClient   initialized API client
   */
  public EmailNotificationsTestBuilderResource(AbstractTestBuilder<ApiClient> testBuilder, AccessTokenProvider accessTokenProvider, ApiClient apiClient) {
    super(testBuilder, apiClient);
    this.accessTokenProvider = accessTokenProvider;
  }

  /**
   * Creates new email notification for a Metaform
   *
   * @param metaform metaform
   * @param subjectTemplate freemarker template for subject
   * @param contentTemplate freemarker template for content
   * @param emails email addresses
   * @return email notification
   */
  public EmailNotification createEmailNotification(Metaform metaform, String subjectTemplate, String contentTemplate, List<String> emails) {
    return createEmailNotification(metaform.getId(), subjectTemplate, contentTemplate, emails, null);
  }

  /**
   * Creates new email notification for a Metaform
   *
   * @param metaformId metaform id
   * @param subjectTemplate freemarker template for subject
   * @param contentTemplate freemarker template for content
   * @param emails email addresses
   * @param notifyIf notify if rule
   * @return email notification
   */
  public EmailNotification createEmailNotification(UUID metaformId, String subjectTemplate, String contentTemplate, List<String> emails, FieldRule notifyIf) {
    EmailNotification notification = new EmailNotification(subjectTemplate, contentTemplate, emails.toArray(String[]::new), null, notifyIf);
    EmailNotification createdNotification = getApi().createEmailNotification(metaformId, notification);
    emailNotificationMetaforms.put(createdNotification.getId(), metaformId);
    return addClosable(createdNotification);
  }

  @Override
  protected EmailNotificationsApi getApi() {
    try {
      ApiClient.Companion.setAccessToken(accessTokenProvider.getAccessToken());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return new EmailNotificationsApi(TestSettings.basePath);
  }

  @Override
  public void clean(EmailNotification emailNotification){
    UUID metaformId = emailNotificationMetaforms.get(emailNotification.getId());
    getApi().deleteEmailNotification(metaformId, emailNotification.getId());
  }

  /**
   * Finds email notification
   *
   * @param metaformId metaform id
   * @param emailNotId notification id
   * @return found email notification
   */
  public EmailNotification findEmailNotification(UUID metaformId, UUID emailNotId) {
    return getApi().findEmailNotification(metaformId, emailNotId);
  }

  /**
   * Returns all email notifications for metaform
   *
   * @param metaformId metaform id
   * @return found notifications
   */
  public List<EmailNotification> listEmailNotifications(UUID metaformId) {
    return Arrays.asList(getApi().listEmailNotifications(metaformId));
  }
}
