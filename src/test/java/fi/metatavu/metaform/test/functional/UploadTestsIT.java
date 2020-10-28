package fi.metatavu.metaform.test.functional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Test;

@SuppressWarnings ("squid:S1192")
public class UploadTestsIT extends AbstractIntegrationTest {
  
  @Test
  public void findUploadedTest() throws IOException, URISyntaxException {
    FileUploadResponse fileUpload = uploadResourceFile("test-image-480-320.jpg");
    assertNotNull(fileUpload);
    assertUploadFound(fileUpload.getFileRef().toString());
  }
  
  @Test
  public void findUploadedMeta() throws IOException {
    FileUploadResponse fileUpload = uploadResourceFile("test-image-480-320.jpg");
    assertNotNull(fileUpload);
    FileUploadMeta meta = getFileRefMeta(fileUpload.getFileRef());
    
    assertNotNull(meta);
    assertEquals("test-image-480-320.jpg", meta.getFileName());
    assertEquals("image/jpg", meta.getContentType());
  }
  
  @Test
  public void deleteUploadedTest() throws IOException, URISyntaxException {
    FileUploadResponse fileUpload = uploadResourceFile("test-image-480-320.jpg");
    assertNotNull(fileUpload);
    String fileRef = fileUpload.getFileRef().toString();
    assertUploadFound(fileRef);
    deleteUpload(fileRef);
    assertUploadNotFound(fileRef);
  }
  
}
