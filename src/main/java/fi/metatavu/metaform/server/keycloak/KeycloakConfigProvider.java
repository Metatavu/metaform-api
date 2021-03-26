package fi.metatavu.metaform.server.keycloak;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.config.ConfigProvider;
import org.keycloak.authorization.client.Configuration;

/**
 * Keycloak realm config provider
 * 
 * @author Antti Leppä
 */
public class KeycloakConfigProvider {
  
  private static final String REALM = ConfigProvider.getConfig().getValue("metaforms.keycloak.admin.realm", String.class);
  private static final String CLIENT_ID = ConfigProvider.getConfig().getValue("metaforms.keycloak.admin.admin_client_id", String.class);
  private static final String CLIENT_SECRET = ConfigProvider.getConfig().getValue("metaforms.keycloak.admin.secret", String.class);
  private static final String ADMIN_USER = ConfigProvider.getConfig().getValue("metaforms.keycloak.admin.user", String.class);
  private static final String ADMIN_PASS = ConfigProvider.getConfig().getValue("metaforms.keycloak.admin.password", String.class);
  private static final String URL =  ConfigProvider.getConfig().getValue("metaforms.keycloak.admin.host", String.class);
  
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
