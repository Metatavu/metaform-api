package fi.metatavu.metaform.test.functional.builder.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fi.metatavu.metaform.client.ApiClient;
import fi.metatavu.metaform.test.functional.builder.TestBuilder;

/**
 * Abstract base class for API test resource builders
 *
 * @param <T> Resource
 * @param <A> API
 */
public abstract class ApiTestBuilderResource<T, A extends ApiClient.Api> extends fi.metatavu.jaxrs.test.functional.builder.AbstractApiTestBuilderResource<T, A, ApiClient> {

  private ApiClient apiClient;

  /**
   * Constructor
   *
   * @param testBuilder test builder instance
   * @param apiClient   API client instance
   */
  public ApiTestBuilderResource(TestBuilder testBuilder, ApiClient apiClient) {
    super(testBuilder);
    this.apiClient = apiClient;
  }

  @Override
  protected ApiClient getApiClient() {
    return apiClient;
  }

  /**
   * Builds API client
   *
   * @return API client
   */
  protected A getApi() {
    return apiClient.buildClient(getApiClass());
  }

  /**
   * Returns object mapper with default modules and settings
   *
   * @return object mapper
   */
  protected ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    return objectMapper;
  }

}
