package fi.metatavu.metaform.test.functional.tests;

import fi.metatavu.metaform.api.client.models.AuditLogEntry;
import fi.metatavu.metaform.api.client.models.Metaform;
import fi.metatavu.metaform.api.client.models.Reply;
import fi.metatavu.metaform.server.rest.ReplyMode;
import fi.metatavu.metaform.test.functional.AbstractIntegrationTest;
import fi.metatavu.metaform.test.functional.builder.TestBuilder;
import fi.metatavu.metaform.test.functional.builder.resources.KeycloakResource;
import fi.metatavu.metaform.test.functional.builder.resources.MysqlResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * Tests for AuditLogEntriesApi
 *
 * @author Katja Danilova
 */
@QuarkusTest
@QuarkusTestResource.List(value = {
  @QuarkusTestResource(MysqlResource.class),
  @QuarkusTestResource(KeycloakResource.class)
})
public class AuditLogEntryTestsIT extends AbstractIntegrationTest {

  @Test
  public void basicActionsOnReplyTest() throws Exception {
    try (TestBuilder builder = new TestBuilder()) {
      Metaform parsedMetaform = builder.metaformAdmin().metaforms().readMetaform("simple");
      Metaform metaform = builder.metaformAdmin().metaforms().create(parsedMetaform);

      Map<String, Object> replyData = new HashMap<>();
      replyData.put("text", "Test text value");
      Reply createdReply = builder.test1().replies().createReplyWithData(replyData);

      Reply reply = builder.test1().replies().create(metaform.getId(), null, ReplyMode.REVISION.toString(), createdReply);
      builder.test1().replies().findReply(metaform.getId(), reply.getId(), null);
      builder.test1().replies().listReplies(metaform.getId(), null, null, null, null, null, true, null, null, null);
      builder.test1().replies().updateReply(metaform.getId(), reply.getId(), reply, reply.getOwnerKey());
      builder.test1().replies().delete(metaform.getId(), reply, reply.getOwnerKey());

      AuditLogEntry[] auditLogEntries = builder.test1().auditLogs().listAuditLogEntries(metaform.getId(), null, reply.getId(), null, null);

      Assertions.assertEquals(5, auditLogEntries.length);
      Assertions.assertEquals(String.format("user %s created reply %s", REALM1_USER_1_ID, reply.getId()), auditLogEntries[0].getMessage());
      Assertions.assertEquals(String.format("user %s viewed reply %s", REALM1_USER_1_ID, reply.getId()), auditLogEntries[1].getMessage());
      Assertions.assertEquals(String.format("user %s listed reply %s", REALM1_USER_1_ID, reply.getId()), auditLogEntries[2].getMessage());
      Assertions.assertEquals(String.format("user %s modified reply %s", REALM1_USER_1_ID, reply.getId()), auditLogEntries[3].getMessage());
      Assertions.assertEquals(String.format("user %s deleted reply %s", REALM1_USER_1_ID, reply.getId()), auditLogEntries[4].getMessage());
    }
  }

  @Test
  public void queryByUserTest() throws Exception {
    try (TestBuilder builder = new TestBuilder()) {
      Metaform parsedMetaform = builder.metaformAdmin().metaforms().readMetaform("simple");
      Metaform metaform = builder.metaformAdmin().metaforms().create(parsedMetaform);

      Map<String, Object> replyData = new HashMap<>();
      replyData.put("text", "Test text value");
      Reply reply = builder.test1().replies().createReplyWithData(replyData);

      Reply createdReply1 = builder.test1().replies().create(metaform.getId(), null, ReplyMode.REVISION.toString(), reply);
      Reply createdReply2 = builder.test2().replies().create(metaform.getId(), null, ReplyMode.REVISION.toString(), reply);

      AuditLogEntry[] auditLogEntriesForUser1 = builder.test1().auditLogs().listAuditLogEntries(metaform.getId(), REALM1_USER_1_ID, null, null, null);
      AuditLogEntry[] auditLogEntriesForUser2 = builder.test1().auditLogs().listAuditLogEntries(metaform.getId(), REALM1_USER_2_ID, null, null, null);

      Assertions.assertEquals(String.format("user %s created reply %s", REALM1_USER_1_ID, createdReply1.getId()),
        auditLogEntriesForUser1[0].getMessage());
      Assertions.assertEquals(String.format("user %s created reply %s", REALM1_USER_2_ID, createdReply2.getId()),
        auditLogEntriesForUser2[0].getMessage());
    }
  }

