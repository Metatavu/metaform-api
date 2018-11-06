package fi.metatavu.metaform.server.liquibase.changes;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.Configuration;
import org.keycloak.representations.idm.authorization.ResourceRepresentation;
import org.keycloak.representations.idm.authorization.ScopeRepresentation;

import fi.metatavu.metaform.server.keycloak.AuthorizationScope;
import fi.metatavu.metaform.server.keycloak.KeycloakConfigProvider;

/**
 * Abstract base class for custom Liquibase changes regarding authz operations
 * 
 * @author Antti Leppä
 */
public abstract class AbstractAuthzCustomChange extends AbstractCustomChange {
  
  /**
   * Creates protected resource into Keycloak
   * 
   * @param ownerId resource owner id
   * @param name resource's human readable name
   * @param uri resource's uri
   * @param type resource's type
   * @param scopes resource's scopes
   * 
   * @return created resource
   */
  protected ResourceRepresentation createProtectedResource(AuthzClient client, UUID ownerId, String name, String uri, String type, List<AuthorizationScope> scopes) {
    Set<ScopeRepresentation> scopeRepresentations = scopes.stream()
      .map(AuthorizationScope::getName)
      .map(ScopeRepresentation::new)
      .collect(Collectors.toSet());

    ResourceRepresentation resource = new ResourceRepresentation(name, scopeRepresentations, uri, type);
    resource.setOwner(ownerId.toString());
    resource.setOwnerManagedAccess(true);

    return client.protection().resource().create(resource);
  }
  
  /**
   * Constructs authz client for a realm
   * 
   * @param realmName realm
   * @return created authz client or null if client could not be created
   */
  protected AuthzClient getAuthzClient(String realmName) {
    Configuration configuration = KeycloakConfigProvider.getConfig(realmName);
    if (configuration != null) {
      return AuthzClient.create(configuration);
    }
    
    return null;
  }

}
