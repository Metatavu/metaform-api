package fi.metatavu.metaform.test.functional.tests;

import com.github.tomakehurst.wiremock.client.WireMock;
import fi.metatavu.metaform.api.client.models.ExportTheme;
import fi.metatavu.metaform.api.client.models.Metaform;
import fi.metatavu.metaform.api.client.models.Reply;
import fi.metatavu.metaform.server.rest.ReplyMode;
import fi.metatavu.metaform.test.functional.AbstractIntegrationTest;
import fi.metatavu.metaform.test.functional.MailgunMocker;
import fi.metatavu.metaform.test.functional.builder.TestBuilder;
import fi.metatavu.metaform.test.functional.builder.resources.KeycloakResource;
import fi.metatavu.metaform.test.functional.builder.resources.MysqlResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;


@QuarkusTest
@QuarkusTestResource.List(value = {
  @QuarkusTestResource(MysqlResource.class),
  @QuarkusTestResource(KeycloakResource.class)
})
@TestProfile(DefTestProfile.class)
public class ReplyPermissionTestsIT extends AbstractIntegrationTest {

  @BeforeAll
  public static void setMocker() {
    int port = Integer.parseInt(ConfigProvider.getConfig().getValue("wiremock.port", String.class));
    WireMock.configureFor("localhost", port);
  }

  /**
   * Test that asserts that user may find his / her own reply
   */
  @Test
  public void findOwnReply() throws Exception {
    try (TestBuilder builder = new TestBuilder()) {
      Metaform parsedMetaform = builder.metaformAdmin().metaforms().readMetaform("simple-permission-context");
      Metaform metaform = builder.metaformAdmin().metaforms().create(parsedMetaform);

      Reply createdReply = builder.test1().replies().create(metaform.getId(), null, ReplyMode.REVISION.toString(),
        builder.test1().replies().createReplyWithData(createPermissionSelectReplyData("group-1")));

      Reply foundReply = builder.test1().replies().findReply(metaform.getId(), createdReply.getId(), null);
      Assertions.assertNotNull(foundReply);
    }
  }

  /**
   * Test that asserts that anonymous users may not find their "own" replies
   */
  @Test
  public void findOwnReplyAnonymous() throws Exception {
    try (TestBuilder builder = new TestBuilder()) {
      Metaform parsedMetaform = builder.metaformAdmin().metaforms().readMetaform("simple-permission-context");
      Metaform metaform = builder.metaformAdmin().metaforms().create(parsedMetaform);

      Reply createdReply = builder.test1().replies().create(metaform.getId(), null, ReplyMode.REVISION.toString(),
        builder.test1().replies().createReplyWithData(createPermissionSelectReplyData("group-1")));

      builder.anonymousToken().replies().assertFindFailStatus(403, metaform.getId(), createdReply.getId(), null);
    }
  }

  /**
   * Test that asserts that other users may not find their replies
   */
  @Test
  public void findOthersReplyUser() throws Exception {
    try (TestBuilder builder = new TestBuilder()) {
      Metaform parsedMetaform = builder.metaformAdmin().metaforms().readMetaform("simple-permission-context");
      Metaform metaform = builder.metaformAdmin().metaforms().create(parsedMetaform);

      Reply createdReply = builder.test1().replies().create(metaform.getId(), null, ReplyMode.REVISION.toString(),
        builder.test1().replies().createReplyWithData(createPermissionSelectReplyData("group-1")));

      builder.test2().replies().assertFindFailStatus(403, metaform.getId(), createdReply.getId(), null);
    }
  }

  /**
   * Test that asserts that metaform-admin may find replies created by others
   */
  @Test
  public void findOthersReplyAdmin() throws Exception {
    try (TestBuilder builder = new TestBuilder()) {
      Metaform parsedMetaform = builder.metaformAdmin().metaforms().readMetaform("simple-permission-context");
      Metaform metaform = builder.metaformAdmin().metaforms().create(parsedMetaform);

      Reply createdReply = builder.test1().replies().create(metaform.getId(), null, ReplyMode.REVISION.toString(),
        builder.test1().replies().createReplyWithData(createPermissionSelectReplyData("group-1")));

      Reply foundReply = builder.metaformAdmin().replies().findReply(metaform.getId(), createdReply.getId(), null);
      Assertions.assertNotNull(foundReply);
    }
  }

