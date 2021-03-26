package fi.metatavu.metaform.server.files;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Controller for file functions
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class FileController {
  
  @Inject
  private Logger logger;

  //todo add correct quarkus caching
  private Map<String, byte[]> dataCache = new HashMap<>();

  private Map<String, String> metaCache = new HashMap<>();
  
  /**
   * Stores file and returns reference id
   * 
   * @param inputStream input stream
   * @return reference id
   */
  public String storeFile(String contentType, String fileName, InputStream inputStream) {
    String fileRef = UUID.randomUUID().toString();
    
    try {
      dataCache.put(fileRef, IOUtils.toByteArray(inputStream));
      metaCache.put(fileRef, getObjectMapper().writeValueAsString(new FileMeta(contentType, fileName)));
      return fileRef;
    } catch (IOException e) {
      logger.error("Failed to store file", e);
    }
    
    return null;
  }
  
  /**
   * Returns a file data
   * 
   * @param fileRef file reference id
   * @return file data or null if not found
   */
  public File getFileData(String fileRef) {
    if (StringUtils.isEmpty(fileRef)) {
      return null;
    }
    
    byte[] data = dataCache.get(fileRef);
    String metaData = metaCache.get(fileRef);

    if (!dataCache.containsKey(fileRef) || !metaCache.containsKey(fileRef)) {
      return null;
    }
    
    return createFile(fileRef, data, metaData);
  }
  
  /**
   * Returns meta data for a file or null if file does not exist
   * 
   * @param fileRef file reference id
   * @return meta data
   * @throws IOException
   */
  public FileMeta getFileMeta(String fileRef) {
    String metaData = getRawFileMeta(fileRef);
    if (StringUtils.isBlank(metaData)) {
      return null;
    }
    
    try {
      return getObjectMapper().readValue(metaData, FileMeta.class);
    } catch (IOException e) {
      logger.error("Failed to retrieve file meta", e);
    } 
    
    return null;
  }
  
  /**
   * Returns raw meta data for a file or null if file does not exist
   * 
   * @param fileRef file reference id
   * @return meta data as JSON string
   * @throws IOException
   */
  public String getRawFileMeta(String fileRef) {
    return metaCache.get(fileRef);
  }
  
  /**
   * Returns a file data and removes it from the store
   * 
   * @param fileRef file reference id
   * @return file data or null if not found
   */
  public File popFileData(String fileRef) {
    if (StringUtils.isEmpty(fileRef)) {
      return null;
    }
    
    if (!dataCache.containsKey(fileRef) || !metaCache.containsKey(fileRef)) {
      return null;
    }
    
    byte[] data = dataCache.remove(fileRef);
    String metaData = metaCache.remove(fileRef);
    
    return createFile(fileRef, data, metaData);
  }
  
  /**
   * Deletes file from store
   * 
   * @param fileRef file ref
   */
  public void deleteFile(String fileRef) {
    dataCache.remove(fileRef);
    metaCache.remove(fileRef);
  }

  private File createFile(String fileRef, byte[] data, String metaData) {
    if (data != null && metaData != null) {
      try {
        FileMeta fileMeta = getObjectMapper().readValue(metaData, FileMeta.class);
        return new File(fileMeta, data);
      } catch (IOException e) {
        logger.error(String.format("Failed to unserialize file meta for fileRef %s", fileRef), e);
      }
    }
    
    return null;
  }
  
  private ObjectMapper getObjectMapper() {
    return new ObjectMapper();
  }

}