  @Test
  public void queryByMetaformTest() throws Exception {
    try (TestBuilder builder = new TestBuilder()) {
      Metaform parsedMetaform = builder.metaformAdmin().metaforms().readMetaform("simple");
      Metaform metaform1 = builder.metaformAdmin().metaforms().create(parsedMetaform);
      Metaform metaform2 = builder.metaformAdmin().metaforms().create(parsedMetaform);

      Map<String, Object> replyData = new HashMap<>();
      replyData.put("text", "Test text value");
      Reply reply = builder.test1().replies().createReplyWithData(replyData);

      Reply createdReply1 = builder.test1().replies().create(metaform1.getId(), null, ReplyMode.REVISION.toString(), reply);
      Reply createdReply2 = builder.test1().replies().create(metaform2.getId(), null, ReplyMode.REVISION.toString(), reply);

      AuditLogEntry[] metaform1AuditLogs = builder.test1().auditLogs().listAuditLogEntries(metaform1.getId(), null, null, null, null);
      AuditLogEntry[] metaform2AuditLogs = builder.test1().auditLogs().listAuditLogEntries(metaform2.getId(), null, null, null, null);

      Assertions.assertEquals(1, metaform1AuditLogs.length);
      Assertions.assertEquals(1, metaform2AuditLogs.length);
      Assertions.assertEquals(String.format("user %s created reply %s", REALM1_USER_1_ID, createdReply1.getId()), metaform1AuditLogs[0].getMessage());
      Assertions.assertEquals(String.format("user %s created reply %s", REALM1_USER_1_ID, createdReply2.getId()), metaform2AuditLogs[0].getMessage());
    }
  }

  /**
   * test verifies that sorting by reply id words
   *
   * @throws IOException
   */
  @Test
  public void queryByReplyIdTest() throws Exception {
    try (TestBuilder builder = new TestBuilder()) {
      Metaform parsedMetaform = builder.metaformAdmin().metaforms().readMetaform("simple");
      Metaform metaform = builder.metaformAdmin().metaforms().create(parsedMetaform);

      Map<String, Object> replyData = new HashMap<>();
      replyData.put("text", "Test text value");
      Reply reply1 = builder.test1().replies().createReplyWithData(replyData);
      Reply reply2 = builder.test1().replies().createReplyWithData(replyData);

      Reply createdReply1 = builder.test1().replies().create(metaform.getId(), null, ReplyMode.REVISION.toString(), reply1);
      Reply createdReply2 = builder.test1().replies().create(metaform.getId(), null, ReplyMode.REVISION.toString(), reply2);

      AuditLogEntry[] auditLogEntries = builder.test1().auditLogs().listAuditLogEntries(metaform.getId(), null, null, null, null);
      Assertions.assertEquals(2, auditLogEntries.length);

      AuditLogEntry[] entryByReply = builder.test1().auditLogs().listAuditLogEntries(metaform.getId(), null, createdReply1.getId(), null, null);
      Assertions.assertEquals(1, entryByReply.length);
      Assertions.assertEquals(String.format("user %s created reply %s", REALM1_USER_1_ID, createdReply1.getId()), entryByReply[0].getMessage());
    }
  }

  @Test
  public void accessRightsTest() throws Exception {
    try (TestBuilder builder = new TestBuilder()) {
      Metaform parsedMetaform = builder.metaformAdmin().metaforms().readMetaform("simple");
      Metaform metaform = builder.metaformAdmin().metaforms().create(parsedMetaform);

      Map<String, Object> replyData = new HashMap<>();
      replyData.put("text", "Test text value");
      Reply reply1 = builder.test1().replies().createReplyWithData(replyData);
      builder.test1().replies().create(metaform.getId(), null, ReplyMode.REVISION.toString(), reply1);

      AuditLogEntry[] auditLogEntries = builder.test1().auditLogs().listAuditLogEntries(metaform.getId(), null, null, null, null);
      Assertions.assertNotNull(auditLogEntries);

      builder.test2().auditLogs().assertListFailStatus(403, metaform.getId(), null, null, null, null);
    }
  }
}
