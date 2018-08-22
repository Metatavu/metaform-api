package fi.metatavu.metaform.server.files;

/**
 * Class representing a file uploaded into the system but not yet persisted into the database
 * 
 * @author Antti Leppä
 */
public class File {

  private FileMeta meta;
  private byte[] data;

  /**
   * Constructor
   * 
   * @param meta file meta
   * @param data file data
   */
  public File(FileMeta meta, byte[] data) {
    super();
    this.meta = meta;
    this.data = data;
  }
  
  public byte[] getData() {
    return data;
  }

  public FileMeta getMeta() {
    return meta;
  }

}
