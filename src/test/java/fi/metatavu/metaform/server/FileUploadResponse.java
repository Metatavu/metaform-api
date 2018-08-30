package fi.metatavu.metaform.server;

import java.util.UUID;

/**
 * Model for file upload response
 * 
 * @author Antti Leppä
 */
public class FileUploadResponse {

  private UUID fileRef;
  private String fileName;

  /**
   * @return the fileName
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * @param fileName the fileName to set
   */
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  /**
   * @return the fileRef
   */
  public UUID getFileRef() {
    return fileRef;
  }

  /**
   * @param fileRef the fileRef to set
   */
  public void setFileRef(UUID fileRef) {
    this.fileRef = fileRef;
  }

} 