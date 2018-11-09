package fi.metatavu.metaform.server.keycloak;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.GroupPoliciesResource;
import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.admin.client.resource.PoliciesResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.ResourceScopesResource;
import org.keycloak.admin.client.resource.ResourcesResource;
import org.keycloak.admin.client.resource.ScopePermissionsResource;
import org.keycloak.authorization.client.Configuration;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.authorization.DecisionEffect;
import org.keycloak.representations.idm.authorization.DecisionStrategy;
import org.keycloak.representations.idm.authorization.GroupPolicyRepresentation;
import org.keycloak.representations.idm.authorization.Logic;
import org.keycloak.representations.idm.authorization.PolicyEvaluationRequest;
import org.keycloak.representations.idm.authorization.PolicyEvaluationResponse;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;
import org.keycloak.representations.idm.authorization.ResourceRepresentation;
import org.keycloak.representations.idm.authorization.ScopePermissionRepresentation;
import org.keycloak.representations.idm.authorization.ScopeRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for Keycloak admin client
 * 
 * @author Antti Leppä
 */
public class KeycloakAdminUtils {
  
  private static Logger logger = LoggerFactory.getLogger(KeycloakAdminUtils.class);
  
  private KeycloakAdminUtils() {
    // Private constructor
  }
  
  /**
   * Creates admin client for a realm
   * 
   * @param realmName realm
   * @return admin client
   */
  public static Keycloak getAdminClient(String realmName) {
    return getAdminClient(KeycloakConfigProvider.getConfig(realmName));
  }
  
  /**
   * Creates admin client for config
   * 
   * @param configuration configuration
   * @return admin client
   */
  public static Keycloak getAdminClient(Configuration configuration) {
    Map<String, Object> credentials = configuration.getCredentials();
    String clientSecret = (String) credentials.get("secret");
    String adminUser = (String) credentials.get("realm-admin-user");
    String adminPass = (String) credentials.get("realm-admin-pass");
    
    return KeycloakBuilder.builder()
      .serverUrl(configuration.getAuthServerUrl())
      .realm(configuration.getRealm())
      .grantType(OAuth2Constants.PASSWORD)
      .clientId(configuration.getResource())
      .clientSecret(clientSecret)
      .username(adminUser)
      .password(adminPass)
      .build();
  }
  
  /**
   * Constructs Keycloak realm configuration
   * 
   * @param realmName realm
   * @return Keycloak realm configuration
   */
  public static Configuration getKeycloakConfiguration(String realmName) {
    return KeycloakConfigProvider.getConfig(realmName);
  }
  
  /**
   * Resolves Keycloak API client
   * 
   * @param keycloak admin client
   * @param realmName realm name
   * @return Keycloak client
   */
  public static ClientRepresentation getKeycloakClient(Keycloak keycloak, String realmName) {
    Configuration keycloakConfiguration = getKeycloakConfiguration(realmName);
    return findClient(keycloak, realmName, keycloakConfiguration.getResource());
  }
  
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
  @SuppressWarnings ("squid:S00107")
  public static UUID createProtectedResource(Keycloak keycloak, String realmName, ClientRepresentation keycloakClient, UUID ownerId, String name, String uri, String type, List<AuthorizationScope> scopes) {
    ResourcesResource resources = keycloak.realm(realmName).clients().get(keycloakClient.getId()).authorization().resources();
    
    Set<ScopeRepresentation> scopeRepresentations = scopes.stream()
      .map(AuthorizationScope::getName)
      .map(ScopeRepresentation::new)
      .collect(Collectors.toSet());

    ResourceRepresentation resource = new ResourceRepresentation(name, scopeRepresentations, uri, type);
    
    if (ownerId != null) {
      resource.setOwner(ownerId.toString());
      resource.setOwnerManagedAccess(true);
    }
    
    UUID resourceId = getCreateResponseId(resources.create(resource));
    if (resourceId != null) {
      return resourceId;
    }
    
    List<ResourceRepresentation> foundResources = resources.findByName(name, ownerId == null ? null : ownerId.toString());
    if (foundResources.isEmpty()) {
      return null;
    }
    
    if (foundResources.size() > 1) {
      logger.warn("Found more than one resource with name {}", name);
    }
    
    return UUID.fromString(foundResources.get(0).getId());
  }
  
