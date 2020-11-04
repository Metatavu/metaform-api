package fi.metatavu.metaform.server.settings;

import javax.enterprise.context.ApplicationScoped;

/**
 * Controller for system settings.
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class SystemSettingController {
  
  /**
   * Returns whether the system is running in test mode or not
   * 
   * @return whether the system is running in test mode or not
   */
  public boolean inTestMode() {
    return "TEST".equals(System.getProperty("runmode"));
  }
  
}
