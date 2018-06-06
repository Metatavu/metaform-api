package fi.metatavu.metaform.server.settings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;

import fi.metatavu.metaform.server.persistence.dao.SystemSettingDAO;
import fi.metatavu.metaform.server.persistence.model.SystemSetting;

/**
 * Controller for system settings.
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class SystemSettingController {
  
  @Inject
  private Logger logger;

  @Inject
  private SystemSettingDAO systemSettingDAO;
  
  /**
   * Returns system setting by key or defaultValue if setting is not defined
   * 
   * @param key system setting key
   * @return setting value
   */
  public String getSettingValue(String key, String defaultValue) {
    SystemSetting systemSetting = systemSettingDAO.findByKey(key);
    if (systemSetting != null) {
      return systemSetting.getValue();
    }
    
    return defaultValue;
  }
  
  /**
   * Returns system setting by key or null if setting is not defined
   * 
   * @param key system setting key
   * @return setting value
   */
  public String getSettingValue(String key) {
    return getSettingValue(key, null);
  }
  
  /**
   * Returns comma delimitered system setting by key or defaultValue if setting is not defined
   * 
   * @param key system setting key
   * @return setting value
   */
  public String[] getSettingValues(String key, String[] defaultValue) {
    String value = getSettingValue(key);
    String[] result = StringUtils.split(value, ',');
    if (result == null) {
      return defaultValue;
    }
    
    return result;
  }
  
  /**
   * Returns comma delimitered system setting by key or null if setting is not defined
   * 
   * @param key system setting key
   * @return setting value
   */
  public String[] getSettingValues(String key) {
    return getSettingValues(key, null);
  }
  
  /**
   * Updates system setting value
   * 
   * @param key system setting key
   * @param value new setting value
   */
  public void setSettingValue(String key, String value) {
    SystemSetting systemSetting = systemSettingDAO.findByKey(key);
    if (systemSetting != null) {
      systemSettingDAO.updateValue(systemSetting, value);
    } else {
      createSystemSetting(key, value);
    }
  }
  
  /**
   * Returns system setting by key or null if setting is not defined
   * 
   * @param key system setting key
   * @return setting value
   */
  public Integer getSettingValueInteger(String key) {
    return getSettingValueInteger(key, null);
  }

  /**
   * Returns system setting by key or defaultValue if setting is not defined
   * 
   * @param key system setting key
   * @param defaultValue returned value if setting is not defined
   * @return setting value
   */
  public Integer getSettingValueInteger(String key, Integer defaultValue) {
    SystemSetting systemSetting = systemSettingDAO.findByKey(key);
    if (systemSetting != null) {
      Integer result = NumberUtils.createInteger(systemSetting.getValue());
      if (result != null) {
        return result;
      }
    }
    
    return defaultValue;
  }
  
  /**
   * Returns system setting by key or false if setting is not defined
   * 
   * @param key system setting key
   * @return setting value
   */
  public boolean getSettingValueBoolean(String key) {
    return getSettingValueBoolean(key, false);
  }
  
  /**
   * Returns system setting by key or false if setting is not defined
   * 
   * @param key system setting key
   * @param defaultValue returned value if setting is not defined
   * @return setting value
   */
  public boolean getSettingValueBoolean(String key, Boolean defaultValue) {
    SystemSetting systemSetting = systemSettingDAO.findByKey(key);
    if (systemSetting != null) {
      return BooleanUtils.toBoolean(systemSetting.getValue());
    }
    
    return defaultValue;
  }

  
  /**
   * Creates a new system setting
   * 
   * @param key key
   * @param value value 
   * @return SystemSetting
   */
  public SystemSetting createSystemSetting(String key, String value) {
    return systemSettingDAO.create(UUID.randomUUID(), key, value);
  }
  
  /**
   * Updates an system setting
   * 
   * @param systemSetting setting
   * @param value value 
   * @return SystemSetting
   */
  @SuppressWarnings ("squid:S3457")
  public SystemSetting updateSystemSetting(SystemSetting systemSetting, String value) {
    if (systemSetting == null) {
      logger.error("Unable to update null setting");
      return null;
    }
    
    return systemSettingDAO.updateValue(systemSetting, value);
  }
  
  /**
   * Finds system setting by id
   * 
   * @param id system setting id
   * @return system setting or null if not found
   */
  public SystemSetting findSystemSetting(String id) {
    return systemSettingDAO.findById(id);
  }

  /**
   * Finds system setting by key
   * 
   * @param key key
   * @return system setting or null if not found
   */
  public SystemSetting findSystemSettingByKey(String key) {
    return systemSettingDAO.findByKey(key);
  }

  /**
   * Deletes an system setting
   * 
   * @param systemSetting setting
   */
  public void deleteSystemSetting(SystemSetting systemSetting) {
    systemSettingDAO.delete(systemSetting);
  }
  
  /**
   * Returns whether system setting has a value set or not
   * 
   * @param key key
   * @return whether system setting has a value set or not
   **/
  public boolean hasSettingValue(String key) {
    return StringUtils.isNotBlank(getSettingValue(key));
  }

  /**
   * Returns system settings as key - value pairs where keys start with prefix
   * 
   * @param prefix prefix
   * @return Returns system settings as key - value pairs
   */
  public Map<String, String> getSettingsWithPrefix(String prefix) {
    Map<String, String> result = new HashMap<>();
    
    List<SystemSetting> systemSettings = systemSettingDAO.listAll();
    for (SystemSetting systemSetting : systemSettings) {
      if (StringUtils.startsWith(systemSetting.getKey(), prefix)) {
        result.put(systemSetting.getKey(), systemSetting.getValue());
      }
    }
    
    return result;
  }
  
}
