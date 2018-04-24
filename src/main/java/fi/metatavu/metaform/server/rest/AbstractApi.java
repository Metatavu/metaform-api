package fi.metatavu.metaform.server.rest;

import fi.metatavu.metaform.server.rest.model.BadRequest;
import fi.metatavu.metaform.server.rest.model.Forbidden;
import fi.metatavu.metaform.server.rest.model.InternalServerError;
import fi.metatavu.metaform.server.rest.model.NotImplemented;
import fi.metatavu.metaform.server.rest.model.NotFound;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * Abstract base class for all API services
 * 
 * @author Antti Leppä
 */
public abstract class AbstractApi {
  
  /**
   * Returns logged user id
   * 
   * @return logged user id
   */
  protected UUID getLoggerUserId() {
    HttpServletRequest httpServletRequest = ResteasyProviderFactory.getContextData(HttpServletRequest.class);
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
      .status(Response.Status.BAD_REQUEST)
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

}