  /**
   * Creates new scope permission for resource
   * 
   * @param keycloak keycloak admin client
   * @param realmName realm's name
   * @param clientId client id
   * @param resourceId resource id
   * @param scope authorization scope
   * @param name name
   * @param policyIds policies
   * @param decisionStrategy 
   * @return created permission
   */
  @SuppressWarnings ("squid:S00107")
  public static void upsertScopePermission(Keycloak keycloak, String realmName, ClientRepresentation client, UUID resourceId, Collection<AuthorizationScope> scopes, String name, DecisionStrategy decisionStrategy, Collection<UUID> policyIds) {
    RealmResource realm = keycloak.realm(realmName);    
    ScopePermissionsResource scopeResource = realm.clients().get(client.getId()).authorization().permissions().scope();    
    ScopePermissionRepresentation existingPermission = scopeResource.findByName(name);
    
    ScopePermissionRepresentation representation = new ScopePermissionRepresentation();
    representation.setDecisionStrategy(decisionStrategy);
    representation.setLogic(Logic.POSITIVE);
    representation.setName(name);
    representation.setType("scope");    
    representation.setResources(Collections.singleton(resourceId.toString()));
    representation.setScopes(scopes.stream().map(AuthorizationScope::getName).collect(Collectors.toSet()));
    representation.setPolicies(policyIds.stream().map(UUID::toString).collect(Collectors.toSet()));
    
    Response response = scopeResource.create(representation);
    try {
      if (existingPermission == null) {
        int status = response.getStatus();
        if (status != 201) {
          String message = "Unknown error";
          try {
            message = IOUtils.toString((InputStream) response.getEntity(), "UTF-8");
          } catch (IOException e) {
            logger.warn("Failed read error message", e);
          }
          
          logger.warn("Failed to create scope permission for resource {} with message {}", resourceId, message);
        }
      } else {
        realm.clients().get(client.getId()).authorization().permissions().scope()
          .findById(existingPermission.getId())
          .update(representation);
      }
    } finally {
      response.close();
    }
  }
  
  /**
   * Updates groups and group policies into Keycloak
   * 
   * @param keycloak admin client
   * @param realmName realm name
   * @param clientId client id
   * @param groupMap groups names
   */
  public static void updatePermissionGroups(Keycloak keycloak, String realmName, ClientRepresentation client, List<String> groupNames) {
    RealmResource realm = keycloak.realm(realmName);
    GroupsResource groups = realm.groups();
    GroupPoliciesResource groupPolicies = realm.clients().get(client.getId()).authorization().policies().group();
    
    Map<String, UUID> existingGroups = groups.groups().stream()
      .collect(Collectors.toMap(GroupRepresentation::getName, group -> UUID.fromString(group.getId())));
    
    for (String groupName : groupNames) {
      UUID groupId = existingGroups.get(groupName);
      if (groupId == null) {
        GroupRepresentation groupRepresentation = new GroupRepresentation();
        groupRepresentation.setName(groupName);
        groupId = getCreateResponseId(groups.add(groupRepresentation));  
      }
      
      GroupPolicyRepresentation policyRepresentation = groupPolicies.findByName(groupName);
      if (policyRepresentation == null && groupId != null) {
        groupPolicies.create(policyRepresentation);
        policyRepresentation = new GroupPolicyRepresentation();
        policyRepresentation.setName(groupName);
        policyRepresentation.setDecisionStrategy(DecisionStrategy.UNANIMOUS);
        policyRepresentation.addGroup(groupId.toString(), true);
        groupPolicies.create(policyRepresentation);
      }
    }
  }

