package fi.metatavu.metaform.server.test.functional.builder.impl;

import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder;
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider;
import fi.metatavu.metaform.api.client.apis.AuditLogEntriesApi;
import fi.metatavu.metaform.api.client.infrastructure.ApiClient;
import fi.metatavu.metaform.api.client.infrastructure.ClientException;
import fi.metatavu.metaform.api.client.models.AuditLogEntry;
import fi.metatavu.metaform.server.test.TestSettings;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test builder resource for Audit Log Entries API
 */
public class AuditLogEntriesTestBuilderResource extends ApiTestBuilderResource<AuditLogEntry, AuditLogEntriesApi> {

  private final AccessTokenProvider accessTokenProvider;
  private final Map<UUID, UUID> auditLogEntriesMetaforms = new HashMap<>();

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

  /**
   * Lists audit log entries
   *
   * @param metaformId    metaform id
   * @param userId        user id
   * @param replyId       reply id
   * @param createdBefore created before
   * @param createdAfter  created after
   * @return audit entries
   */
  public AuditLogEntry[] listAuditLogEntries(UUID metaformId, UUID userId, UUID replyId, String createdBefore, String createdAfter) throws IOException {
    return getApi().listAuditLogEntries(metaformId, userId, replyId, createdBefore, createdAfter);
  }

  /**
   * Asserts that find returns fail with given status
   *
   * @param status        expected status
   * @param metaformId    metaform id
   * @param userId        user id
   * @param replyId       reply id
   * @param createdBefore created before
   * @param createdAfter  created after
   */
  public void assertListFailStatus(int status, UUID metaformId, UUID userId, UUID replyId, String createdBefore, String createdAfter) throws IOException {
    try {
      getApi().listAuditLogEntries(metaformId, userId, replyId, createdBefore, createdAfter);
      fail(String.format("Only users with metaform-view-all-audit-logs can access this view"));
    } catch (ClientException e) {
      assertEquals(status, e.getStatusCode());
    }
  }
}
