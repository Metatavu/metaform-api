package fi.metatavu.metaform.server.liquibase.changes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.authorization.client.Configuration;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.authorization.DecisionStrategy;

import fi.metatavu.metaform.server.keycloak.AuthorizationScope;
import fi.metatavu.metaform.server.keycloak.KeycloakAdminUtils;
import fi.metatavu.metaform.server.keycloak.KeycloakConfigProvider;
import fi.metatavu.metaform.server.keycloak.ResourceType;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;

/**
 * Custom change for creating authorization resources for all replies
 * 
 * @author Antti Leppä
 */
public class CreateRealmAuthorizations extends AbstractAuthzCustomChange {
  
  @Override
  public void execute(Database database) throws CustomChangeException {
    for (String realmName : KeycloakConfigProvider.getConfiguredRealms()) {
      this.createRealmAuthorizations(realmName);
    }
  }

  /**
   * create realm authorizations
   * 
   * @param connection JDBC connection
   * @param realmName realm name
   * @throws CustomChangeException when migration fails
   */
  private void createRealmAuthorizations(String realmName) throws CustomChangeException {
    try {
      Configuration keycloakConfiguration = KeycloakAdminUtils.getKeycloakConfiguration();
      Keycloak adminClient = KeycloakAdminUtils.getAdminClient(keycloakConfiguration);
      ClientRepresentation keycloakClient = KeycloakAdminUtils.getKeycloakClient(adminClient);    
      KeycloakAdminUtils.createAuthorizationScopes(adminClient, realmName, keycloakClient, Arrays.asList(AuthorizationScope.values()));
      UUID defaultPolicyId = KeycloakAdminUtils.getPolicyIdByName(adminClient, keycloakClient, "Default Policy");
      List<AuthorizationScope> scopes = Arrays.asList(AuthorizationScope.REPLY_CREATE, AuthorizationScope.REPLY_VIEW);
      UUID resourceId = KeycloakAdminUtils.createProtectedResource(adminClient, keycloakClient, null, "replies", "/v1/metaforms/{metaformId}/replies", ResourceType.REPLY.getUrn(), scopes);
      KeycloakAdminUtils.upsertScopePermission(adminClient, keycloakClient, resourceId, scopes, "replies", DecisionStrategy.UNANIMOUS, Collections.singleton(defaultPolicyId));
      appendConfirmationMessage(String.format("Created default permissions into realm %s", realmName));  
    } catch (Exception e) {
      String keycloakErrorMessage = getKeycloakErrorMessage(e);
      throw new CustomChangeException(String.format("Realm %s migration failed with following error: %s", realmName, keycloakErrorMessage == null ? e.getMessage() : keycloakErrorMessage), e);
    }
  }

}
