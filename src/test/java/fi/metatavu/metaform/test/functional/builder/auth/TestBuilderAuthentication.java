package fi.metatavu.metaform.test.functional.builder.auth;

import java.io.IOException;

import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder;
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider;
import fi.metatavu.jaxrs.test.functional.builder.auth.AuthorizedTestBuilderAuthentication;
import fi.metatavu.metaform.api.client.infrastructure.ApiClient;
import fi.metatavu.metaform.api.client.models.EmailNotification;
import fi.metatavu.metaform.test.functional.builder.TestBuilder;
import fi.metatavu.metaform.test.functional.builder.impl.*;

import static fi.metatavu.metaform.test.TestSettings.basePath;

/**
 * Default implementation of test builder authentication provider
 *
 * @author Antti Leppä
 */
public class TestBuilderAuthentication extends AuthorizedTestBuilderAuthentication<ApiClient> {

  private final AbstractTestBuilder<ApiClient> testBuilder;
  private final AccessTokenProvider accessTokenProvider;

  private MetaformTestBuilderResource metaforms;
  private ReplyTestBuilderResource replies;
  private ExportThemeTestBuilderResource exportThemes;
  private ExportThemeFilesTestBuilderResource exportfiles;
  private DraftTestBuilderResource drafts;
  private AttachmentTestBuilderResource attachments;
  private AuditLogEntriesTestBuilderResource auditLogs;
  private EmailNotificationsTestBuilderResource emailNotifications;

  public TestBuilderAuthentication(AbstractTestBuilder<ApiClient> testBuilder, AccessTokenProvider accessTokenProvider) throws IOException {
    super(testBuilder, accessTokenProvider);
    this.testBuilder = testBuilder;
    this.accessTokenProvider = accessTokenProvider;
  }

  /**
   * Returns metaform test builder resource
   *
   * @return metaform test builder resource
   * @throws IOException thrown on communication error
   */
  public MetaformTestBuilderResource metaforms() throws IOException {
    if (metaforms == null) {
      metaforms = new MetaformTestBuilderResource(testBuilder, accessTokenProvider, createClient());
    }

    return metaforms;
  }

  /**
   * Returns replies test builder resource
   *
   * @return replies test builder resource
   * @throws IOException thrown on communication error
   */
  public ReplyTestBuilderResource replies() throws IOException {
    if (replies == null) {
      replies = new ReplyTestBuilderResource(testBuilder, accessTokenProvider, createClient());
    }

    return replies;
  }

  /**
   * Returns export themes test builder resource
   *
   * @return export themes test builder resource
   * @throws IOException thrown on communication error
   */
  public ExportThemeTestBuilderResource exportThemes() throws IOException {
    if (exportThemes == null) {
      exportThemes = new ExportThemeTestBuilderResource(testBuilder, accessTokenProvider, createClient());
    }

    return exportThemes;
  }

  /**
   * Returns export files test builder resource
   *
   * @return export files test builder resource
   * @throws IOException thrown on communication error
   */
  public ExportThemeFilesTestBuilderResource exportfiles() throws IOException {
    if (exportfiles == null) {
      exportfiles = new ExportThemeFilesTestBuilderResource(testBuilder, accessTokenProvider, createClient());
    }

    return exportfiles;
  }

  /**
   * Builds Drafts API test resource
   *
   * @return Drafts API test resource
   * @throws IOException thrown on communication error
   */
  public DraftTestBuilderResource drafts() throws IOException {
    if (drafts == null) {
      drafts = new DraftTestBuilderResource(testBuilder, accessTokenProvider, createClient());
    }

    return drafts;
  }

  /**
   * Builds Attachments API test resource
   *
   * @return Builds Attachments API test resource
   * @throws IOException thrown on communication error
   */
  public AttachmentTestBuilderResource attachments() throws IOException {
    if (attachments == null) {
      attachments = new AttachmentTestBuilderResource(testBuilder, accessTokenProvider, createClient());
    }

    return attachments;
  }

  /**
   * Builds Audit Logs API test resource
   *
   * @return Audit Logs API resource
   * @throws IOException thrown on communication error
   */
  public AuditLogEntriesTestBuilderResource auditLogs() throws IOException {
    if (auditLogs == null) {
      auditLogs = new AuditLogEntriesTestBuilderResource(testBuilder, accessTokenProvider, createClient());
    }

    return auditLogs;
  }

  /**
   * Builds EmailNotifications API test resource
   *
   * @return EmailNotifications API resource
   * @throws IOException thrown on communication error
   */
  public EmailNotificationsTestBuilderResource emailNotifications() throws IOException {
    if (emailNotifications == null) {
      emailNotifications = new EmailNotificationsTestBuilderResource(testBuilder, accessTokenProvider, createClient());
    }

    return emailNotifications;
  }

  @Override
  protected ApiClient createClient(String s) {
    ApiClient client = new ApiClient(basePath);
    ApiClient.Companion.setAccessToken(s);
    return client;
  }


  public String token() throws IOException {
    return accessTokenProvider.getAccessToken();
  }
}