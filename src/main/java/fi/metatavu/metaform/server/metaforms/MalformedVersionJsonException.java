package fi.metatavu.metaform.server.metaforms;
/**
 * Exception throw when malformed version data json
 * 
 * @author Tianxing Wu
 */
public class MalformedVersionJsonException extends Exception {


  private static final long serialVersionUID = 6760138346241227829L;

  /**
   * Constructor
   *
   * @param message message
   * @param original original exception
   */
  public MalformedVersionJsonException(String message, Throwable original) {
    super(message, original);
  }

  /**
   * Constructor
   *
   * @param message message
   */
  public MalformedVersionJsonException(String message) {
    super(message);
  }
  
}
