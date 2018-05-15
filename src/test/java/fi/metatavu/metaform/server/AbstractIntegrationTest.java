package fi.metatavu.metaform.server;

import static io.restassured.RestAssured.given;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.math.NumberUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.metatavu.metaform.ApiClient;
import fi.metatavu.metaform.client.Metaform;
import fi.metatavu.metaform.client.MetaformsApi;
import fi.metatavu.metaform.client.RepliesApi;


/**
 * Abstract base class for integration tests
 * 
 * @author Heikki Kurhinen
 * @author Antti Leppä
 */
@SuppressWarnings ("squid:S1192")
public abstract class AbstractIntegrationTest extends AbstractTest {
  
  protected static final String BASE_URL = "/v1";
  protected static final String AUTH_SERVER_URL = "http://localhost:8280";
  protected static final String DEFAULT_KEYCLOAK_CLIENT_ID = "ui";
  protected static final UUID REALM1_USER_1_ID = UUID.fromString("b6039e55-3758-4252-9858-a973b0988b63");
  
  /**
   * Returns API base path
   * 
   * @return API base path
   */
  protected String getBasePath() {
   return String.format("http://%s:%d", getHost(), getPort());
  }
  
  /**
   * Returns API host
   * 
   * @return API host
   */
  protected String getHost() {
    return System.getProperty("it.host");
  }
  
  /**
   * Returns API port
   * 
   * @return API port
   */
  protected Integer getPort() {
    return NumberUtils.createInteger(System.getProperty("it.port.http"));
  }
  
  /**
   * Reads a Metaform from JSON file
   * 
   * @param form file name
   * @return Metaform object
   * @throws IOException throws IOException when JSON reading fails
   */
  protected Metaform readMetaform(String form) throws IOException {
    ObjectMapper objectMapper = getObjectMapper();
    String path = String.format("fi/metatavu/metaform/testforms/%s.json", form);
    ClassLoader classLoader = getClass().getClassLoader();
    try (InputStream formStream = classLoader.getResourceAsStream(path)) {    
      return objectMapper.readValue(formStream, Metaform.class);
    }
  }
  
  /**
   * Returns replies API authenticated by the given access token
   * 
   * @param accessTokenaccess token
   * @return replies API authenticated by the given access token
   */
  protected RepliesApi getRepliesApi(String accessToken) {
    ApiClient apiClient = getApiClient(accessToken);
    return apiClient.buildClient(RepliesApi.class);
  }

  /**
   * Returns metaforms API authenticated by the given access token
   * 
   * @param accessTokenaccess token
   * @return metaforms API authenticated by the given access token
   */
  protected MetaformsApi getMetaformsApi(String accessToken) {
    ApiClient apiClient = getApiClient(accessToken);
    return apiClient.buildClient(MetaformsApi.class);
  }
  
  /**
   * Returns API client authenticated by the given access token
   * 
   * @param accessTokenaccess token
   * @return API client authenticated by the given access token
   */
  private ApiClient getApiClient(String accessToken) {
    ApiClient apiClient = new ApiClient("bearer", String.format("Bearer %s", accessToken));
    String basePath = String.format("http://%s:%d/v1", getHost(), getPort());
    apiClient.setBasePath(basePath);
    return apiClient;
  }
  
  /**
   * Resolves an access token for realm, username and password
   * 
   * @param realm realm
   * @param username username
   * @param password password
   * @return an access token
   * @throws IOException thrown on communication failure
   */
  protected String getAccessToken(String realm, String username, String password) throws IOException {
    return getAccessToken(realm, DEFAULT_KEYCLOAK_CLIENT_ID, username, password);
  }

  /**
   * Resolves an admin access token for realm
   * 
   * @param realm realm
   * @return an access token
   * @throws IOException thrown on communication failure
   */
  protected String getAdminToken(String realm) throws IOException {
    return getAccessToken(realm, DEFAULT_KEYCLOAK_CLIENT_ID, "metaform-admin", "test"); 
  }

  /**
   * Resolves an access token for realm, client, username and password
   * 
   * @param realm realm
   * @param clientId clientId
   * @param username username
   * @param password password
   * @return an access token
   * @throws IOException thrown on communication failure
   */
  protected String getAccessToken(String realm, String clientId, String username, String password) throws IOException {
    String path = String.format("/auth/realms/%s/protocol/openid-connect/token", realm);
    
    String response = given()
      .baseUri(AUTH_SERVER_URL)
      .formParam("client_id", clientId)
      .formParam("grant_type", "password")
      .formParam("username", username)
      .formParam("password", password)
      .post(path)
      .getBody()
      .asString();
    
    Map<String, Object> responseMap = readJsonMap(response);
    return (String) responseMap.get("access_token");
  }
  
}