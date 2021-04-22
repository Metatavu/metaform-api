package fi.metatavu.metaform.server.settings;

import org.eclipse.microprofile.config.ConfigProvider;

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
    return "TEST".equals(ConfigProvider.getConfig().getValue("runmode", String.class));
  }
  
}
