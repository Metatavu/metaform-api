package fi.metatavu.metaform.server.rest;

/**
 * Enumeration that decribes reply mode
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
public enum ReplyMode {
  
  /**
   * Updates existing reply if one exists, otherwise create new
   */
  UPDATE,
  
  /**
   * Revision existing reply and create new one
   */
  REVISION,
  
  /**
   * Always create new reply 
   */
  CUMULATIVE

}
