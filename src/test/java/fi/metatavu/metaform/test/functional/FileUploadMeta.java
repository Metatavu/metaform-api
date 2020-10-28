package fi.metatavu.metaform.test.functional;

/**
 * Model for uploaded file meta
 * 
 * @author Antti Leppä
 */
public class FileUploadMeta {

  private String contentType;
  private String fileName;

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getContentType() {
    return contentType;
  }
  
  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

} 