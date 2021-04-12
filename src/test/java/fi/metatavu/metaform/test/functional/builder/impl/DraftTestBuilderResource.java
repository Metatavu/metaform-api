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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Test builder resource for Drafts API
 */
public class DraftTestBuilderResource extends ApiTestBuilderResource<Draft, DraftsApi> {

  private final AccessTokenProvider accessTokenProvider;

  private final Map<Metaform, Draft> metaformDraftMap = new HashMap<>();

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

  /**
   * Creates draft
   *
   * @param metaform metaform
   * @param draftData draft data map
   * @return created draft
   */
  public Draft createDraft(Metaform metaform, Map<String, Object> draftData) {
    Draft draft = new Draft(draftData, null, null, null);
    Draft createdDraft = getApi().createDraft(Objects.requireNonNull(metaform.getId()), draft);
    metaformDraftMap.put(metaform, createdDraft);
    return addClosable(createdDraft);
  }

  /**
   * Updates draft
   * @param metaformId metaform id
   * @param draftId draft id
   * @param draft new payload
   * @return updated draft
   */
  public Draft updateDraft(UUID metaformId, UUID draftId, Draft draft) {
    return getApi().updateDraft(metaformId, draftId, draft);
  }

  /**
   * Finds draft
   * @param metaformId metaform id
   * @param draftId draft id
   * @return found draft
   */
  public Draft findDraft(UUID metaformId, UUID draftId) {
    return getApi().findDraft(metaformId, draftId);
  }
}