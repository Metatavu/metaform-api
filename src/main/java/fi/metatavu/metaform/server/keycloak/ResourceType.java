package fi.metatavu.metaform.server.keycloak;

/**
 * Enumeration for authorization resource types
 * 
 * @author Antti Leppä
 */
public enum ResourceType {
  
  /**
   * Authorization resource for a reply
   */
  REPLY ("urn:metaform:resources:reply");
  
  private String urn;
  
  private ResourceType(String name) {
    this.urn = name;
  }
  
  public String getUrn() {
    return urn;
  }
  
}
