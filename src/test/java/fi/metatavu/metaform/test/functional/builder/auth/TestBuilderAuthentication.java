package fi.metatavu.metaform.test.functional.builder.auth;

import java.io.IOException;

import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder;
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider;
import fi.metatavu.jaxrs.test.functional.builder.auth.AuthorizedTestBuilderAuthentication;
import fi.metatavu.metaform.api.client.infrastructure.ApiClient;
import fi.metatavu.metaform.test.functional.builder.TestBuilder;
import fi.metatavu.metaform.test.functional.builder.impl.*;

import static fi.metatavu.metaform.test.TestSettings.basePath;

/**
 * Default implementation of test builder authentication provider
 *
 * @author Antti Leppä
 */
public class TestBuilderAuthentication extends AuthorizedTestBuilderAuthentication<ApiClient> {

  private AbstractTestBuilder<ApiClient> testBuilder;
  private AccessTokenProvider accessTokenProvider;

  private MetaformTestBuilderResource metaforms;
  private ReplyTestBuilderResource replies;
  private ExportThemeTestBuilderResource exportThemes;
  private ExportThemeFilesTestBuilderResource exportfiles;
  private DraftTestBuilderResource drafts;
  private AttachmentTestBuilderResource attachments;
  private AuditLogEntriesTestBuilderResource auditLogs;

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

  public DraftTestBuilderResource drafts() throws IOException {
    if (drafts == null) {
      drafts = new DraftTestBuilderResource(testBuilder, accessTokenProvider, createClient());
    }

    return drafts;
  }

  public AttachmentTestBuilderResource attachments() throws IOException {
    if (attachments == null) {
      attachments = new AttachmentTestBuilderResource(testBuilder, accessTokenProvider, createClient());
    }

    return attachments;
  }

  public AuditLogEntriesTestBuilderResource auditLogs() throws IOException {
    if (auditLogs == null) {
      auditLogs = new AuditLogEntriesTestBuilderResource(testBuilder, accessTokenProvider, createClient());
    }

    return auditLogs;
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