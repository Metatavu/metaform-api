package fi.metatavu.metaform.server.keycloak;

/**
 * Enumeration for authorization scopes
 * 
 * @author Antti Leppä
 */
public enum AuthorizationScope {
  
  /**
   * Authorization scope for viewing a reply
   */
  REPLY_VIEW ("reply:view"),
  
  /**
   * Authorization scope for editing a reply
   */
  REPLY_EDIT ("reply:edit"),
  
  /**
   * Authorization scope for receiving a notification about reply
   */
  REPLY_NOTIFY ("reply:notify");
  
  private String name;
  
  private AuthorizationScope(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
  
}
