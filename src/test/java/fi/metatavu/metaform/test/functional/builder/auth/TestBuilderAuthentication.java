package fi.metatavu.metaform.test.functional.builder.auth;

import java.io.IOException;

import feign.Feign.Builder;
import fi.metatavu.feign.UmaErrorDecoder;
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider;
import fi.metatavu.jaxrs.test.functional.builder.auth.AuthorizedTestBuilderAuthentication;
import fi.metatavu.metaform.client.ApiClient;
import fi.metatavu.metaform.test.functional.builder.TestBuilder;
import fi.metatavu.metaform.test.functional.builder.impl.MetaformTestBuilderResource;
import fi.metatavu.metaform.test.functional.builder.impl.ReplyTestBuilderResource;
import fi.metatavu.metaform.test.functional.settings.TestSettings;

/**
 * Default implementation of test builder authentication provider
 *
 * @author Antti Leppä
 */
public class TestBuilderAuthentication extends AuthorizedTestBuilderAuthentication<ApiClient> {

  private TestBuilder testBuilder;
  private MetaformTestBuilderResource metaforms;
  private ReplyTestBuilderResource replies;

  /**
   * Constructor
   *
   * @param testBuilder         testBuilder
   * @param accessTokenProvider access token builder
   */
  public TestBuilderAuthentication(TestBuilder testBuilder, AccessTokenProvider accessTokenProvider) throws IOException {
    super(testBuilder, accessTokenProvider);
    this.testBuilder = testBuilder;
  }

  /**
   * Returns metaform test builder resource
   *
   * @return metaform test builder resource
   * @throws IOException thrown on communication error
   */
  public MetaformTestBuilderResource metaforms() throws IOException {
    if (metaforms == null) {
      metaforms = new MetaformTestBuilderResource(testBuilder, createClient());
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
      replies = new ReplyTestBuilderResource(testBuilder, createClient());
    }

    return replies;
  }

  @Override
  protected ApiClient createClient(String accessToken) {
    String authorization = accessToken != null ? String.format("Bearer %s", accessToken) : null;
    ApiClient apiClient = authorization != null ? new ApiClient("bearer", authorization) : new ApiClient();

    String basePath = TestSettings.getApiBasePath();
    if (accessToken != null) {
      Builder feignBuilder = apiClient.getFeignBuilder();
      feignBuilder.errorDecoder(new UmaErrorDecoder(feignBuilder, authorization, apiClient::setApiKey));
    }

    apiClient.setBasePath(basePath);
    return apiClient;
  }

}