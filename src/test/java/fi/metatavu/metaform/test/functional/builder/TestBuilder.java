package fi.metatavu.metaform.test.functional.builder;

import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder;
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider;
import fi.metatavu.jaxrs.test.functional.builder.auth.AuthorizedTestBuilderAuthentication;
import fi.metatavu.jaxrs.test.functional.builder.auth.KeycloakAccessTokenProvider;
import fi.metatavu.metaform.client.ApiClient;
import fi.metatavu.metaform.test.functional.builder.auth.TestBuilderAuthentication;
import fi.metatavu.metaform.test.functional.settings.TestSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Test builder class
 *
 * @author Antti Leppä
 */
public class TestBuilder extends AbstractTestBuilder<ApiClient> {

  private Logger logger = LoggerFactory.getLogger(getClass());
  private TestBuilderAuthentication metaformAdmin;
  private TestBuilderAuthentication metaformAnon;

  /**
   * Returns admin user instance of test builder authentication
   *
   * @return admin user instance of test builder authentication
   * @throws IOException thrown on communication errors
   */
  public TestBuilderAuthentication admin() throws IOException {
    if (metaformAdmin == null) {
      KeycloakAccessTokenProvider tokenProvider = new KeycloakAccessTokenProvider(
        TestSettings.getKeycloakHost(),
        TestSettings.getKeycloakRealm(),
        TestSettings.getKeycloakClientId(),
        "metaform-admin",
        "test",
        TestSettings.getKeycloakClientSecret()
      );

      metaformAdmin = new TestBuilderAuthentication(this, tokenProvider);
    }

    return metaformAdmin;
  }

  /**
   * Returns anonymous user instance of test builder authentication
   *
   * @return anonymous user instance of test builder authentication
   * @throws IOException thrown on communication errors
   */
  public TestBuilderAuthentication anon() throws IOException {
    if (metaformAnon == null) {
      KeycloakAccessTokenProvider tokenProvider = new KeycloakAccessTokenProvider(
        TestSettings.getKeycloakHost(),
        TestSettings.getKeycloakRealm(),
        TestSettings.getKeycloakClientId(),
        "anonymous",
        "anonymous",
        TestSettings.getKeycloakClientSecret()
      );

      metaformAnon = new TestBuilderAuthentication(this, tokenProvider);
    }

    return metaformAnon;
  }

  @Override
  public AuthorizedTestBuilderAuthentication<ApiClient> createTestBuilderAuthentication(AbstractTestBuilder<ApiClient> abstractTestBuilder, AccessTokenProvider accessTokenProvider) {
    try {
      return new TestBuilderAuthentication(this, accessTokenProvider);
    } catch (IOException e) {
      logger.error("Failed to authenticate", e);
    }

    return null;
  }
}