  /**
   * Returns list of permitted users for a resource with given scopes 
   * 
   * @param keycloak keycloak client instance
   * @param realmName realm name
   * @param clientId client id
   * @param resourceId resource id
   * @param resourceName resource name
   * @param scopes scopes
   * @return set of user ids
   */
  public static Set<UUID> getResourcePermittedUsers(Keycloak keycloak, String realmName, ClientRepresentation client, UUID resourceId, String resourceName, List<AuthorizationScope> scopes) {
    RealmResource realm = keycloak.realm(realmName);
    return getPermittedUsers(realm, client, resourceId, resourceName, scopes);      
  }
  
  /**
   * Find policy id by name
   * 
   * @param keycloak Keycloak admin client
   * @param realmName realm name
   * @param client client
   * @param names group names
   * @return list of group policy ids
   */
  public static UUID getPolicyIdByName(Keycloak keycloak, String realmName, ClientRepresentation client, String name) {
    Set<UUID> ids = getPolicyIdsByNames(keycloak, realmName, client, Arrays.asList(name));
    if (ids.isEmpty()) {
      return null;
    }
    
    return ids.iterator().next();
  }

  /**
   * Lists policy ids by names
   * 
   * @param keycloak Keycloak admin client
   * @param realmName realm name
   * @param client client
   * @param names group names
   * @return list of group policy ids
   */
  public static Set<UUID> getPolicyIdsByNames(Keycloak keycloak, String realmName, ClientRepresentation client, List<String> names) {
    RealmResource realm = keycloak.realm(realmName);
    PoliciesResource policies = realm.clients().get(client.getId()).authorization().policies();
    
    return names.stream()
      .map(policies::findByName)
      .filter(Objects::nonNull)
      .map(PolicyRepresentation::getId)
      .map(UUID::fromString)
      .collect(Collectors.toSet());
  }

  /**
   * Creates authorization scopes into Keycloak
   * 
   * @param keycloak Keycloak admin client
   * @param realmName realm name
   * @param client client
   * @param scopes scopes to be created
   * @return 
   */
  public static List<UUID> createAuthorizationScopes(Keycloak keycloak, String realmName, ClientRepresentation client, List<AuthorizationScope> scopes) {
    ResourceScopesResource scopesResource = keycloak.realm(realmName).clients().get(client.getId()).authorization().scopes();
    
    return scopes.stream()
      .map(AuthorizationScope::getName)
      .map(ScopeRepresentation::new)
      .map(scopesResource::create)
      .map(KeycloakAdminUtils::getCreateResponseId)
      .collect(Collectors.toList());
  }

  /**
   * Finds user by username
   * 
   * @param keycloak Keycloak admin client
   * @param realmName realm name
   * @param username username
   * @return found user or null if not found
   */
  public static UserRepresentation findUser(Keycloak keycloak, String realmName, String username) {
    List<UserRepresentation> result = keycloak.realm(realmName).users().search(username);
    if (result.isEmpty()) {
      return null;
    }
    
    return result.get(0);
  }
  
  /**
   * Returns list of permitted users for a resource with given scopes 
   * 
   * @param realm realm name
   * @param client client
   * @param resourceId resource id
   * @param resourceName resource name
   * @param scopes scopes
   * @return set of user ids
   */
  private static Set<UUID> getPermittedUsers(RealmResource realm, ClientRepresentation client, UUID resourceId, String resourceName, List<AuthorizationScope> scopes) {
    Map<UUID, DecisionEffect> policies = evaluatePolicies(realm, client, resourceId, resourceName, scopes);
    
    return policies.entrySet().stream()
      .filter(entry -> DecisionEffect.PERMIT.equals(entry.getValue()))
      .map(Entry::getKey)
      .collect(Collectors.toSet());
  }
  
