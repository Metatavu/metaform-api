package fi.metatavu.metaform.server.test.functional.builder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder;
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider;
import fi.metatavu.jaxrs.test.functional.builder.auth.AuthorizedTestBuilderAuthentication;
import fi.metatavu.jaxrs.test.functional.builder.auth.KeycloakAccessTokenProvider;
import fi.metatavu.metaform.api.client.infrastructure.ApiClient;
import fi.metatavu.metaform.server.test.functional.builder.auth.TestBuilderAuthentication;
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

  private final Logger logger = LoggerFactory.getLogger(TestBuilder.class);
  private TestBuilderAuthentication anonymousToken;
  private final String serverUrl = ConfigProvider.getConfig().getValue("metaforms.keycloak.admin.host", String.class);
  private static final String DEFAULT_UI_CLIENT_SECRET = "22614bd2-6a85-441c-857d-7606f4359e5b";
  protected static final String DEFAULT_UI_CLIENT_ID = "ui";
  protected static final String REALM_1 = "test-1";

  public TestBuilderAuthentication metaformAdmin = createTestBuilderAuthentication("metaform-admin", "test");
  public TestBuilderAuthentication metaformSuper = createTestBuilderAuthentication("metaform-super", "test");
  public TestBuilderAuthentication test1 = createTestBuilderAuthentication("test1.realm1", "test");
  public TestBuilderAuthentication test2 = createTestBuilderAuthentication("test2.realm1", "test");
  public TestBuilderAuthentication test3 = createTestBuilderAuthentication("test3.realm1", "test");
  public TestBuilderAuthentication answerer1 = createTestBuilderAuthentication("answerer-1", "test");
  public TestBuilderAuthentication answerer2 = createTestBuilderAuthentication("answerer-2", "test");
  public TestBuilderAuthentication anon = createTestBuilderAuthentication("anonymous", "anonymous");

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

  /**
   * Creates test builder authentication
   *
   * @param username username
   * @param password password
   * @return created test builder authentication
   */
  private TestBuilderAuthentication createTestBuilderAuthentication(String username, String password) {
    try {
      KeycloakAccessTokenProvider accessTokenProvider = new KeycloakAccessTokenProvider(serverUrl, REALM_1, DEFAULT_UI_CLIENT_ID, username, password, DEFAULT_UI_CLIENT_SECRET);
      return new TestBuilderAuthentication(this, accessTokenProvider);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}