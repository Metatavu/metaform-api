package fi.metatavu.metaform.test.functional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import fi.metatavu.metaform.api.client.models.Reply;
import fi.metatavu.metaform.test.TestSettings;
import fi.metatavu.metaform.test.functional.builder.TestBuilder;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

import fi.metatavu.metaform.server.rest.ReplyMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings ("squid:S1192")
@QuarkusTest
@QuarkusTestResource.List(value = {
  @QuarkusTestResource(MysqlResource.class),
  @QuarkusTestResource(KeycloakResource.class)
})
public class AttachmentTestsIT extends AbstractIntegrationTest{

  @Test
  public void findAttachmentTest() throws Exception {
    try (TestBuilder builder = new TestBuilder()) {
      fi.metatavu.metaform.api.client.models.Metaform readMetaform = builder.metaformAdmin().metaforms().readMetaform("files");
      fi.metatavu.metaform.api.client.models.Metaform metaform = builder.metaformAdmin().metaforms().create(readMetaform);

      FileUploadResponse fileUpload = uploadResourceFile("test-image-480-320.jpg");

      Map<String, Object> replyData = new HashMap<>();
      replyData.put("files", fileUpload.getFileRef());

      Reply replyWithData = builder.test1().replies().createReplyWithData(replyData);
      Reply reply = builder.test1().replies().create(metaform.getId(), ReplyMode.REVISION.toString(), replyWithData);

      Assertions.assertNotNull(reply);
      Assertions.assertNotNull(reply.getData());
      Assertions.assertEquals(Collections.singletonList(fileUpload.getFileRef().toString()), reply.getData().get("files"));

      Reply foundReply = builder.test1().replies().findReply(metaform.getId(), reply.getId(), (String) null);
      Assertions.assertNotNull(foundReply);
      Assertions.assertNotNull(foundReply.getData());
      Assertions.assertEquals(Collections.singletonList(fileUpload.getFileRef().toString()), foundReply.getData().get("files"));

      builder.metaformAdmin().attachments().assertAttachmentExists(fileUpload);
      assertEquals(getResourceMd5("test-image-480-320.jpg"), DigestUtils.md5Hex(getAttachmentData(builder.test1().token(), fileUpload.getFileRef())));
    }
  }

  @Test
  public void findMultipleAttachmentsTest() throws Exception {
    try (TestBuilder builder = new TestBuilder()) {
      fi.metatavu.metaform.api.client.models.Metaform readMetaform = builder.metaformAdmin().metaforms().readMetaform("files");
      fi.metatavu.metaform.api.client.models.Metaform metaform = builder.metaformAdmin().metaforms().create(readMetaform);

      FileUploadResponse fileUpload1 = uploadResourceFile("test-image-480-320.jpg");
      FileUploadResponse fileUpload2 = uploadResourceFile("test-image-667-1000.jpg");

      String fileRef1 = fileUpload1.getFileRef().toString();
      String fileRef2 = fileUpload2.getFileRef().toString();
      List<String> fileRefs = Arrays.asList(fileRef1, fileRef2);

      Map<String, Object> replyData = new HashMap<>();
      replyData.put("files", fileRefs);

      Reply replyWithData = builder.test1().replies().createReplyWithData(replyData);
      Reply reply = builder.test1().replies().create(metaform.getId(), ReplyMode.REVISION.toString(), replyWithData);

      assertListsEqualInAnyOrder(fileRefs, reply.getData().get("files"));
      builder.metaformAdmin().attachments().assertAttachmentExists(fileUpload1);
      builder.metaformAdmin().attachments().assertAttachmentExists(fileUpload2);

      String token = builder.test1().token();
      assertEquals(getResourceMd5("test-image-480-320.jpg"), DigestUtils.md5Hex(getAttachmentData(token, fileUpload1.getFileRef())));
      assertEquals(getResourceMd5("test-image-667-1000.jpg"), DigestUtils.md5Hex(getAttachmentData(token, fileUpload2.getFileRef())));
    }
  }