  /**
   * Evaluates policies resource for realm users 
   * 
   * @param realm realm name
   * @param client client
   * @param resourceId resource id
   * @param resourceName resource name
   * @param scopes scopes
   * @return map of results where key is user id and value is decision
   */
  private static Map<UUID, DecisionEffect> evaluatePolicies(RealmResource realm, ClientRepresentation client, UUID resourceId, String resourceName, List<AuthorizationScope> scopes) {
    Map<UUID, DecisionEffect> result = new HashMap<>();
    int firstResult = 0;
    int maxResults = 10;
    
    while (firstResult < 1000) {
      List<UserRepresentation> users = realm.users().list(firstResult, maxResults);
      
      for (UserRepresentation user : users) {
        String userId = user.getId();
        result.put(UUID.fromString(userId), evaluatePolicy(realm, client, resourceId, resourceName, userId, scopes));
      }
      
      if (users.isEmpty() || (users.size() < maxResults)) {
        break;
      }

      firstResult += maxResults;
    }
    
    return result;
  }
   
  /**
   * Evaluates policy for a resource
   * 
   * @param realm realm name
   * @param client client
   * @param resourceId resource id
   * @param resourceName resource name
   * @param userId user's id
   * @param scopes scopes
   * @return decision
   */
  private static DecisionEffect evaluatePolicy(RealmResource realm, ClientRepresentation client, UUID resourceId, String resourceName, String userId, List<AuthorizationScope> scopes) {
    try {
      PolicyEvaluationRequest evaluationRequest = createEvaluationRequest(client, resourceId, resourceName, userId, scopes);
      PolicyEvaluationResponse response = realm.clients().get(client.getId()).authorization().policies().evaluate(evaluationRequest);
      return response.getStatus();
    } catch (InternalServerErrorException e) {
      logger.error("Failed to evaluate resource {} policy for {}", resourceName, userId, e);
      return DecisionEffect.DENY;
    }
  }
  
  /**
   * Creates Keycloak policy evaluation request
   * 
   * @param client client
   * @param resourceId resource id
   * @param resourceName resource name
   * @param userId userId 
   * @param scopes scopes
   * @return created request
   */
  private static PolicyEvaluationRequest createEvaluationRequest(ClientRepresentation client, UUID resourceId, String resourceName, String userId, Collection<AuthorizationScope> scopes) {
    Set<ScopeRepresentation> resourceScopes = scopes.stream().map(AuthorizationScope::getName).map(ScopeRepresentation::new).collect(Collectors.toSet());
    ResourceRepresentation resource = new ResourceRepresentation(resourceName, resourceScopes);
    resource.setId(resourceId.toString());
    
    PolicyEvaluationRequest evaluationRequest = new PolicyEvaluationRequest();
    evaluationRequest.setClientId(client.getId());
    evaluationRequest.setResources(Arrays.asList(resource));
    evaluationRequest.setUserId(userId);
    
    return evaluationRequest;
  }  
  
  
  /**
   * Finds a id from Keycloak create response 
   * 
   * @param response response object
   * @return id
   */
  private static UUID getCreateResponseId(Response response) {
    if (response.getStatus() != 201) {
      try {
        if (logger.isErrorEnabled()) {
          logger.error("Failed to execute create: {}", IOUtils.toString((InputStream) response.getEntity(), "UTF-8"));
        }
      } catch (IOException e) {
        logger.error("Failed to extract error message", e);
      }
      
      return null;
    }
    
    String location = response.getHeaderString("location");
    if (StringUtils.isBlank(location)) {
      return null;
    }
    
    Pattern pattern = Pattern.compile(".*\\/(.*)$");
    Matcher matcher = pattern.matcher(location);
    
    if (matcher.find()) {
      return UUID.fromString(matcher.group(1));
    }
    
    return null;
  }

  /**
   * Finds a Keycloak client by realm and clientId 
   * 
   * @param keycloak keycloak admin client
   * @param realmName realm's name
   * @param clientId clientId 
   * @return client or null if not found
   */
  private static ClientRepresentation findClient(Keycloak keycloak, String realmName, String clientId) {
    List<ClientRepresentation> clients = keycloak.realm(realmName).clients().findByClientId(clientId);
    if (!clients.isEmpty()) {
      return clients.get(0);
    }
    
    return null;
  }
  
}
