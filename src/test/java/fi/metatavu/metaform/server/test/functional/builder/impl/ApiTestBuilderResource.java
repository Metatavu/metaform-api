package fi.metatavu.metaform.server.test.functional.builder.impl;

import fi.metatavu.jaxrs.test.functional.builder.AbstractApiTestBuilderResource;
import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder;
import fi.metatavu.metaform.api.client.infrastructure.ApiClient;

/**
 * Abstract base class for API test resource builders
 *
 * @param <T> resource
 * @param <A> ApiClient for the resource
 */
abstract class ApiTestBuilderResource<T, A> extends AbstractApiTestBuilderResource<T, A, ApiClient> {

  private final ApiClient apiClient;

  /**
   * Returns API client
   *
   * @return API client
   */
  @Override
  protected ApiClient getApiClient() {
    return apiClient;
  }

  /**
   * Constructor
   *
   * @param testBuilder testBuilder
   */
  public ApiTestBuilderResource(AbstractTestBuilder<ApiClient> testBuilder, ApiClient apiClient) {
    super(testBuilder);
    this.apiClient = apiClient;
  }

}
