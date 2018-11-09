package fi.metatavu.metaform.server.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.UUID;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.ClientAuthorizationContext;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessToken.Access;
import org.slf4j.Logger;

import fi.metatavu.metaform.server.rest.model.BadRequest;
import fi.metatavu.metaform.server.rest.model.Forbidden;
import fi.metatavu.metaform.server.rest.model.InternalServerError;
import fi.metatavu.metaform.server.rest.model.NotFound;
import fi.metatavu.metaform.server.rest.model.NotImplemented;

/**
 * Abstract base class for all API services
 * 
 * @author Antti Leppä
 */
public abstract class AbstractApi {

  protected static final String USER_ROLE = "user";
  protected static final String ADMIN_ROLE = "metaform-admin";
  protected static final String SUPER_ROLE = "metaform-super";
  protected static final String VIEW_ALL_REPLIES_ROLE = "metaform-view-all-replies";
  protected static final String NOT_FOUND_MESSAGE = "Not found";
  protected static final String UNAUTHORIZED = "Unauthorized";
  
  private static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
  private static final String FAILED_TO_STREAM_DATA_TO_CLIENT = "Failed to stream data to client";
  private static final String UNSUPPORTED_ENCODING = "Unsupported encoding";

  @Inject
  private Logger logger;

  /**
   * Return current HttpServletRequest
   * 
   * @return current http servlet request
   */
  protected HttpServletRequest getHttpServletRequest() {
    return ResteasyProviderFactory.getContextData(HttpServletRequest.class);
  }
  
  /**
   * Returns request locale
   * 
   * @return request locale
   */
  protected Locale getLocale() {
    return getHttpServletRequest().getLocale();
  }
  
  /**
   * Returns logged user id
   * 
   * @return logged user id
   */
  protected UUID getLoggerUserId() {
    HttpServletRequest httpServletRequest = getHttpServletRequest();
    String remoteUser = httpServletRequest.getRemoteUser();
    if (remoteUser == null) {
      return null;
    }
    
    return UUID.fromString(remoteUser);
  }
  
  /**
   * Constructs ok response
   * 
   * @param entity payload
   * @return response
   */
  protected Response createOk(Object entity) {
    return Response
      .status(Response.Status.OK)
      .entity(entity)
      .build();
  }

  /**
   * Constructs no content response
   * 
   * @return response
   */
  protected Response createNoContent() {
    return Response
      .status(Response.Status.NO_CONTENT)
      .build();
  }

  /**
   * Constructs bad request response
   * 
   * @param message message
   * @return response
   */
  protected Response createBadRequest(String message) {
    BadRequest entity = new BadRequest();
    entity.setCode(Response.Status.BAD_REQUEST.getStatusCode());
    entity.setMessage(message);
    return Response
      .status(Response.Status.BAD_REQUEST)
      .entity(entity)
      .build();
  }

  /**
   * Constructs not found response
   * 
   * @param message message
   * @return response
   */
  protected Response createNotFound(String message) {
    NotFound entity = new NotFound();
    entity.setCode(Response.Status.NOT_FOUND.getStatusCode());
    entity.setMessage(message);
    return Response
      .status(Response.Status.NOT_FOUND)
      .entity(entity)
      .build();
  }

  /**
   * Constructs not implemented response
   * 
   * @param message message
   * @return response
   */
  protected Response createNotImplemented(String message) {
    NotImplemented entity = new NotImplemented();
    entity.setCode(Response.Status.NOT_IMPLEMENTED.getStatusCode());
    entity.setMessage(message);
    return Response
      .status(Response.Status.NOT_IMPLEMENTED)
      .entity(entity)
      .build();
  }

  /**
   * Constructs internal server error response
   * 
   * @param message message
   * @return response
   */
  protected Response createInternalServerError(String message) {
    InternalServerError entity = new InternalServerError();
    entity.setCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    entity.setMessage(message);
    return Response
      .status(Response.Status.INTERNAL_SERVER_ERROR)
      .entity(entity)
      .build();
  }

  /**
   * Constructs forbidden response
   * 
   * @param message message
   * @return response
   */
  protected Response createForbidden(String message) {
    Forbidden entity = new Forbidden();
    entity.setCode(Response.Status.FORBIDDEN.getStatusCode());
    entity.setMessage(message);
    return Response
      .status(Response.Status.FORBIDDEN)
      .entity(entity)
      .build();
  }

  /**
   * Creates streamed response from string using a UTF-8 encoding
   * 
   * @param data data
   * @param type content type
   * @return Response
   */
  public Response streamResponse(String data, String type) {
    return streamResponse(data, "UTF-8", type);
  }
  
