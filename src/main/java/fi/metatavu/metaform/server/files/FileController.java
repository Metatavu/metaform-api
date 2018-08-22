package fi.metatavu.metaform.server.files;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.infinispan.Cache;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Controller for file functions
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
@Singleton
public class FileController {
  
  @Inject
  private Logger logger;
  
  @Resource(lookup = "java:jboss/infinispan/cache/metaform/file-data")
  private Cache<String, byte[]> dataCache;

  @Resource(lookup = "java:jboss/infinispan/cache/metaform/file-meta")
  private Cache<String, String> metaCache;
  
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