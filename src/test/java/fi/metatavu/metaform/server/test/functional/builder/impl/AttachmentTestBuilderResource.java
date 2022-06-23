package fi.metatavu.metaform.server.test.functional.builder.impl;

import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder;
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider;
import fi.metatavu.metaform.api.client.apis.AttachmentsApi;
import fi.metatavu.metaform.api.client.infrastructure.ApiClient;
import fi.metatavu.metaform.api.client.infrastructure.ClientException;
import fi.metatavu.metaform.api.client.models.Attachment;
import fi.metatavu.metaform.server.test.functional.FileUploadResponse;
import fi.metatavu.metaform.server.test.TestSettings;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test builder resource for Attachments API
 */
public class AttachmentTestBuilderResource extends ApiTestBuilderResource<Attachment, AttachmentsApi> {

  private final AccessTokenProvider accessTokenProvider;

  public AttachmentTestBuilderResource(
    AbstractTestBuilder<ApiClient> testBuilder,
    AccessTokenProvider accessTokenProvider,
    ApiClient apiClient) {
    super(testBuilder, apiClient);
    this.accessTokenProvider = accessTokenProvider;
  }

  @Override
  protected AttachmentsApi getApi() {
    try {
      ApiClient.Companion.setAccessToken(accessTokenProvider.getAccessToken());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return new AttachmentsApi(TestSettings.basePath);
  }

  @Override
  public void clean(Attachment attachment) {

  }

  /**
   * Asserts that attachment exists
   *
   * @param fileUpload1 response
   */
  public void assertAttachmentExists(FileUploadResponse fileUpload1) throws IOException {
    Attachment attachment1 = getApi().findAttachment(fileUpload1.getFileRef(), "");
    Assertions.assertNotNull(attachment1);
    Assertions.assertEquals(fileUpload1.getFileRef(), attachment1.getId());
  }

  /**
   * Assert that attachment search returns 404
   *
   * @param fileRef file ref
   */
  public void assertAttachmentNotFound(UUID fileRef) throws IOException {
    try {
      getApi().findAttachment(fileRef, "");
      fail(String.format("Expected find to fail with status %d", 404));
    } catch (ClientException e) {
      assertEquals(404, e.getStatusCode());
    }
  }
}
