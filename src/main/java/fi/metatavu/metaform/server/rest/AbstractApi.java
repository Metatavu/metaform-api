package fi.metatavu.metaform.server.rest;

import fi.metatavu.metaform.server.rest.model.BadRequest;
import fi.metatavu.metaform.server.rest.model.Forbidden;
import fi.metatavu.metaform.server.rest.model.InternalServerError;
import fi.metatavu.metaform.server.rest.model.NotImplemented;
import fi.metatavu.metaform.server.rest.model.NotFound;

import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessToken.Access;

/**
 * Abstract base class for all API services
 * 
 * @author Antti Leppä
 */
public abstract class AbstractApi {
  
  protected static final String ADMIN_ROLE = "metaform-admin";
  protected static final String VIEW_ALL_REPLIES_ROLE = "metaform-view-all-replies";
  
  
  /**
   * Return current HttpServletRequest
   * 
   * @return current http servlet request
   */
  protected HttpServletRequest getHttpServletRequest() {
    return ResteasyProviderFactory.getContextData(HttpServletRequest.class);
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
   * Returns whether logged user is realm Metaform admin
   * 
   * @return whether logged user is realm Metaform admin
   */
  protected boolean isRealmMetaformAdmin() {
    return hasRealmRole(ADMIN_ROLE);
  }
  
  /**
   * Returns whether logged user has at least one of specified realm roles
   * 
   * @param role role
   * @return whether logged user has specified realm role or not
   */
  protected boolean hasRealmRole(String... roles) {
    HttpServletRequest request = getHttpServletRequest();
    Principal userPrincipal = request.getUserPrincipal();
    KeycloakPrincipal<?> kcPrincipal = (KeycloakPrincipal<?>) userPrincipal;
    KeycloakSecurityContext keycloakSecurityContext = kcPrincipal.getKeycloakSecurityContext();
    AccessToken token = keycloakSecurityContext.getToken();
    Access realmAccess = token.getRealmAccess();
    
    for (int i = 0; i < roles.length; i++) {
      if (realmAccess.isUserInRole(roles[i])) {
        return true;
      }
    }
    
    return false;
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
  
}

