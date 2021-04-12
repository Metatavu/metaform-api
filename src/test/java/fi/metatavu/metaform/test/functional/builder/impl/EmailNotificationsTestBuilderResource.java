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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Test builder resource for EmailNotifications API
 */
public class EmailNotificationsTestBuilderResource extends ApiTestBuilderResource<EmailNotification, EmailNotificationsApi> {

  private final Map<EmailNotification, UUID> emailNotificationMetaforms = new HashMap();
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
   * @throws IOException
   */
  public EmailNotification createEmailNotification(Metaform metaform, String subjectTemplate, String contentTemplate, List<String> emails) {
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
  public EmailNotification createEmailNotification(Metaform metaform, String subjectTemplate, String contentTemplate, List<String> emails, FieldRule notifyIf) {
    EmailNotification notification = new EmailNotification(subjectTemplate, contentTemplate, emails.toArray(String[]::new), null, notifyIf);

    emailNotificationMetaforms.put(notification, metaform.getId());
    return addClosable(getApi().createEmailNotification(metaform.getId(), notification));
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
  public void clean(EmailNotification emailNotification) throws Exception {
    getApi().deleteEmailNotification(emailNotificationMetaforms.get(emailNotification), emailNotification.getId());
  }
}
