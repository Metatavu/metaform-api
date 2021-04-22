package fi.metatavu.metaform.server.script;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;

import fi.metatavu.metaform.api.spec.model.Attachment;
import fi.metatavu.metaform.api.spec.model.Metaform;
import fi.metatavu.metaform.api.spec.model.Reply;
import fi.metatavu.metaform.server.xlsx.XlsxBuilder;

/**
 * Form runtime context for holding state within single request
 * 
 * @author Antti Leppä
 */
@RequestScoped
public class FormRuntimeContext {

  private UUID loggedUserId;
  private Metaform metaform;
  private Reply reply;
  private XlsxBuilder xlsxBuilder;
  private Map<String, Object> variableValues;
  private Map<String, Attachment> attachmentMap;
  private String exportThemeName;
  private Locale locale;

  /**
   * Post construct method
   */
  @PostConstruct
  public void init() {
    variableValues = new HashMap<>();
  }
  
  /**
   * Returns currently logged user id
   * 
   * @return currently logged user id
   */
  public UUID getLoggedUserId() {
    return loggedUserId;
  }
  
  /**
   * Sets currently logged user id
   * 
   * @param loggedUserId currently logged user id
   */
  public void setLoggedUserId(UUID loggedUserId) {
    this.loggedUserId = loggedUserId;
  }
  
  /**
   * Returns current metaform
   * 
   * @return current metaform
   */
  public Metaform getMetaform() {
    return metaform;
  }
  
  /**
   * Sets current metaform
   * 
   * @param metaform metaform
   */
  public void setMetaform(Metaform metaform) {
    this.metaform = metaform;
  }
  
  /**
   * Returns current reply
   * 
   * @return current reply
   */
  public Reply getReply() {
    return reply;
  }
  
  /**
   * Sets current reply
   * 
   * @param reply current reply
   */
  public void setReply(Reply reply) {
    this.reply = reply;
  }
  
  /**
   * Sets XLSX builder instance
   * 
   * @param xlsxBuilder XLSX builder instance
   */
  public void setXlsxBuilder(XlsxBuilder xlsxBuilder) {
    this.xlsxBuilder = xlsxBuilder;
  }
  
  /**
   * Returns XLSX builder instance. Builder is only available on XLSX report scripts
   * 
   * @return XLSX builder instance
   */
  public XlsxBuilder getXlsxBuilder() {
    return xlsxBuilder;
  }
  
  /**
   * Sets variable value
   * 
   * @param name variable name
   * @param value variable value
   */
  public void setVariableValue(String name, Object value) {
    variableValues.put(name, value != null ? value : null);
  }
  
  /**
   * Returns variable value
   * 
   * @param name variable name
   * @return variable value
   */
  public Object getVariableValue(String name) {
    return variableValues.get(name);
  }
  
  /**
   * Returns attachment map
   * 
   * @return attachment map
   */
  public Map<String, Attachment> getAttachmentMap() {
    return attachmentMap;
  }
  
  /**
   * Sets attachment map
   * 
   * @param attachmentMap attachment map
   */
  public void setAttachmentMap(Map<String, Attachment> attachmentMap) {
    this.attachmentMap = attachmentMap;
  }
  
  /**
   * Returns an attachment by id
   * 
   * @param id attachment id
   * @return attachment
   */
  public Attachment getAttachment(String id) {
    return attachmentMap.get(id);
  }

  /**
   * Returns export theme name or null if not defined
   * 
   * @return export theme name or null if not defined
   */
  public String getExportThemeName() {
    return exportThemeName;
  }
  
  /**
   * Sets export theme name
   * 
   * @param exportThemeName export theme name
   */
  public void setExportThemeName(String exportThemeName) {
    this.exportThemeName = exportThemeName;
  }
  
  /**
   * Returns current locale
   * 
   * @return current locale
   */
  public Locale getLocale() {
    return locale;
  }
  
  /**
   * Sets current locale
   * @param locale current locale
   */
  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  
}
