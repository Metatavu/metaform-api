package fi.metatavu.metaform.test.functional.builder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder;
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider;
import fi.metatavu.jaxrs.test.functional.builder.auth.AuthorizedTestBuilderAuthentication;
import fi.metatavu.jaxrs.test.functional.builder.auth.KeycloakAccessTokenProvider;
import fi.metatavu.metaform.api.client.infrastructure.ApiClient;
import fi.metatavu.metaform.test.functional.builder.auth.TestBuilderAuthentication;
import org.apache.commons.codec.binary.Base64;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertNotNull;

/**
 * Test builder class
 *
 * @author Antti Leppä
 */
public class TestBuilder extends AbstractTestBuilder<ApiClient> {
  private Logger logger = LoggerFactory.getLogger(TestBuilder.class);

  private TestBuilderAuthentication admin;
  private TestBuilderAuthentication metaformSuper;
  private TestBuilderAuthentication test1, test2, test3;
  private TestBuilderAuthentication anonymousToken;
  private TestBuilderAuthentication anon;

  private final String serverUrl = ConfigProvider.getConfig().getValue("metaforms.keycloak.admin.host", String.class);
  private static final String DEFAULT_UI_CLIENT_SECRET = "22614bd2-6a85-441c-857d-7606f4359e5b";
  protected static final String DEFAULT_UI_CLIENT_ID = "ui";
  protected static final String REALM_1 = "test-1";


  /**
   * Returns admin user instance of test builder authentication
   *
   * @return admin user instance of test builder authentication
   * @throws IOException thrown on communication errors
   */
  public TestBuilderAuthentication metaformAdmin() throws IOException {
    if (admin != null) {
      return admin;
    }

    return admin = new TestBuilderAuthentication(this, new KeycloakAccessTokenProvider(serverUrl, REALM_1,
      DEFAULT_UI_CLIENT_ID, "metaform-admin", "test", DEFAULT_UI_CLIENT_SECRET));
  }

  /**
   * Returns super user instance of test builder authentication
   *
   * @return super user instance of test builder authentication
   * @throws IOException thrown on communication errors
   */
  public TestBuilderAuthentication metaformSuper() throws IOException {
    if (metaformSuper != null) {
      return metaformSuper;
    }

    return metaformSuper = new TestBuilderAuthentication(this, new KeycloakAccessTokenProvider(serverUrl, REALM_1,
      DEFAULT_UI_CLIENT_ID, "metaform-super", "test", DEFAULT_UI_CLIENT_SECRET));
  }

  /**
   * Returns test1 user instance of test builder authentication
   *
   * @return test1 user instance of test builder authentication
   * @throws IOException thrown on communication errors
   */
  public TestBuilderAuthentication test1() throws IOException {
    if (test1 != null) {
      return test1;
    }

    return test1 = new TestBuilderAuthentication(this, new KeycloakAccessTokenProvider(serverUrl, REALM_1,
      DEFAULT_UI_CLIENT_ID, "test1.realm1", "test", DEFAULT_UI_CLIENT_SECRET));
  }

  /**
   * Returns test2 user instance of test builder authentication
   *
   * @return test2 user instance of test builder authentication
   * @throws IOException thrown on communication errors
   */
  public TestBuilderAuthentication test2() throws IOException {
    if (test2 != null) {
      return test2;
    }

    return test2 = new TestBuilderAuthentication(this, new KeycloakAccessTokenProvider(serverUrl, REALM_1,
      DEFAULT_UI_CLIENT_ID, "test2.realm1", "test", DEFAULT_UI_CLIENT_SECRET));
  }

  /**
   * Returns test3 user instance of test builder authentication
   *
   * @return test3 user instance of test builder authentication
   * @throws IOException thrown on communication errors
   */
  public TestBuilderAuthentication test3() throws IOException {
    if (test3 != null) {
      return test3;
    }

    return test3 = new TestBuilderAuthentication(this, new KeycloakAccessTokenProvider(serverUrl, REALM_1,
      DEFAULT_UI_CLIENT_ID, "test3.realm1", "test", DEFAULT_UI_CLIENT_SECRET));
  }

  /**
   * Returns anonymous user instance of test builder authentication
   *
   * @return anonymous user instance of test builder authentication
   * @throws IOException thrown on communication errors
   */
  public TestBuilderAuthentication anon() throws IOException {
    if (anon != null) {
      return anon;
    }

    return anon = new TestBuilderAuthentication(this, new KeycloakAccessTokenProvider(serverUrl, REALM_1,
      DEFAULT_UI_CLIENT_ID, "anonymous", "anonymous", DEFAULT_UI_CLIENT_SECRET));
  }

  /**
   * Returns anonymous token auth
   *
   * @return anonymous user instance of test builder authentication
   * @throws IOException thrown on communication errors
   */
  public TestBuilderAuthentication anonymousToken() throws IOException {
    if (anonymousToken != null) {
      return anonymousToken;
    }

    String path = String.format("/realms/%s/protocol/openid-connect/token", REALM_1);

    String password = String.format("%s:%s", DEFAULT_UI_CLIENT_ID, DEFAULT_UI_CLIENT_SECRET);
    String passwordEncoded = Base64.encodeBase64String(password.getBytes(StandardCharsets.UTF_8));
    String authorization = String.format("Basic %s", passwordEncoded);
    String response = given()
      .baseUri(ConfigProvider.getConfig().getValue("metaforms.keycloak.admin.host", String.class))
      .header("Authorization", authorization)
      .formParam("grant_type", "client_credentials")
      .post(path)
      .getBody()
      .asString();

    ObjectMapper objectMapper = new ObjectMapper();
    Map<String, Object> responseMap = objectMapper.readValue(response, new TypeReference<>() {
    });
    String token = (String) responseMap.get("access_token");
    assertNotNull(token);

    return anonymousToken = new TestBuilderAuthentication(this, new FilledAccessTokenProvider(token));
  }

  @Override
  public AuthorizedTestBuilderAuthentication<ApiClient> createTestBuilderAuthentication(AbstractTestBuilder<ApiClient> abstractTestBuilder, AccessTokenProvider accessTokenProvider) {
    try {
      return new TestBuilderAuthentication(this, accessTokenProvider);
    } catch (IOException e) {
      logger.error(e.getMessage());
    }
    return null;
  }
}