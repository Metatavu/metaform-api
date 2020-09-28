package fi.metatavu.metaform.server.xlsx;
/**
 * Exception throw when XLSX generation fails
 * 
 * @author Antti Leppä
 */
public class XlsxException extends Exception {

  private static final long serialVersionUID = 8611634313163534478L;

  /**
   * Constructor
   * 
   * @param message message
   * @param original original exception
   */
  public XlsxException(String message, Throwable original) {
    super(message, original);
  }
  
  /**
   * Constructor
   * 
   * @param message message
   */
  public XlsxException(String message) {
    super(message);
  }
  
}
