package fi.metatavu.metaform.server.files;

/**
 * Class describing meta data of a uploaded file
 * 
 * @author Antti Leppä
 */
public class FileMeta {

  private String contentType;
  private String fileName;
  
  /**
   * Single argument constructor
   */
  public FileMeta() {
    // Empty
  }
  
  /**
   * Constructor
   * 
   * @param contentType file's content type
   * @param fileName file's original name
   */
  public FileMeta(String contentType, String fileName) {
    super();
    this.contentType = contentType;
    this.fileName = fileName;
  }

  public String getContentType() {
    return contentType;
  }
  
  public void setContentType(String contentType) {
    this.contentType = contentType;
  }
  
  public String getFileName() {
    return fileName;
  }
  
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

}

