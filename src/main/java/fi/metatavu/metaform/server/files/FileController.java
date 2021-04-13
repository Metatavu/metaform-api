package fi.metatavu.metaform.server.files;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.UTF8;
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

  private final String DATA_PREFIX = "/tmp/data-";
  private final String META_PREFIX = "/tmp/meta-";
  private Map<String, byte[]> dataCache = new HashMap<>();

  private Map<String, String> metaCache = new HashMap<>();

  /**
   * Persists file
   *
   * @param path path
   * @param data data
   * @throws IOException
   */
  private void persistFile(String path, byte[] data) throws IOException {
    Path dataFile = Files.createFile(Path.of(path));

    try (FileOutputStream outputStream = new FileOutputStream(dataFile.toFile())) {
      outputStream.write(data);
    }
  }

  /**
   * Reads file into byte array
   *
   * @param path path to file
   * @return file data
   */
  private byte[] readFileData(Path path) {
    if (Files.exists(path)) {
      try ( FileInputStream fileInputStream = new FileInputStream(path.toFile())) {
        return IOUtils.toByteArray(fileInputStream);
      }
      catch (Exception e) {
        return null;
      }
    }
    return null;
  }

  /**
   * Stores file and returns reference id
   * 
   * @param inputStream input stream
   * @return reference id
   */
  public String storeFile(String contentType, String fileName, InputStream inputStream) {
    String fileRef = UUID.randomUUID().toString();
    
    try {
      persistFile(DATA_PREFIX+fileRef, IOUtils.toByteArray(inputStream));
      persistFile(META_PREFIX+fileRef, getObjectMapper().writeValueAsString(new FileMeta(contentType, fileName)).getBytes());
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
    Path dataPath = Path.of(DATA_PREFIX+ fileRef);
    Path metaPath = Path.of(META_PREFIX+ fileRef);

    if (Files.notExists(dataPath) || Files.notExists(metaPath)) {
      return null;
    }

    byte[] data = readFileData(dataPath);
    String metaData = new String(readFileData(metaPath));
    return createFile(fileRef, data, metaData);
  }
  
  /**
   * Returns meta data for a file or null if file does not exist
   * 
   * @param fileRef file reference id
   * @return meta data
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
   */
  public String getRawFileMeta(String fileRef) {
    try {
      byte[] bytes = readFileData(Path.of(META_PREFIX + fileRef));
      if (bytes != null) {
        return IOUtils.toString(bytes, "UTF8");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
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

    File fileData = getFileData(fileRef);
    deleteFile(fileRef);
    return fileData;
  }
  
  /**
   * Deletes file from store
   * 
   * @param fileRef file ref
   */
  public void deleteFile(String fileRef) {
    try {
      Files.delete(Path.of(DATA_PREFIX+fileRef));
      Files.delete(Path.of(META_PREFIX+fileRef));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Creates file from data and metadata
   *
   * @param fileRef file ref
   * @param data byte array data
   * @param metaData metadata
   * @return new file
   */
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