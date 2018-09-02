package fi.metatavu.metaform.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import feign.FeignException;
import fi.metatavu.metaform.client.Attachment;
import fi.metatavu.metaform.client.AttachmentsApi;
import fi.metatavu.metaform.client.Metaform;
import fi.metatavu.metaform.client.RepliesApi;
import fi.metatavu.metaform.client.Reply;
import fi.metatavu.metaform.client.ReplyData;
import fi.metatavu.metaform.server.rest.ReplyMode;

@SuppressWarnings ("squid:S1192")
public class AttachmentTestsIT extends AbstractIntegrationTest {
  
  @Test
  public void findAttachmentTest() throws IOException, URISyntaxException {
    String accessToken = getAccessToken(REALM_1, "test1.realm1", "test");
    RepliesApi repliesApi = getRepliesApi(accessToken);

    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      Metaform metaform = dataBuilder.createMetaform("files");
      
      AttachmentsApi adminAttachmentsApi = dataBuilder.getAdminAttachmentsApi();
      FileUploadResponse fileUpload = uploadResourceFile("test-image-480-320.jpg");
      
      ReplyData replyData = new ReplyData();
      replyData.put("files", fileUpload.getFileRef());
      
      Reply reply = dataBuilder.createReply(metaform, replyData, ReplyMode.REVISION);
      assertNotNull(reply);
      assertNotNull(reply.getData());
      assertEquals(Arrays.asList(fileUpload.getFileRef().toString()), reply.getData().get("files"));

      Reply foundReply = repliesApi.findReply(REALM_1, metaform.getId(), reply.getId());
      assertNotNull(foundReply);
      assertNotNull(foundReply.getData());
      assertEquals(Arrays.asList(fileUpload.getFileRef().toString()), foundReply.getData().get("files"));
      
      assertAttachmentExists(adminAttachmentsApi, fileUpload);
      
      assertEquals(getResourceMd5("test-image-480-320.jpg"), DigestUtils.md5Hex(getAttachmentData(accessToken, fileUpload.getFileRef())));
    } finally {
      dataBuilder.clean();
    }
  }

  @Test
  public void findMultipleAttachmentsTest() throws IOException, URISyntaxException {
    String accessToken = getAccessToken(REALM_1, "test1.realm1", "test");
    RepliesApi repliesApi = getRepliesApi(accessToken);

    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      Metaform metaform = dataBuilder.createMetaform("files");
      AttachmentsApi adminAttachmentsApi = dataBuilder.getAdminAttachmentsApi();
      FileUploadResponse fileUpload1 = uploadResourceFile("test-image-480-320.jpg");
      FileUploadResponse fileUpload2 = uploadResourceFile("test-image-667-1000.jpg");
      
      String fileRef1 = fileUpload1.getFileRef().toString();
      String fileRef2 = fileUpload2.getFileRef().toString();
      List<String> fileRefs = Arrays.asList(fileRef1, fileRef2);
      
      ReplyData replyData = new ReplyData();
      replyData.put("files", fileRefs);
      
      Reply reply = dataBuilder.createReply(metaform, replyData, ReplyMode.REVISION);
      assertEquals(fileRefs, reply.getData().get("files"));

      Reply foundReply = repliesApi.findReply(REALM_1, metaform.getId(), reply.getId());
      assertEquals(fileRefs, foundReply.getData().get("files"));
      
      assertAttachmentExists(adminAttachmentsApi, fileUpload1);
      assertAttachmentExists(adminAttachmentsApi, fileUpload2);
      
      assertEquals(getResourceMd5("test-image-480-320.jpg"), DigestUtils.md5Hex(getAttachmentData(accessToken, fileUpload1.getFileRef())));
      assertEquals(getResourceMd5("test-image-667-1000.jpg"), DigestUtils.md5Hex(getAttachmentData(accessToken, fileUpload2.getFileRef())));
    } finally {
      dataBuilder.clean();
    }
  }

  @Test
  public void updateAttachmentsTest() throws IOException, URISyntaxException {
    String accessToken = getAccessToken(REALM_1, "test1.realm1", "test");
    RepliesApi repliesApi = getRepliesApi(accessToken);

    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      Metaform metaform = dataBuilder.createMetaform("files");
      AttachmentsApi adminAttachmentsApi = dataBuilder.getAdminAttachmentsApi();
      FileUploadResponse fileUpload1 = uploadResourceFile("test-image-480-320.jpg");
      FileUploadResponse fileUpload2 = uploadResourceFile("test-image-667-1000.jpg");
      
      String fileRef1 = fileUpload1.getFileRef().toString();
      String fileRef2 = fileUpload2.getFileRef().toString();
      List<String> fileRefs = Arrays.asList(fileRef1, fileRef2);
      
      ReplyData replyData = new ReplyData();
      replyData.put("files", fileRefs);
      
      Reply reply = dataBuilder.createReply(metaform, replyData, ReplyMode.REVISION);
      assertEquals(fileRefs, reply.getData().get("files"));

      Reply foundReply = repliesApi.findReply(REALM_1, metaform.getId(), reply.getId());
      assertEquals(fileRefs, foundReply.getData().get("files"));
      
      assertAttachmentExists(adminAttachmentsApi, fileUpload1);
      assertAttachmentExists(adminAttachmentsApi, fileUpload2);

      ReplyData updateData = new ReplyData();
      updateData.put("files", Arrays.asList(fileRef2));
      
      repliesApi.updateReply(REALM_1, metaform.getId(), reply.getId(), createReplyWithData(updateData));
      
      Reply updatedReply = repliesApi.findReply(REALM_1, metaform.getId(), reply.getId());
      assertEquals(Arrays.asList(fileRef2), updatedReply.getData().get("files"));

      assertAttachmentNotFound(adminAttachmentsApi, fileUpload1.getFileRef());
      assertAttachmentExists(adminAttachmentsApi, fileUpload2);
    } finally {
      dataBuilder.clean();
    }
  }

  @Test
  public void deleteAttachmentsTest() throws IOException, URISyntaxException {
    String adminToken = getAdminToken(REALM_1);
    String accessToken = getAccessToken(REALM_1, "test1.realm1", "test");
    RepliesApi repliesApi = getRepliesApi(accessToken);
    RepliesApi adminRepliesApi = getRepliesApi(adminToken);

    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      Metaform metaform = dataBuilder.createMetaform("files");
      AttachmentsApi adminAttachmentsApi = dataBuilder.getAdminAttachmentsApi();
      FileUploadResponse fileUpload1 = uploadResourceFile("test-image-480-320.jpg");
      FileUploadResponse fileUpload2 = uploadResourceFile("test-image-667-1000.jpg");
      
      String fileRef1 = fileUpload1.getFileRef().toString();
      String fileRef2 = fileUpload2.getFileRef().toString();
      List<String> fileRefs = Arrays.asList(fileRef1, fileRef2);
      
      ReplyData replyData = new ReplyData();
      replyData.put("files", fileRefs);
      
      Reply reply = repliesApi.createReply(REALM_1, metaform.getId(), createReplyWithData(replyData), null, ReplyMode.REVISION.toString());
      assertEquals(fileRefs, reply.getData().get("files"));

      Reply foundReply = repliesApi.findReply(REALM_1, metaform.getId(), reply.getId());
      assertEquals(fileRefs, foundReply.getData().get("files"));
      
      assertAttachmentExists(adminAttachmentsApi, fileUpload1);
      assertAttachmentExists(adminAttachmentsApi, fileUpload2);
      
      adminRepliesApi.deleteReply(REALM_1, metaform.getId(), reply.getId());

      assertAttachmentNotFound(adminAttachmentsApi, fileUpload1.getFileRef());
      assertAttachmentNotFound(adminAttachmentsApi, fileUpload2.getFileRef());
    } finally {
      dataBuilder.clean();
    }
  }

  private void assertAttachmentExists(AttachmentsApi adminAttachmentsApi, FileUploadResponse fileUpload1) {
    Attachment attachment1 = adminAttachmentsApi.findAttachment(REALM_1, fileUpload1.getFileRef());
    assertNotNull(attachment1);
    assertEquals(fileUpload1.getFileRef(), attachment1.getId());
  }

  private void assertAttachmentNotFound(AttachmentsApi adminAttachmentsApi, UUID fileRef) {
    try {
      adminAttachmentsApi.findAttachment(REALM_1, fileRef);
      fail(String.format("Attachment %s should not be present", fileRef.toString()));
    } catch (FeignException e) {
      assertEquals(404, e.status());
    }
  }
  

  private byte[] getAttachmentData(String accessToken, UUID id) throws IOException {
    URL url = new URL(String.format("%s/v1/realms/%s/attachments/%s/data", getBasePath(), REALM_1, id.toString()));
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestProperty("Authorization", String.format("Bearer %s", accessToken));
    connection.setDoOutput(true);
    return IOUtils.toByteArray(connection.getInputStream());
  }
  
}