  /**
   * Creates streamed response from string using specified encoding
   * 
   * @param data data
   * @param type content type
   * @return Response
   */
  public Response streamResponse(String data, String charsetName, String type) {
    try {
      return streamResponse(data.getBytes(charsetName), type);
    } catch (UnsupportedEncodingException e) {
      logger.error(UNSUPPORTED_ENCODING, e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
        .entity(INTERNAL_SERVER_ERROR)
        .build();
    }
  }
  
  /**
   * Creates streamed response from byte array
   * 
   * @param data data
   * @param type content type
   * @return Response
   */
  public Response streamResponse(byte[] data, String type) {
    try (InputStream byteStream = new ByteArrayInputStream(data)) {
      return streamResponse(type, byteStream, data.length);
    } catch (IOException e) {
      logger.error(FAILED_TO_STREAM_DATA_TO_CLIENT, e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
        .entity(INTERNAL_SERVER_ERROR)
        .build();
    }
  }

  /**
   * Creates streamed response from input stream
   * 
   * @param inputStream data
   * @param type content type
   * @param contentLength content length
   * @return Response
   */
  public Response streamResponse(String type, InputStream inputStream, int contentLength) {
    return Response.ok(new StreamingOutputImpl(inputStream), type)
      .header("Content-Length", contentLength)
      .build();
  }

  /**
   * Returns whether logged user is realm user
   * 
   * @return whether logged user is realm user
   */
  protected boolean isRealmUser() {
    return hasRealmRole(USER_ROLE);
  }

  /**
   * Returns whether logged user is realm Metaform admin
   * 
   * @return whether logged user is realm Metaform admin
   */
  protected boolean isRealmMetaformAdmin() {
    return hasRealmRole(ADMIN_ROLE);
  }

  /**
   * Returns whether logged user is realm Metaform super
   * 
   * @return whether logged user is realm Metaform super
   */
  protected boolean isRealmMetaformSuper() {
    return hasRealmRole(SUPER_ROLE);
  }
  
  /**
   * Returns whether logged user has at least one of specified realm roles
   * 
   * @param role role
   * @return whether logged user has specified realm role or not
   */
  protected boolean hasRealmRole(String... roles) {
    AccessToken token = getAccessToken();
    if (token == null) {
      return false;
    }
    
    Access realmAccess = token.getRealmAccess();
    if (realmAccess == null) {
      return false;
    }
    
    for (int i = 0; i < roles.length; i++) {
      if (realmAccess.isUserInRole(roles[i])) {
        return true;
      }
    }
    
    return false;
  }
  
  /**
   * Returns access token
   * 
   * @return access token
   */
  protected AccessToken getAccessToken() {
    KeycloakSecurityContext keycloakSecurityContext = getSecurityContext();
    if (keycloakSecurityContext == null) {
      return null;
    }
    
    return keycloakSecurityContext.getToken();
  }
  
  /**
   * Returns access token as string
   * 
   * @return access token as string
   */
  protected String getTokenString() {
    KeycloakSecurityContext keycloakSecurityContext = getSecurityContext();
    if (keycloakSecurityContext == null) {
      return null;
    }
    
    return keycloakSecurityContext.getTokenString();
  }

  /**
   * Returns Keycloak security context
   * 
   * @return Keycloak security context
   */
  private KeycloakSecurityContext getSecurityContext() {
    HttpServletRequest request = getHttpServletRequest();
    Principal userPrincipal = request.getUserPrincipal();
    KeycloakPrincipal<?> kcPrincipal = (KeycloakPrincipal<?>) userPrincipal;
    if (kcPrincipal == null) {
      return null;
    }
    
    return kcPrincipal.getKeycloakSecurityContext();
  }

  /**
   * Parses date time from string
   * 
   * @param timeString
   * @return
   */
  protected OffsetDateTime parseTime(String timeString) {
    if (StringUtils.isEmpty(timeString)) {
      return null;
    }
    
    return OffsetDateTime.parse(timeString);
  }
  
  /**
   * Constructs authz client
   * 
   * @return created authz client
   */
  protected AuthzClient getAuthzClient() {
    ClientAuthorizationContext clientAuthorizationContext = getAuthorizationContext();
    if (clientAuthorizationContext == null) {
      return null;
    }

    return clientAuthorizationContext.getClient();
  }

  /**
   * Return Keycloak authorization client context or null if not available 
   * 
   * @return Keycloak authorization client
   */
  protected ClientAuthorizationContext getAuthorizationContext() {
    KeycloakSecurityContext keycloakSecurityContext = getKeycloakSecurityContext();
    if (keycloakSecurityContext == null) {
      return null;
    }

    return (ClientAuthorizationContext) keycloakSecurityContext.getAuthorizationContext();
  }

  /**
   * Returns Keycloak security context from request or null if not available
   * 
   * @return Keycloak security context
   */
  protected KeycloakSecurityContext getKeycloakSecurityContext() {
    HttpServletRequest request = getHttpServletRequest();
    Principal userPrincipal = request.getUserPrincipal();
    KeycloakPrincipal<?> kcPrincipal = (KeycloakPrincipal<?>) userPrincipal;
    if (kcPrincipal == null) {
      return null;
    }
    
    return kcPrincipal.getKeycloakSecurityContext();
  }
  
}

