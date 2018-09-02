package fi.metatavu.metaform.server;

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
  public void deleteUploadedTest() throws IOException, URISyntaxException {
    FileUploadResponse fileUpload = uploadResourceFile("test-image-480-320.jpg");
    assertNotNull(fileUpload);
    String fileRef = fileUpload.getFileRef().toString();
    assertUploadFound(fileRef);
    deleteUpload(fileRef);
    assertUploadNotFound(fileRef);
  }
  
}
