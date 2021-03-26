package fi.metatavu.metaform.test.functional.builder.impl;

import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder;
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider;
import fi.metatavu.metaform.api.client.apis.DraftsApi;
import fi.metatavu.metaform.api.client.infrastructure.ApiClient;
import fi.metatavu.metaform.api.client.models.Draft;
import fi.metatavu.metaform.api.client.models.Metaform;
import fi.metatavu.metaform.test.TestSettings;
import fi.metatavu.metaform.test.functional.builder.TestBuilder;

import java.io.IOException;
import java.util.*;

public class DraftTestBuilderResource extends ApiTestBuilderResource<Draft, DraftsApi> {

  private AccessTokenProvider accessTokenProvider;

  private Map<Metaform, Draft> metaformDraftMap = new HashMap<>();

  public DraftTestBuilderResource(
    AbstractTestBuilder<ApiClient> testBuilder,
    AccessTokenProvider accessTokenProvider,
    ApiClient apiClient) {
    super(testBuilder, apiClient);
    this.accessTokenProvider = accessTokenProvider;
  }

  @Override
  protected DraftsApi getApi() {
    try {
      ApiClient.Companion.setAccessToken(accessTokenProvider.getAccessToken());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return new DraftsApi(TestSettings.basePath);
  }

  @Override
  public void clean(Draft draft) throws IOException {
    TestBuilder testBuilder = new TestBuilder();
    final DraftsApi draftsAdminApi = testBuilder.metaformAdmin().drafts().getApi();

    metaformDraftMap.entrySet().stream()
      .filter(entry -> entry.getValue().equals(draft))
      .forEach(metaformDraftEntry ->
        draftsAdminApi.deleteDraft(Objects.requireNonNull(metaformDraftEntry.getKey().getId()),
          Objects.requireNonNull(metaformDraftEntry.getValue().getId())));
  }

  public Draft createDraft(Metaform metaform, Map<String, Object> draftData) {
    Draft draft = new Draft(draftData, null, null, null);
    Draft createdDraft = getApi().createDraft(Objects.requireNonNull(metaform.getId()), draft);
    metaformDraftMap.put(metaform, createdDraft);
    return addClosable(createdDraft);
  }

  public Draft updateDraft(UUID id, UUID id1, Draft updatePayload) {
    return getApi().updateDraft(id, id1, updatePayload);
  }

  public Draft findDraft(UUID metaformId, UUID draftId) {
    return getApi().findDraft(metaformId, draftId);
  }
}