  @Test
  public void updateAttachmentsTest() throws Exception {
    try (TestBuilder builder = new TestBuilder()) {
      fi.metatavu.metaform.api.client.models.Metaform readMetaform = builder.metaformAdmin().metaforms().readMetaform("files");
      fi.metatavu.metaform.api.client.models.Metaform metaform = builder.metaformAdmin().metaforms().create(readMetaform);


      FileUploadResponse fileUpload1 = uploadResourceFile("test-image-480-320.jpg");
      FileUploadResponse fileUpload2 = uploadResourceFile("test-image-667-1000.jpg");

      String fileRef1 = fileUpload1.getFileRef().toString();
      String fileRef2 = fileUpload2.getFileRef().toString();
      List<String> fileRefs = Arrays.asList(fileRef1, fileRef2);

      Map<String, Object> replyData = new HashMap<>();
      replyData.put("files", fileRefs);

      Reply replyWithData = builder.test1().replies().createReplyWithData(replyData);
      Reply reply = builder.test1().replies().create(metaform.getId(), ReplyMode.REVISION.toString(), replyWithData);
      assertListsEqualInAnyOrder(fileRefs, reply.getData().get("files"));

      Reply foundReply = builder.test1().replies().findReply(metaform.getId(), reply.getId(), (String) null);
      assertListsEqualInAnyOrder(fileRefs, foundReply.getData().get("files"));

      builder.metaformAdmin().attachments().assertAttachmentExists(fileUpload1);
      builder.metaformAdmin().attachments().assertAttachmentExists(fileUpload2);

      Map<String, Object> updateData = new HashMap<>();
      updateData.put("files", Arrays.asList(fileRef2));

      Reply newReplyWithData = builder.test1().replies().createReplyWithData(updateData);
      builder.test1().replies().updateReply(metaform.getId(), reply.getId(), newReplyWithData, null);

      Reply updatedReply = builder.test1().replies().findReply(metaform.getId(), reply.getId(), (String) null);
      assertEquals(Arrays.asList(fileRef2), updatedReply.getData().get("files"));

      builder.metaformAdmin().attachments().assertAttachmentNotFound(fileUpload1.getFileRef());
      builder.metaformAdmin().attachments().assertAttachmentExists(fileUpload2);
    }
  }

  @Test
  public void deleteAttachmentsTest() throws Exception {
    try (TestBuilder builder = new TestBuilder()) {
      fi.metatavu.metaform.api.client.models.Metaform readMetaform = builder.metaformAdmin().metaforms().readMetaform("files");
      fi.metatavu.metaform.api.client.models.Metaform metaform = builder.metaformAdmin().metaforms().create(readMetaform);

      FileUploadResponse fileUpload1 = uploadResourceFile("test-image-480-320.jpg");
      FileUploadResponse fileUpload2 = uploadResourceFile("test-image-667-1000.jpg");

      String fileRef1 = fileUpload1.getFileRef().toString();
      String fileRef2 = fileUpload2.getFileRef().toString();
      List<String> fileRefs = Arrays.asList(fileRef1, fileRef2);

      Map<String, Object> replyData = new HashMap<>();
      replyData.put("files", fileRefs);

      Reply replyWithData = builder.test1().replies().createReplyWithData(replyData);
      Reply reply = builder.test1().replies().create(metaform.getId(), ReplyMode.REVISION.toString(), replyWithData);
      assertListsEqualInAnyOrder(fileRefs, reply.getData().get("files"));

      Reply foundReply = builder.test1().replies().findReply(metaform.getId(), reply.getId(), (String) null);
      assertListsEqualInAnyOrder(fileRefs, foundReply.getData().get("files"));

      builder.metaformAdmin().attachments().assertAttachmentExists(fileUpload1);
      builder.metaformAdmin().attachments().assertAttachmentExists(fileUpload2);

      builder.metaformAdmin().replies().delete(metaform.getId(), reply, (String) null);

      builder.metaformAdmin().attachments().assertAttachmentNotFound(fileUpload1.getFileRef());
      builder.metaformAdmin().attachments().assertAttachmentNotFound(fileUpload2.getFileRef());
    }
  }

  private byte[] getAttachmentData(String accessToken, UUID id) throws IOException {
    URL url = new URL(String.format("%sv1/attachments/%s/data", TestSettings.basePath, id.toString()));
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestProperty("Authorization", String.format("Bearer %s", accessToken));
    connection.setDoOutput(true);
    return IOUtils.toByteArray(connection.getInputStream());
  }

}
