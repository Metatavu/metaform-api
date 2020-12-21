package fi.metatavu.metaform.test.functional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import feign.FeignException;
import fi.metatavu.metaform.client.model.Attachment;
import fi.metatavu.metaform.client.api.AttachmentsApi;
import fi.metatavu.metaform.client.model.Metaform;
import fi.metatavu.metaform.client.api.RepliesApi;
import fi.metatavu.metaform.client.model.Reply;
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
      
      Map<String, Object> replyData = new HashMap<>();
      replyData.put("files", fileUpload.getFileRef());
      
      Reply reply = dataBuilder.createReply(metaform, replyData, ReplyMode.REVISION);
      assertNotNull(reply);
      assertNotNull(reply.getData());
      assertEquals(Arrays.asList(fileUpload.getFileRef().toString()), reply.getData().get("files"));

      Reply foundReply = repliesApi.findReply(metaform.getId(), reply.getId(), (String) null);
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
      
      Map<String, Object> replyData = new HashMap<>();
      replyData.put("files", fileRefs);
      
      Reply reply = dataBuilder.createReply(metaform, replyData, ReplyMode.REVISION);
      assertListsEqualInAnyOrder(fileRefs, reply.getData().get("files"));

      Reply foundReply = repliesApi.findReply(metaform.getId(), reply.getId(), (String) null);
      assertListsEqualInAnyOrder(fileRefs, foundReply.getData().get("files"));
      
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
      
      Map<String, Object> replyData = new HashMap<>();
      replyData.put("files", fileRefs);
      
      Reply reply = dataBuilder.createReply(metaform, replyData, ReplyMode.REVISION);
      assertListsEqualInAnyOrder(fileRefs, reply.getData().get("files"));
      
      Reply foundReply = repliesApi.findReply(metaform.getId(), reply.getId(), (String) null);
      assertListsEqualInAnyOrder(fileRefs, foundReply.getData().get("files"));
      
      assertAttachmentExists(adminAttachmentsApi, fileUpload1);
      assertAttachmentExists(adminAttachmentsApi, fileUpload2);

      Map<String, Object> updateData = new HashMap<>();
      updateData.put("files", Arrays.asList(fileRef2));
      
      repliesApi.updateReply(metaform.getId(), reply.getId(), createReplyWithData(updateData), (String) null);
      
      Reply updatedReply = repliesApi.findReply(metaform.getId(), reply.getId(), (String) null);
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
      
      Map<String, Object> replyData = new HashMap<>();
      replyData.put("files", fileRefs);
      
      Reply reply = repliesApi.createReply(metaform.getId(), createReplyWithData(replyData), null, ReplyMode.REVISION.toString());
      assertListsEqualInAnyOrder(fileRefs, reply.getData().get("files")); 
      
      Reply foundReply = repliesApi.findReply(metaform.getId(), reply.getId(), (String) null);
      assertListsEqualInAnyOrder(fileRefs, foundReply.getData().get("files"));
      
      assertAttachmentExists(adminAttachmentsApi, fileUpload1);
      assertAttachmentExists(adminAttachmentsApi, fileUpload2);
      
      adminRepliesApi.deleteReply(metaform.getId(), reply.getId(), (String) null);

      assertAttachmentNotFound(adminAttachmentsApi, fileUpload1.getFileRef());
      assertAttachmentNotFound(adminAttachmentsApi, fileUpload2.getFileRef());
    } finally {
      dataBuilder.clean();
    }
  }

  private void assertAttachmentExists(AttachmentsApi adminAttachmentsApi, FileUploadResponse fileUpload1) {
    Attachment attachment1 = adminAttachmentsApi.findAttachment(fileUpload1.getFileRef());
    assertNotNull(attachment1);
    assertEquals(fileUpload1.getFileRef(), attachment1.getId());
  }

  private void assertAttachmentNotFound(AttachmentsApi adminAttachmentsApi, UUID fileRef) {
    try {
      adminAttachmentsApi.findAttachment(fileRef);
      fail(String.format("Attachment %s should not be present", fileRef.toString()));
    } catch (FeignException e) {
      assertEquals(404, e.status());
    }
  }
  
  private byte[] getAttachmentData(String accessToken, UUID id) throws IOException {
    URL url = new URL(String.format("%s/v1/attachments/%s/data", getBasePath(), id.toString()));
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestProperty("Authorization", String.format("Bearer %s", accessToken));
    connection.setDoOutput(true);
    return IOUtils.toByteArray(connection.getInputStream());
  }
  
}
