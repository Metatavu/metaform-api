package fi.metatavu.metaform.server.script;
import java.util.Base64;
import javax.enterprise.context.ApplicationScoped;

/**
 * Encoding services
 * 
 * @author Heikki Kurhinen
 */
@ApplicationScoped
public class EncodingServices {

  /**
   * Base64 encodes data
   * 
   * @param data data to encode
   */
  public String base64Encode(byte[] data) {
    return Base64.getEncoder().encodeToString(data);
  }
  
}