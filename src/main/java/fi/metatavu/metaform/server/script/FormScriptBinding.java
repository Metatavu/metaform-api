package fi.metatavu.metaform.server.script;

import fi.metatavu.metaform.api.spec.model.Reply;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Script bindings entrypoint 
 *
 * @author Antti Leppä
 */
@ApplicationScoped
public class FormScriptBinding {
  
  @Inject
  private FormRuntimeContext formRuntimeContext;

  @Inject
  private PdfServices pdfServices;

  @Inject
  private XlsxServices xlsxServices;

  @Inject
  private EncodingServices encodingServices;
  
  /**
   * Returns reply object
   * 
   * @return reply object
   */
  public Reply getReply() {
    return formRuntimeContext.getReply();
  }
  
  /**
   * Returns reply data
   * 
   * @return reply data
   */
  public Map<String, Object> getReplyData() {
   return getReply().getData();
  }
  
  /**
   * Sets variable value
   * 
   * @param name variable name
   * @param value variable value
   */
  public void setVariableValue(String name, Object value) {
    formRuntimeContext.setVariableValue(name, value);
  }
  
  /**
   * Returns variable value
   * 
   * @param name variable name
   * @return variable value
   */
  public Object getVariableValue(String name) {
    return formRuntimeContext.getVariableValue(name);
  }
  
  /**
   * Returns PDF services
   * 
   * @return PDF services
   */
  public PdfServices getPdfServices() {
    return pdfServices;
  }

  /**
   * Returns encoding services
   * 
   * @return the encodingServices
   */
  public EncodingServices getEncodingServices() {
    return encodingServices;
  }
  
  /**
   * Returns XLSX services
   * 
   * @return XLSX services
   */
  public XlsxServices getXlsxServices() {
    return xlsxServices;
  }
    
}
