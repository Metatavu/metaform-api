package fi.metatavu.metaform.test.functional.builder.resources;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Starts test container for keycloak
 */
public class KeycloakResource implements QuarkusTestResourceLifecycleManager {
  static final KeycloakContainer keycloak = new KeycloakContainer().withRealmImportFile("kc.json");

  @Override
  public Map<String, String> start() {
    keycloak.start();

    HashMap config = new HashMap<String, String>();
    config.put("quarkus.oidc.auth-server-url", String.format("%s/realms/test-1", keycloak.getAuthServerUrl()));
    config.put("quarkus.oidc.client-id", "metaform-api");
    config.put("metaforms.keycloak.admin.host", keycloak.getAuthServerUrl());
    config.put("metaforms.keycloak.admin.realm", "test-1");
    config.put("metaforms.keycloak.admin.user", "realm-admin");
    config.put("metaforms.keycloak.admin.password", "test");
    config.put("metaforms.keycloak.admin.admin_client_id", "metaform-api");
    config.put("metaforms.keycloak.admin.secret", "378833f9-dde8-4443-84ca-edfa26e2f0ee");
    return config;
  }

  @Override
  public void stop() {
    keycloak.stop();
  }

}