  /**
   * Test that asserts that user may list only his / her own replies
   */
  @Test
  public void listOwnReplies() throws Exception {
    try (TestBuilder builder = new TestBuilder()) {
      Metaform parsedMetaform = builder.metaformAdmin().metaforms().readMetaform("simple-permission-context");
      Metaform metaform = builder.metaformAdmin().metaforms().create(parsedMetaform);

      Reply reply1 = builder.test1().replies().create(metaform.getId(), null, ReplyMode.REVISION.toString(),
        builder.test1().replies().createReplyWithData(createPermissionSelectReplyData("group-1")));
      Reply reply2 = builder.test2().replies().create(metaform.getId(), null, ReplyMode.REVISION.toString(),
        builder.test1().replies().createReplyWithData(createPermissionSelectReplyData("group-2")));
      Reply reply3 = builder.test3().replies().create(metaform.getId(), null, ReplyMode.REVISION.toString(),
        builder.test1().replies().createReplyWithData(createPermissionSelectReplyData("group-3")));

      Reply[] replies = builder.test1().replies().listReplies(metaform.getId());
      assertEquals(1, replies.length);
      assertEquals(reply1.getId(), replies[0].getId());
    }
  }

  /**
   * Test that asserts that user in permission context group may see replies targeted to that group
   */
  @Test
  public void listPermissionContextReplies() throws Exception {
    try (TestBuilder builder = new TestBuilder()) {
      Metaform parsedMetaform = builder.metaformAdmin().metaforms().readMetaform("simple-permission-context");
      Metaform metaform = builder.metaformAdmin().metaforms().create(parsedMetaform);

      Reply reply1 = builder.test1().replies().create(metaform.getId(), null, ReplyMode.REVISION.toString(),
        builder.test1().replies().createReplyWithData(createPermissionSelectReplyData("group-2")));
      Reply reply2 = builder.test2().replies().create(metaform.getId(), null, ReplyMode.REVISION.toString(),
        builder.test1().replies().createReplyWithData(createPermissionSelectReplyData("group-2")));
      Reply reply3 = builder.test3().replies().create(metaform.getId(), null, ReplyMode.REVISION.toString(),
        builder.test1().replies().createReplyWithData(createPermissionSelectReplyData("group-2")));

      Reply[] replies1 = builder.test1().replies().listReplies(metaform.getId());
      Reply[] replies2 = builder.test2().replies().listReplies(metaform.getId());
      Reply[] replies3 = builder.test3().replies().listReplies(metaform.getId());

      Assertions.assertEquals(1, replies1.length);
      Assertions.assertEquals(reply1.getId(), replies1[0].getId());

      Assertions.assertEquals(3, replies2.length);

      Set<UUID> reply2Ids = Arrays.stream(replies2).map(Reply::getId).collect(Collectors.toSet());
      Assertions.assertTrue(reply2Ids.contains(reply1.getId()));
      Assertions.assertTrue(reply2Ids.contains(reply2.getId()));
      Assertions.assertTrue(reply2Ids.contains(reply3.getId()));

      Assertions.assertEquals(1, replies3.length);
      Assertions.assertEquals(reply3.getId(), replies3[0].getId());
    }
  }

  /**
   * Test that asserts that user in permission context group may see replies targeted to that group
   */
  @Test
  public void exportPermissionContextReplyPdf() throws Exception {
    try (TestBuilder builder = new TestBuilder()) {
      ExportTheme theme = builder.metaformSuper().exportThemes().createSimpleExportTheme("theme 1");
      builder.metaformSuper().exportfiles().createSimpleExportThemeFile(theme.getId(), "reply/pdf.ftl", "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></meta><title>title</title></head><body>content</body></html>");

      Metaform parsedMetaform = builder.metaformAdmin().metaforms().readMetaform("simple-permission-context");
      Metaform metaform = builder.metaformAdmin().metaforms().create(parsedMetaform);

      Metaform updateData = new Metaform(metaform.getId(), metaform.getReplyStrategy(), theme.getId(), metaform.getAllowAnonymous(), metaform.getAllowDrafts(),
        metaform.getAllowReplyOwnerKeys(), metaform.getAllowInvitations(), metaform.getAutosave(), metaform.getTitle(), metaform.getSections(), metaform.getFilters(), metaform.getScripts());

      builder.metaformAdmin().metaforms().updateMetaform(metaform.getId(), updateData);

      Reply reply1 = builder.test1().replies().create(metaform.getId(), null, ReplyMode.REVISION.toString(),
        builder.test1().replies().createReplyWithData(createPermissionSelectReplyData("group-2")));
      Reply reply2 = builder.test2().replies().create(metaform.getId(), null, ReplyMode.REVISION.toString(),
        builder.test1().replies().createReplyWithData(createPermissionSelectReplyData("group-2")));
      Reply reply3 = builder.test3().replies().create(metaform.getId(), null, ReplyMode.REVISION.toString(),
        builder.test1().replies().createReplyWithData(createPermissionSelectReplyData("group-2")));

      // test1.realm1 may download only own reply
      assertPdfDownloadStatus(200, builder.test1().token(), metaform, reply1);
      assertPdfDownloadStatus(403, builder.test1().token(), metaform, reply2);
      assertPdfDownloadStatus(403, builder.test1().token(), metaform, reply3);

      // test2.realm1 may download all the replies
      assertPdfDownloadStatus(200, builder.test2().token(), metaform, reply1);
      assertPdfDownloadStatus(200, builder.test2().token(), metaform, reply2);
      assertPdfDownloadStatus(200, builder.test2().token(), metaform, reply3);

      // test3.realm1 may download only own reply
      assertPdfDownloadStatus(403, builder.test3().token(), metaform, reply1);
      assertPdfDownloadStatus(403, builder.test3().token(), metaform, reply2);
      assertPdfDownloadStatus(200, builder.test3().token(), metaform, reply3);

      // anonymous may not download any replies
      assertPdfDownloadStatus(403, builder.anonymousToken().token(), metaform, reply1);
      assertPdfDownloadStatus(403, builder.anonymousToken().token(), metaform, reply2);
      assertPdfDownloadStatus(403, builder.anonymousToken().token(), metaform, reply3);
    }
  }

