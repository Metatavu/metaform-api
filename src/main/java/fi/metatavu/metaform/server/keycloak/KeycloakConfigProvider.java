package fi.metatavu.metaform.server.keycloak;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.authorization.client.Configuration;

/**
 * Keycloak realm config provider
 * 
 * @author Antti Leppä
 */
public class KeycloakConfigProvider {
  
  private static final String REALM = System.getenv("KEYCLOAK_REALM");
  private static final String CLIENT_ID = System.getenv("KEYCLOAK_RESOURCE");
  private static final String CLIENT_SECRET = System.getenv("KEYCLOAK_SECRET");
  private static final String ADMIN_USER = System.getenv("KEYCLOAK_ADMIN_USER");
  private static final String ADMIN_PASS = System.getenv("KEYCLOAK_ADMIN_PASS");
  private static final String URL = System.getenv("KEYCLOAK_URL");
  
  /**
   * Returns all configured realms
   * 
   * @return all configured realms
   */
  public static List<String> getConfiguredRealms() {
    return Collections.singletonList(REALM);
  }
     
  /**
   * Resolves Keycloak client configuration for a realm
   * 
   * @return configuration or null if configuration could not be created
   */
  public static Configuration getConfig() {
    Map<String, Object> clientCredentials = new HashMap<>();
    clientCredentials.put("secret", CLIENT_SECRET);
    clientCredentials.put("provider", "secret");
    clientCredentials.put("realm-admin-user", ADMIN_USER);
    clientCredentials.put("realm-admin-pass", ADMIN_PASS);
    
    Configuration result = new Configuration();
    result.setAuthServerUrl(URL);
    result.setRealm(REALM);
    result.setResource(CLIENT_ID);
    result.setCredentials(clientCredentials);
    
    return result;
  }

}
