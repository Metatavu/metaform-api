package fi.metatavu.metaform.server.pdf;

/**
 * Exception throw when PDF generation fails
 * 
 * @author Antti Leppä
 */
public class PdfRenderException extends Exception {

  private static final long serialVersionUID = -835234745158422224L;

  public PdfRenderException(String message, Throwable original) {
    super(message, original);
  }
  
  public PdfRenderException(String message) {
    super(message);
  }
  
}