  /**
   * Test that asserts that admin may list all replies
   */
  @Test
  public void listRepliesAdmin() throws Exception {
    try (TestBuilder builder = new TestBuilder()) {
      Metaform parsedMetaform = builder.metaformAdmin().metaforms().readMetaform("simple-permission-context");
      Metaform metaform = builder.metaformAdmin().metaforms().create(parsedMetaform);

      builder.test1().replies().create(metaform.getId(), null, ReplyMode.REVISION.toString(),
        builder.test1().replies().createReplyWithData(createPermissionSelectReplyData("group-2")));
      builder.test2().replies().create(metaform.getId(), null, ReplyMode.REVISION.toString(),
        builder.test1().replies().createReplyWithData(createPermissionSelectReplyData("group-2")));
      builder.test3().replies().create(metaform.getId(), null, ReplyMode.REVISION.toString(),
        builder.test1().replies().createReplyWithData(createPermissionSelectReplyData("group-2")));

      Reply[] replies = builder.metaformAdmin().replies().listReplies(metaform.getId());
      Assertions.assertEquals(3, replies.length);
    }
  }

  /**
   * Test that asserts that user in permission context receives an email when notification is posted and
   * another user receives when reply is updated
   */
  @Test
  public void notifyPermissionContextReply() throws Exception {
    MailgunMocker mailgunMocker = startMailgunMocker();

    try (TestBuilder builder = new TestBuilder()) {
      Metaform parsedMetaform = builder.metaformAdmin().metaforms().readMetaform("simple-permission-context");
      Metaform metaform = builder.metaformAdmin().metaforms().create(parsedMetaform);

      builder.test3().emailNotifications().createEmailNotification(metaform, "Permission context subject", "Permission context content", Collections.emptyList());
      Reply createdReply = builder.test3().replies().create(metaform.getId(), null, ReplyMode.REVISION.toString(),
        builder.test3().replies().createReplyWithData(createPermissionSelectReplyData("group-2")));

      builder.test3().replies().updateReply(metaform.getId(),
        createdReply.getId(),
        builder.test3().replies().createPermisionSelectReply("group-1"), (String) null);
      builder.test3().replies().updateReply(metaform.getId(),
        createdReply.getId(),
        builder.test3().replies().createPermisionSelectReply("group-1"), (String) null);

      mailgunMocker.verifyHtmlMessageSent(1, "Metaform Test", "metaform-test@example.com", "user1@example.com", "Permission context subject", "Permission context content");
      mailgunMocker.verifyHtmlMessageSent(1, "Metaform Test", "metaform-test@example.com", "user2@example.com", "Permission context subject", "Permission context content");
    }
    finally {
      stopMailgunMocker(mailgunMocker);
    }
  }


  /**
   * Creates permission select reply data with given value
   *
   * @param value value
   */
  private Map<String, Object> createPermissionSelectReplyData(String value) {
    Map<String, Object> replyData = new HashMap<>();
    replyData.put("permission-select", value);
    return replyData;
  }

}
