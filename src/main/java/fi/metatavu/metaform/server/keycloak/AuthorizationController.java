package fi.metatavu.metaform.server.keycloak;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.GroupPoliciesResource;
import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.ScopePermissionsResource;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.authorization.DecisionEffect;
import org.keycloak.representations.idm.authorization.DecisionStrategy;
import org.keycloak.representations.idm.authorization.GroupPolicyRepresentation;
import org.keycloak.representations.idm.authorization.Logic;
import org.keycloak.representations.idm.authorization.PolicyEvaluationRequest;
import org.keycloak.representations.idm.authorization.PolicyEvaluationResponse;
import org.keycloak.representations.idm.authorization.ResourceRepresentation;
import org.keycloak.representations.idm.authorization.ScopePermissionRepresentation;
import org.keycloak.representations.idm.authorization.ScopeRepresentation;
import org.slf4j.Logger;

/**
 * Controller for Keycloak authrorization functions
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class AuthorizationController {
  
  @Inject
  private Logger logger;

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
  public ResourceRepresentation createProtectedResource(AuthzClient client, UUID ownerId, String name, String uri, String type, List<AuthorizationScope> scopes) {
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
   * Creates new scope permission for resource
   * 
   * @param keycloak keycloak admin client
   * @param realmName realm's name
   * @param clientId client id
   * @param resourceId resource id
   * @param scope authorization scope
   * @param name name
   * @param policyIds policies
   * @return created permission
   */
  public void upsertScopePermission(Keycloak keycloak, String realmName, ClientRepresentation client, UUID resourceId, AuthorizationScope scope, String name, Set<UUID> policyIds) {
    RealmResource realm = keycloak.realm(realmName);    
    ScopePermissionsResource scopeResource = realm.clients().get(client.getId()).authorization().permissions().scope();    
    ScopePermissionRepresentation existingPermission = scopeResource.findByName(name);
    
    ScopePermissionRepresentation representation = new ScopePermissionRepresentation();
    representation.setDecisionStrategy(DecisionStrategy.UNANIMOUS);
    representation.setLogic(Logic.POSITIVE);
    representation.setName(name);
    representation.setType("group");
    representation.setResources(Collections.singleton(resourceId.toString()));
    representation.setScopes(Collections.singleton(scope.getName()));
    representation.setPolicies(policyIds.stream().map(UUID::toString).collect(Collectors.toSet()));
    Response response = scopeResource.create(representation);
    
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
  }
  
  /**
   * Updates groups and group policies into Keycloak
   * 
   * @param keycloak admin client
   * @param realmName realm name
   * @param clientId client id
   * @param groupMap groups names
   */
  public void updatePermissionGroups(Keycloak keycloak, String realmName, ClientRepresentation client, List<String> groupNames) {
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
      if (policyRepresentation == null) {
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
   * @param resourceName resource's name
   * @param scopes scopes
   * @return set of user ids
   */
  public Set<UUID> getResourcePermittedUsers(Keycloak keycloak, String realmName, ClientRepresentation client, String resourceName, List<String> scopes) {
    RealmResource realm = keycloak.realm(realmName);
    return getPermittedUsers(realm, client, resourceName, scopes);
  }

  /**
   * Lists group policy ids by group names
   * 
   * @param keycloak Keycloak admin client
   * @param realmName realm name
   * @param client client
   * @param groupNames group names
   * @return list of group policy ids
   */
  public Set<UUID> getPolicyIdsByGroupNames(Keycloak keycloak, String realmName, ClientRepresentation client, List<String> groupNames) {
    RealmResource realm = keycloak.realm(realmName);
    GroupPoliciesResource groupPolicies = realm.clients().get(client.getId()).authorization().policies().group();
    
    return groupNames.stream()
      .map(groupName -> groupPolicies.findByName(groupName))
      .filter(Objects::nonNull)
      .map(GroupPolicyRepresentation::getId)
      .map(UUID::fromString)
      .collect(Collectors.toSet());
  }
  
  /**
   * Returns list of permitted users for a resource with given scopes 
   * 
   * @param realm realm name
   * @param client client
   * @param resourceName resource's name
   * @param scopes scopes
   * @return set of user ids
   */
  private Set<UUID> getPermittedUsers(RealmResource realm, ClientRepresentation client, String resourceName, List<String> scopes) {
    Map<UUID, DecisionEffect> policies = evaluatePolicies(realm, client, resourceName, scopes);
    
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
   * @param resourceName resource's name
   * @param scopes scopes
   * @return map of results where key is user id and value is decision
   */
  private Map<UUID, DecisionEffect> evaluatePolicies(RealmResource realm, ClientRepresentation client, String resourceName, List<String> scopes) {
    Map<UUID, DecisionEffect> result = new HashMap<>();
    int firstResult = 0;
    int maxResults = 10;
    
    while (firstResult < 1000) {
      List<UserRepresentation> users = realm.users().list(firstResult, maxResults);
      
      for (UserRepresentation user : users) {
        String userId = user.getId();
        result.put(UUID.fromString(userId), evaluatePolicy(realm, client, resourceName, userId, scopes));
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
   * @param resourceName resource's name
   * @param userId user's id
   * @param scopes scopes
   * @return decision
   */
  private DecisionEffect evaluatePolicy(RealmResource realm, ClientRepresentation client, String resourceName, String userId, List<String> scopes) {
    PolicyEvaluationRequest evaluationRequest = createEvaluationRequest(client, resourceName, userId, scopes);
    PolicyEvaluationResponse response = realm.clients().get(client.getId()).authorization().policies().evaluate(evaluationRequest);
    return response.getStatus();
  }
  
  /**
   * Creates Keycloak policy evaluation request
   * 
   * @param client client
   * @param resourceName resource name
   * @param userId userId 
   * @param scopes scopes
   * @return created request
   */
  private PolicyEvaluationRequest createEvaluationRequest(ClientRepresentation client, String resourceName, String userId, Collection<String> scopes) {
    Set<ScopeRepresentation> resourceScopes = scopes.stream().map(ScopeRepresentation::new).collect(Collectors.toSet());
    ResourceRepresentation resource = new ResourceRepresentation(resourceName, resourceScopes);

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
  private UUID getCreateResponseId(Response response) {
    if (response.getStatus() != 201) {
      return null;
    }
    
    String location = response.getHeaderString("location");
    
    Pattern pattern = Pattern.compile(".*\\/(.*)$");
    Matcher matcher = pattern.matcher(location);
    
    if (matcher.find()) {
      return UUID.fromString(matcher.group(1));
    }
    
    return null;
  }
}
