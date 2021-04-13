package fi.metatavu.metaform.test.functional.tests;

import fi.metatavu.metaform.test.functional.AbstractIntegrationTest;
import fi.metatavu.metaform.test.functional.FileUploadMeta;
import fi.metatavu.metaform.test.functional.FileUploadResponse;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;

@QuarkusTest
@TestProfile(DefTestProfile.class)
public class UploadTestsIT extends AbstractIntegrationTest {

  @Test
  public void findUploadedTest() throws IOException {
    FileUploadResponse fileUpload = uploadResourceFile("test-image-480-320.jpg");
    Assertions.assertNotNull(fileUpload);
    assertUploadFound(fileUpload.getFileRef().toString());
  }

  @Test
  public void findUploadedMeta() throws IOException {
    FileUploadResponse fileUpload = uploadResourceFile("test-image-480-320.jpg");
    Assertions.assertNotNull(fileUpload);
    FileUploadMeta meta = getFileRefMeta(fileUpload.getFileRef());

    Assertions.assertNotNull(meta);
    Assertions.assertEquals("test-image-480-320.jpg", meta.getFileName());
    Assertions.assertEquals("image/jpg", meta.getContentType());
  }

  @Test
  public void deleteUploadedTest() throws IOException, URISyntaxException {
    FileUploadResponse fileUpload = uploadResourceFile("test-image-480-320.jpg");
    Assertions.assertNotNull(fileUpload);
    String fileRef = fileUpload.getFileRef().toString();
    assertUploadFound(fileRef);
    deleteUpload(fileRef);
    assertUploadNotFound(fileRef);
  }
}
