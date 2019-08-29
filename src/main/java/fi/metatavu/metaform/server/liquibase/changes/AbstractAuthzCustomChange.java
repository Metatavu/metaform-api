package fi.metatavu.metaform.server.liquibase.changes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ws.rs.BadRequestException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.Configuration;
import org.keycloak.authorization.client.util.HttpResponseException;
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
  
  /**
   * Resolves Keycloak error message from exception
   * 
   * @param e exception
   * @return error message
   */
  protected String getKeycloakErrorMessage(Throwable e) {
    HttpResponseException httpResponseException = unwrapHttpException(e);
    if (httpResponseException != null) {
      String message = new String(httpResponseException.getBytes());
      if (StringUtils.isBlank(message)) {
        message = httpResponseException.getMessage();
      }
      
      if (StringUtils.isBlank(message)) {
        message = httpResponseException.getReasonPhrase();
      }
      
      if (StringUtils.isNotBlank(message)) {
       return message;
      }
      
      return  "Unknown error";
    }
    
    BadRequestException badRequestException = unwrapBadRequestException(e);
    if (badRequestException != null) {
      InputStream body = (InputStream) badRequestException.getResponse().getEntity();
      String message = toString(body);
      if (StringUtils.isBlank(message)) {
        message = badRequestException.getMessage();
      }
      
      if (StringUtils.isNotBlank(message)) {
       return message;
      }
    }

    return "Unknown error";
  }

  /**
   * Gets the contents of an as a String
   * 
   * @param inputStream
   * @return string
   */
  private String toString(InputStream inputStream) {
    try {
      return IOUtils.toString(inputStream, "UTF-8");
    } catch (IOException e) {
      // Just eat IO exceptions
    }
    
    return null;
  }

  /**
   * Unwraps HttpResponseException from Keycloak
   * 
   * @param e Exception
   * @return unwrapped exception
   */
  protected HttpResponseException unwrapHttpException(Throwable e) {
    if (e == null) {
      return null;
    }
    
    if (e instanceof HttpResponseException) {
      return (HttpResponseException) e;
    }
    
    return unwrapHttpException(e.getCause());
  }
  
  /**
   * Unwraps HttpResponseException from Keycloak
   * 
   * @param e Exception
   * @return unwrapped exception
   */
  protected BadRequestException unwrapBadRequestException(Throwable e) {
    if (e == null) {
      return null;
    }
    
    if (e instanceof BadRequestException) {
      return (BadRequestException) e;
    }
    
    return unwrapBadRequestException(e.getCause());
  }

  /**
   * Converts UUID into bytes
   * 
   * @param uuid UUID
   * @return bytes
   */
  protected byte[] getUUIDBytes(UUID uuid) {
    byte[] result = new byte[16];
    ByteBuffer.wrap(result).order(ByteOrder.BIG_ENDIAN).putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits());
    return result;
  }
}
