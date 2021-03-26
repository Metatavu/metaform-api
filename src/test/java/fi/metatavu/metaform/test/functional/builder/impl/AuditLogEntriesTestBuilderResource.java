package fi.metatavu.metaform.test.functional.builder.impl;

import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder;
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider;
import fi.metatavu.metaform.api.client.apis.AuditLogEntriesApi;
import fi.metatavu.metaform.api.client.apis.RepliesApi;
import fi.metatavu.metaform.api.client.infrastructure.ApiClient;
import fi.metatavu.metaform.api.client.infrastructure.ClientException;
import fi.metatavu.metaform.api.client.models.AuditLogEntry;
import fi.metatavu.metaform.api.client.models.Reply;
import fi.metatavu.metaform.test.TestSettings;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class AuditLogEntriesTestBuilderResource extends ApiTestBuilderResource<AuditLogEntry, AuditLogEntriesApi> {

  private AccessTokenProvider accessTokenProvider;
  private Map<UUID, UUID> auditLogEntriesMetaforms = new HashMap<>();

  /**
   * Constructor
   *
   * @param testBuilder test builder
   * @param apiClient   initialized API client
   */
  public AuditLogEntriesTestBuilderResource(
    AbstractTestBuilder<ApiClient> testBuilder,
    AccessTokenProvider accessTokenProvider,
    ApiClient apiClient) {
    super(testBuilder, apiClient);
    this.accessTokenProvider = accessTokenProvider;
  }

  @Override
  protected AuditLogEntriesApi getApi() {
    try {
      ApiClient.Companion.setAccessToken(accessTokenProvider.getAccessToken());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return new AuditLogEntriesApi(TestSettings.basePath);
  }

  @Override
  public void clean(AuditLogEntry auditLogEntry) throws Exception {
    UUID metaformId = auditLogEntriesMetaforms.get(auditLogEntry.getId());
    getApi().deleteAuditLogEntry(metaformId, auditLogEntry.getId());
  }

  public AuditLogEntry[] listAuditLogEntries(UUID metaformId, UUID userId, UUID replyId, String createdBefore, String createdAfter) {
    return getApi().listAuditLogEntries(metaformId, userId, replyId, createdBefore, createdAfter);
  }

  public void assertListFailStatus(int status, UUID metaformId, UUID userId, UUID replyId, String createdBefore, String createdAfter) {
    try {
      getApi().listAuditLogEntries(metaformId, userId, replyId, createdBefore, createdAfter);
      fail(String.format("Only users with metaform-view-all-audit-logs can access this view"));
    } catch (ClientException e) {
      assertEquals(status, e.getStatusCode());
    }
  }
}
