package fi.metatavu.metaform.test.functional;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

import fi.metatavu.metaform.api.client.models.ExportTheme;
import fi.metatavu.metaform.api.client.models.Metaform;
import fi.metatavu.metaform.api.client.models.Reply;
import fi.metatavu.metaform.server.rest.ReplyMode;
import fi.metatavu.metaform.test.TestSettings;
import fi.metatavu.metaform.test.functional.builder.TestBuilder;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings ("squid:S1192")
@QuarkusTest
@QuarkusTestResource.List(value = {
  @QuarkusTestResource(MysqlResource.class),
  @QuarkusTestResource(KeycloakResource.class)
})
public class ReplyTestsIT extends AbstractIntegrationTest{
  
  private static final ZoneId TIMEZONE = ZoneId.of("Europe/Helsinki");

  @Test
  public void createReplyNotLoggedIn() throws Exception {
    try (TestBuilder builder = new TestBuilder()) {
      Metaform metaform =  builder.metaformAdmin().metaforms().readMetaform("simple");
      Metaform createdMetaform = builder.metaformAdmin().metaforms().create(metaform);

      //TODO WHY 401 IS EXPECTED?
      given()
        .baseUri(TestSettings.basePath)
        .header("Content-Type", "application/json")
        .post("/v1/metaforms/{metaformId}/replies", createdMetaform.getId())
        .then()
        .assertThat()
        .statusCode(403);
    }

  }

  @Test
  public void createReply() throws Exception {
    try (TestBuilder builder = new TestBuilder()) {
      Metaform parsedMetaform =  builder.metaformAdmin().metaforms().readMetaform("simple");
      Metaform metaform = builder.metaformAdmin().metaforms().create(parsedMetaform);

      Map<String, Object> replyData = new HashMap<>();
      replyData.put("text", "Test text value");
      Reply reply = builder.test1().replies().createReplyWithData(replyData);
      Reply createdReply = builder.metaformAdmin().replies().create(metaform.getId(), null, ReplyMode.REVISION.toString(), reply);
      Reply foundReply = builder.metaformAdmin().replies().findReply(metaform.getId(), createdReply.getId(), (String) null);
      Assertions.assertNotNull(foundReply);
      Assertions.assertNotNull(foundReply.getId());
      Assertions.assertNotNull(foundReply.getData());
      Assertions.assertEquals("Test text value", foundReply.getData().get("text"));
    }
  }

  @Test
  public void createReplyUpdateExisting() throws Exception {
      TestBuilder builder = new TestBuilder();
      Metaform parsedMetaform = builder.metaformAdmin().metaforms().readMetaform("simple");
      Metaform metaform = builder.metaformAdmin().metaforms().create(parsedMetaform);
      try {
        Map<String, Object> replyData1 = new HashMap<>();
        replyData1.put("text", "Test text value");
        Reply reply1 = builder.metaformAdmin().replies().createReplyWithData(replyData1);

        Map<String, Object> replyData2 = new HashMap<>();
        replyData2.put("text", "Updated text value");

        Reply reply2 = builder.metaformAdmin().replies().createReplyWithData(replyData2);

        Reply createdReply1 = builder.metaformAdmin().replies().create(metaform.getId(), null, ReplyMode.UPDATE.toString(), reply1);

        try {
          Assertions.assertNotNull(createdReply1);
          Assertions.assertNotNull(createdReply1.getId());
          Assertions.assertNotNull(createdReply1.getData());
          Assertions.assertEquals("Test text value", createdReply1.getData().get("text"));

          Reply createdReply2 = builder.metaformAdmin().replies().create(metaform.getId(), null, ReplyMode.UPDATE.toString(), reply2);

          Assertions.assertNotNull(createdReply2);
          Assertions.assertEquals(createdReply1.getId(), createdReply2.getId());
          Assertions.assertNotNull(createdReply2.getData());
          Assertions.assertEquals("Updated text value", createdReply2.getData().get("text"));
        }
        finally {
          builder.metaformAdmin().replies().delete(metaform.getId(), createdReply1, null);
        }
      }
      finally {
        builder.metaformAdmin().metaforms().delete(metaform);
      }
  }

  @Test
  public void createReplyVersionExisting() throws Exception {
    try (TestBuilder builder = new TestBuilder()) {
      Metaform parsedMetaform = builder.metaformAdmin().metaforms().readMetaform("simple");
      Metaform metaform = builder.metaformAdmin().metaforms().create(parsedMetaform);

      Map<String, Object> replyData1 = new HashMap<>();
      replyData1.put("text", "Test text value");
      Reply reply1 = builder.metaformAdmin().replies().createReplyWithData(replyData1);

      Map<String, Object> replyData2 = new HashMap<>();
      replyData2.put("text", "Updated text value");
      Reply reply2 = builder.metaformAdmin().replies().createReplyWithData(replyData2);

      Reply createdReply1 = builder.test1().replies().create(metaform.getId(), null, ReplyMode.UPDATE.toString(), reply1);

      Assertions.assertNotNull(createdReply1);
      Assertions.assertNotNull(createdReply1.getId());
      Assertions.assertEquals("Test text value", createdReply1.getData().get("text"));

      Reply createdReply2 = builder.test1().replies().create(metaform.getId(), null, ReplyMode.REVISION.toString(), reply2);
      Assertions.assertNotNull(createdReply2);
      Assertions.assertNotEquals(createdReply1.getId(), createdReply2.getId());
      Assertions.assertEquals("Updated text value", createdReply2.getData().get("text"));

      List<Reply> replies = Arrays.asList(builder.test1().replies().listReplies(metaform.getId(),
        REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE,
        null, null, null).clone());
      Assertions.assertEquals(2, replies.size());
      Assertions.assertNotNull(replies.get(0).getRevision());
      Assertions.assertEquals("Test text value", replies.get(0).getData().get("text"));
      Assertions.assertNull(replies.get(1).getRevision());
      Assertions.assertEquals("Updated text value", replies.get(1).getData().get("text"));
    }
  }


  @Test
  public void createReplyCumulative() throws Exception {
    try (TestBuilder builder = new TestBuilder()) {
      Metaform parsedMetaform = builder.metaformAdmin().metaforms().readMetaform("simple");
      Metaform metaform = builder.metaformAdmin().metaforms().create(parsedMetaform);

      Assertions.assertNotNull(metaform);
      builder.test1().replies().createSimpleReply(metaform, "val 1", ReplyMode.CUMULATIVE);
      builder.test1().replies().createSimpleReply(metaform, "val 2", ReplyMode.CUMULATIVE);
      builder.test1().replies().createSimpleReply(metaform, "val 3", ReplyMode.CUMULATIVE);

      List<Reply> replies = Arrays.asList(builder.test1().replies().listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.TRUE, null, null, null).clone());

      Assertions.assertEquals(3, replies.size());
      Assertions.assertEquals("val 1", replies.get(0).getData().get("text"));
      Assertions.assertEquals("val 2", replies.get(1).getData().get("text"));
      Assertions.assertEquals("val 3", replies.get(2).getData().get("text"));
    }

  }

  @Test
  public void testUpdateReply() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform parsedMetaform = testBuilder.metaformAdmin().metaforms().readMetaform("simple");
      Metaform metaform = testBuilder.metaformAdmin().metaforms().create(parsedMetaform);

      Reply reply = testBuilder.test1().replies().createSimpleReply(metaform, "test 1", ReplyMode.UPDATE);

      Map<String, Object> updateData = new HashMap<>();
      updateData.put("text", "Updated text value");
      Reply secondReply = new Reply(reply.getId(), reply.getUserId(), reply.getRevision(), reply.getOwnerKey(), reply.getCreatedAt(),
        reply.getModifiedAt(), updateData);

      testBuilder.test1().replies().updateReply(metaform.getId(), secondReply.getId(), secondReply, (String) null);
      Reply updatedReply = testBuilder.test1().replies().findReply(metaform.getId(), reply.getId(), null);

      Assertions.assertEquals("Updated text value", updatedReply.getData().get("text"));
    }
  }

  @Test
  public void listRepliesByTextFields() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform parsedMetaform = testBuilder.metaformAdmin().metaforms().readMetaform("tbnc");
      Metaform metaform = testBuilder.metaformAdmin().metaforms().create(parsedMetaform);

      Assertions.assertNotNull(metaform);

      testBuilder.test1().replies().createTBNCReply(metaform, "test 1", Boolean.TRUE, 1.0d, new String[] { "option 1" });
      testBuilder.test1().replies().createTBNCReply(metaform, "test 2", Boolean.FALSE, 2.5d, new String[] { "option 2" });
      testBuilder.test1().replies().createTBNCReply(metaform, "test 3", null, 0d, new String[] { });

      Reply[] replies1 = testBuilder.test1().replies().listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null,
        Boolean.TRUE, ArrayUtils.toArray("text:test 1"), null, null);
      Reply[] replies2 = testBuilder.test1().replies().listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null,
        Boolean.TRUE, ArrayUtils.toArray("text:test 2"), null, null);
      Reply[] repliesBoth = testBuilder.test1().replies().listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null,
        Boolean.TRUE, ArrayUtils.toArray("text:test 1", "text:test 2"), null, null);
      Reply[] repliesNone = testBuilder.test1().replies().listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null,
        Boolean.TRUE, ArrayUtils.toArray("text:non", "text:existing"), null, null);
      Reply[] notReplies = testBuilder.test1().replies().listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null,
        Boolean.TRUE, ArrayUtils.toArray("text^test 1"), null, null);

      Assertions.assertEquals(1, replies1.length);
      Assertions.assertEquals("test 1", replies1[0].getData().get("text"));

      Assertions.assertEquals(1, replies2.length);
      Assertions.assertEquals("test 2", replies2[0].getData().get("text"));

      Assertions.assertEquals(0, repliesBoth.length);

      Assertions.assertEquals(2, notReplies.length);
      Assertions.assertEquals("test 2", notReplies[0].getData().get("text"));
      Assertions.assertEquals("test 3", notReplies[1].getData().get("text"));

      Assertions.assertEquals(0, repliesNone.length);

    }
  }

  @Test
  public void listRepliesByListFields() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform parsedMetaform = testBuilder.metaformAdmin().metaforms().readMetaform("tbnc");
      Metaform metaform = testBuilder.metaformAdmin().metaforms().create(parsedMetaform);

      Assertions.assertNotNull(metaform);

      testBuilder.test1().replies().createTBNCReply(metaform, "test 1", Boolean.TRUE, 1.0d, new String[] { "option 1" });
      testBuilder.test1().replies().createTBNCReply(metaform, "test 2", Boolean.FALSE, 2.5d, new String[] { "option 2" });
      testBuilder.test1().replies().createTBNCReply(metaform, "test 3",null, 0d, new String[] { });

      Reply[] replies1 = testBuilder.test1().replies().listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null,
        Boolean.TRUE, ArrayUtils.toArray("checklist:option 1"), null, null);
      Reply[] replies2 = testBuilder.test1().replies().listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null,
        Boolean.TRUE, ArrayUtils.toArray("checklist:option 2"), null, null);
      Reply[] repliesBoth = testBuilder.test1().replies().listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null,
        Boolean.TRUE, ArrayUtils.toArray("checklist:option 1", "checklist:option 2"), null, null);
      Reply[] repliesNone = testBuilder.test1().replies().listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null,
        Boolean.TRUE, ArrayUtils.toArray("checklist:non", "checklist:existing"), null, null);
      Reply[] notReplies = testBuilder.test1().replies().listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null,
        Boolean.TRUE, ArrayUtils.toArray("checklist^option 1"), null, null);

      Assertions.assertEquals(1, replies1.length);
      Assertions.assertEquals("test 1", replies1[0].getData().get("text"));

      Assertions.assertEquals(1, replies2.length);
      Assertions.assertEquals("test 2", replies2[0].getData().get("text"));

      Assertions.assertEquals(0, repliesBoth.length);
      Assertions.assertEquals(0, repliesNone.length);

      Assertions.assertEquals(2, notReplies.length);
      Assertions.assertEquals("test 2", notReplies[0].getData().get("text"));
      Assertions.assertEquals("test 3", notReplies[1].getData().get("text"));

    }
  }
  
  @Test
  public void listRepliesByNumberFields() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform parsedMetaform = testBuilder.metaformAdmin().metaforms().readMetaform("tbnc");
      Metaform metaform = testBuilder.metaformAdmin().metaforms().create(parsedMetaform);

      Assertions.assertNotNull(metaform);

      testBuilder.test1().replies().createTBNCReply(metaform, "test 1", Boolean.TRUE, 1.0d, new String[] { "option 1" });
      testBuilder.test1().replies().createTBNCReply(metaform, "test 2", Boolean.FALSE, 2.5d, new String[] { "option 2" });
      testBuilder.test1().replies().createTBNCReply(metaform, "test 3",null, 0d, new String[] { });

      Reply[] replies1 = testBuilder.test1().replies().listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null,
        Boolean.TRUE, ArrayUtils.toArray("number:1"), null, null);
      Reply[] replies2 = testBuilder.test1().replies().listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null,
        Boolean.TRUE, ArrayUtils.toArray("number:2.5"), null, null);
      Reply[] repliesBoth = testBuilder.test1().replies().listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null,
        Boolean.TRUE, ArrayUtils.toArray("number:1", "number:2.5"), null, null);
      Reply[] repliesNone = testBuilder.test1().replies().listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null,
        Boolean.TRUE, ArrayUtils.toArray("number:55", "number:66"), null, null);
      Reply[] notReplies = testBuilder.test1().replies().listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null,
        Boolean.TRUE, ArrayUtils.toArray("number^1"), null, null);


      Assertions.assertEquals(1, replies1.length);
      Assertions.assertEquals("test 1", replies1[0].getData().get("text"));

      Assertions.assertEquals(1, replies2.length);
      Assertions.assertEquals("test 2", replies2[0].getData().get("text"));

      Assertions.assertEquals(0, repliesBoth.length);
      Assertions.assertEquals(0, repliesNone.length);

      Assertions.assertEquals(2, notReplies.length);
      Assertions.assertEquals("test 2", notReplies[0].getData().get("text"));
      Assertions.assertEquals("test 3", notReplies[1].getData().get("text"));
    }
  }
  
  @Test
  public void listRepliesByBooleanFields() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform parsedMetaform = testBuilder.metaformAdmin().metaforms().readMetaform("tbnc");
      Metaform metaform = testBuilder.metaformAdmin().metaforms().create(parsedMetaform);

      Assertions.assertNotNull(metaform);

      testBuilder.test1().replies().createTBNCReply(metaform, "test 1", Boolean.TRUE, 1.0d, new String[]{"option 1"});
      testBuilder.test1().replies().createTBNCReply(metaform, "test 2", Boolean.FALSE, 2.5d, new String[]{"option 2"});
      testBuilder.test1().replies().createTBNCReply(metaform, "test 3", null, 0d, new String[]{});

      Reply[] replies1 = testBuilder.test1().replies().listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null,
        Boolean.TRUE, ArrayUtils.toArray("boolean:true"), null, null);
      Reply[] replies2 = testBuilder.test1().replies().listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null,
        Boolean.TRUE, ArrayUtils.toArray("boolean:false"), null, null);
      Reply[] notReplies = testBuilder.test1().replies().listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null,
        Boolean.TRUE, ArrayUtils.toArray("boolean^false"), null, null);


      Assertions.assertEquals(1, replies1.length);
      Assertions.assertEquals("test 1", replies1[0].getData().get("text"));

      Assertions.assertEquals(1, replies2.length);
      Assertions.assertEquals("test 2", replies2[0].getData().get("text"));

      Assertions.assertEquals(2, notReplies.length);
      Assertions.assertEquals("test 1", notReplies[0].getData().get("text"));
      Assertions.assertEquals("test 3", notReplies[1].getData().get("text"));
    }
  }
  
  @Test
  public void listRepliesByMultiFields() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform parsedMetaform = testBuilder.metaformAdmin().metaforms().readMetaform("tbnc");
      Metaform metaform = testBuilder.metaformAdmin().metaforms().create(parsedMetaform);

      Assertions.assertNotNull(metaform);

      testBuilder.test1().replies().createTBNCReply(metaform, "test 1", Boolean.TRUE, 1.0d, new String[]{"option 1"});
      testBuilder.test1().replies().createTBNCReply(metaform, "test 2", Boolean.FALSE, 2.5d, new String[]{"option 2"});
      testBuilder.test1().replies().createTBNCReply(metaform, "test 3", null, 0d, new String[]{});

      Reply[] replies1 = testBuilder.test1().replies().listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null,
        Boolean.TRUE, ArrayUtils.toArray("boolean:true", "number:1"), null, null);
      Reply[] replies2 = testBuilder.test1().replies().listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null,
        Boolean.TRUE, ArrayUtils.toArray("boolean:false", "number:1"), null, null);
      Reply[] replies3 = testBuilder.test1().replies().listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null,
        Boolean.TRUE, ArrayUtils.toArray("checklist:option 1", "boolean:true"), null, null);
      Reply[] replies4 = testBuilder.test1().replies().listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null,
        Boolean.TRUE, ArrayUtils.toArray("checklist^option 1", "boolean:false"), null, null);

      Assertions.assertEquals(1, replies1.length);
      Assertions.assertEquals("test 1", replies1[0].getData().get("text"));

      Assertions.assertEquals(0, replies2.length);

      Assertions.assertEquals(1, replies3.length);
      Assertions.assertEquals("test 1", replies3[0].getData().get("text"));

      Assertions.assertEquals(1, replies4.length);
      Assertions.assertEquals("test 2", replies4[0].getData().get("text"));
    }
  }

  /**
   * Updates reply to be created at specific time
   *
   * @param reply reply
   * @param created created
   */
  private void updateReplyCreated(Reply reply, OffsetDateTime created) {
    executeUpdate("UPDATE Reply SET createdAt = ? WHERE id = ?", created, reply.getId());
    flushCache();
  }

  /**
   * Updates reply to be modified at specific time
   *
   * @param reply reply
   * @param modified created
   */
  private void updateReplyModified(Reply reply, OffsetDateTime modified) {
    executeUpdate("UPDATE Reply SET modifiedAt = ? WHERE id = ?", modified, reply.getId());
    flushCache();
  }
  @Test
  public void listRepliesByCreatedBefore() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform parsedMetaform = testBuilder.metaformAdmin().metaforms().readMetaform("simple");
      Metaform metaform = testBuilder.metaformAdmin().metaforms().create(parsedMetaform);

      Reply reply1 = testBuilder.test1().replies().createSimpleReply(metaform, "test 1", ReplyMode.CUMULATIVE);
      Reply reply2 = testBuilder.test1().replies().createSimpleReply(metaform, "test 2", ReplyMode.CUMULATIVE);
      Reply reply3 = testBuilder.test1().replies().createSimpleReply(metaform, "test 3", ReplyMode.CUMULATIVE);

      updateReplyCreated(reply1, getOffsetDateTime(2018, 5, 25, TIMEZONE));
      updateReplyCreated(reply2, getOffsetDateTime(2018, 5, 27, TIMEZONE));
      updateReplyCreated(reply3, getOffsetDateTime(2018, 5, 29, TIMEZONE));

      Reply[] allReplies = testBuilder.test1().replies().listReplies(metaform.getId(), REALM1_USER_1_ID,
        null, null, null, null,
        Boolean.TRUE, null, null, null);

      Reply[] createdBefore26 = testBuilder.test1().replies().listReplies(metaform.getId(), REALM1_USER_1_ID,
        getIsoDateTime(2018, 5, 26, TIMEZONE), null, null,
        null, Boolean.FALSE, null, null, null);

      Reply[] createdAfter26 = testBuilder.test1().replies().listReplies(metaform.getId(), REALM1_USER_1_ID,
        null, getIsoDateTime(2018, 5, 26, TIMEZONE), null,
        null, Boolean.FALSE, null, null, null);

      Assertions.assertEquals(3, allReplies.length);
      Assertions.assertEquals("test 1", allReplies[0].getData().get("text"));
      Assertions.assertEquals("test 2", allReplies[1].getData().get("text"));
      Assertions.assertEquals("test 3", allReplies[2].getData().get("text"));

      Assertions.assertEquals(1, createdBefore26.length);
      Assertions.assertEquals("test 1", createdBefore26[0].getData().get("text"));

      Assertions.assertEquals(2, createdAfter26.length);
      Assertions.assertEquals("test 2", createdAfter26[0].getData().get("text"));
      Assertions.assertEquals("test 3", createdAfter26[1].getData().get("text"));
    }
  }



  @Test
  public void listRepliesByModifiedBefore() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform parsedMetaform = testBuilder.metaformAdmin().metaforms().readMetaform("simple");
      Metaform metaform = testBuilder.metaformAdmin().metaforms().create(parsedMetaform);

      Reply reply1 = testBuilder.test1().replies().createSimpleReply(metaform, "test 1", ReplyMode.CUMULATIVE);
      Reply reply2 = testBuilder.test1().replies().createSimpleReply(metaform, "test 2", ReplyMode.CUMULATIVE);
      Reply reply3 = testBuilder.test1().replies().createSimpleReply(metaform, "test 3", ReplyMode.CUMULATIVE);

      updateReplyModified(reply1, getOffsetDateTime(2018, 5, 25, TIMEZONE));
      updateReplyModified(reply2, getOffsetDateTime(2018, 5, 27, TIMEZONE));
      updateReplyModified(reply3, getOffsetDateTime(2018, 5, 29, TIMEZONE));

      Reply[] allReplies = testBuilder.test1().replies().listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, null, Boolean.FALSE, null, null, null);
      Reply[] modifiedBefore26 = testBuilder.test1().replies().listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, getIsoDateTime(2018, 5, 26, TIMEZONE), null, Boolean.FALSE, null, null, null);
      Reply[] modifiedAfter26 = testBuilder.test1().replies().listReplies(metaform.getId(), REALM1_USER_1_ID, null, null, null, getIsoDateTime(2018, 5, 26, TIMEZONE), Boolean.FALSE, null, null, null);

      assertEquals(3, allReplies.length);
      assertEquals("test 1", allReplies[0].getData().get("text"));
      assertEquals("test 2", allReplies[1].getData().get("text"));
      assertEquals("test 3", allReplies[2].getData().get("text"));

      assertEquals(1, modifiedBefore26.length);
      assertEquals("test 1", modifiedBefore26[0].getData().get("text"));

      assertEquals(2, modifiedAfter26.length);
      assertEquals("test 2", modifiedAfter26[0].getData().get("text"));
      assertEquals("test 3", modifiedAfter26[1].getData().get("text"));
    }
  }
  
  @Test
  public void testMetafields() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform parsedMetaform = testBuilder.metaformAdmin().metaforms().readMetaform("simple-meta");
      Metaform metaform = testBuilder.metaformAdmin().metaforms().create(parsedMetaform);

      Reply createdReply = testBuilder.test1().replies().createSimpleReply(metaform, "test 1", ReplyMode.CUMULATIVE);
      String replyCreated = createdReply.getCreatedAt();
      String replyModified = createdReply.getModifiedAt();

      OffsetDateTime parsedCreated = OffsetDateTime.parse(replyCreated);
      OffsetDateTime parsedModified = OffsetDateTime.parse(replyModified);

      Reply reply = testBuilder.test1().replies().findReply(metaform.getId(), createdReply.getId(), (String) null);

      Assertions.assertEquals(parsedCreated.truncatedTo(ChronoUnit.MINUTES).toInstant(), parseOffsetDateTime((String) reply.getData().get("created")).truncatedTo(ChronoUnit.MINUTES).toInstant());
      Assertions.assertEquals(parsedModified.truncatedTo(ChronoUnit.MINUTES).toInstant(), parseOffsetDateTime((String) reply.getData().get("modified")).truncatedTo(ChronoUnit.MINUTES).toInstant());
      Assertions.assertEquals(REALM1_USER_1_ID.toString(), reply.getData().get("lastEditor"));
    }
  }

  @Test
  public void testFindReplyOwnerKeys() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform parsedMetaform = testBuilder.metaformAdmin().metaforms().readMetaform("simple-owner-keys");
      Metaform metaform = testBuilder.metaformAdmin().metaforms().create(parsedMetaform);

      Reply reply1 = testBuilder.test1().replies().createSimpleReply(metaform, "test 1", ReplyMode.CUMULATIVE);
      Reply reply2 = testBuilder.test1().replies().createSimpleReply(metaform, "test 2", ReplyMode.CUMULATIVE);

      Assertions.assertNotNull(reply1.getOwnerKey());
      Assertions.assertNotNull(reply2.getOwnerKey());

      Assertions.assertNotEquals(reply1.getOwnerKey(), reply2.getOwnerKey());

      testBuilder.anonymousToken().replies().findReply(metaform.getId(), reply1.getId(), reply1.getOwnerKey());

      testBuilder.anonymousToken().replies().assertReplyOwnerKeyFindForbidden(metaform, reply1, null);
      testBuilder.anonymousToken().replies().assertReplyOwnerKeyFindForbidden(metaform, reply1, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgZEinFR6yjNnV4utVbvU9KQ8lZasbAWKQJXjp2VzcyQfE1WvH5dMJmI-sgmPyaZFcJLk9tgLfZilytGmeopfafkWp7yVa9zWYAfni0eJmCL9EcemRb_rDNw07Mf1Vb1lbLhLth8r6a2SVGk_dK14TXeGQSX9zROmiqjeT_FXe2Yz8EnvUweLQ5TobVE-azzyW0dqzjoSkBNfZ8r2hot4hQ2mQthU5xaAnOfDeCc95E7cci4-Dnx3B8U_UaHOn7Srf6DdsL_ZDvnSh1CvYiGNOYMpk-TLK6ixH-m83rweBjQ1hl1N_5I3cplmuJGCJLBxDauyjfBYIfR9WkBoau1eDQIDAQAB");
      testBuilder.anonymousToken().replies().assertReplyOwnerKeyFindForbidden(metaform, reply1, reply2.getOwnerKey());

    }
  }

  @Test
  public void testUpdateReplyOwnerKeys() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform parsedMetaform = testBuilder.metaformAdmin().metaforms().readMetaform("simple-owner-keys");
      Metaform metaform = testBuilder.metaformAdmin().metaforms().create(parsedMetaform);

      Reply reply1 = testBuilder.test1().replies().createSimpleReply(metaform, "test 1", ReplyMode.CUMULATIVE);
      Reply reply2 = testBuilder.test1().replies().createSimpleReply(metaform, "test 2", ReplyMode.CUMULATIVE);

      Assertions.assertNotNull(reply1.getOwnerKey());
      Assertions.assertNotNull(reply2.getOwnerKey());
      Assertions.assertNotEquals(reply1.getOwnerKey(), reply2.getOwnerKey());

      testBuilder.anonymousToken().replies().updateReply(metaform.getId(), reply1.getId(), reply1, reply1.getOwnerKey());

      testBuilder.anonymousToken().replies().assertReplyOwnerKeyFindForbidden(metaform, reply1, null);
      testBuilder.anonymousToken().replies().assertReplyOwnerKeyFindForbidden(metaform, reply1, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgZEinFR6yjNnV4utVbvU9KQ8lZasbAWKQJXjp2VzcyQfE1WvH5dMJmI-sgmPyaZFcJLk9tgLfZilytGmeopfafkWp7yVa9zWYAfni0eJmCL9EcemRb_rDNw07Mf1Vb1lbLhLth8r6a2SVGk_dK14TXeGQSX9zROmiqjeT_FXe2Yz8EnvUweLQ5TobVE-azzyW0dqzjoSkBNfZ8r2hot4hQ2mQthU5xaAnOfDeCc95E7cci4-Dnx3B8U_UaHOn7Srf6DdsL_ZDvnSh1CvYiGNOYMpk-TLK6ixH-m83rweBjQ1hl1N_5I3cplmuJGCJLBxDauyjfBYIfR9WkBoau1eDQIDAQAB");
      testBuilder.anonymousToken().replies().assertReplyOwnerKeyFindForbidden(metaform, reply1, reply2.getOwnerKey());
    }
  }

  @Test
  public void testDeleteReplyOwnerKeys() throws Exception {
    TestBuilder testBuilder = new TestBuilder();
    Metaform parsedMetaform = testBuilder.metaformAdmin().metaforms().readMetaform("simple-owner-keys");
    Metaform metaform = testBuilder.metaformAdmin().metaforms().create(parsedMetaform);
    try {
      Reply reply1 = testBuilder.test1().replies().createSimpleReply(metaform, "test 1", ReplyMode.CUMULATIVE);
      Reply reply2 = testBuilder.test1().replies().createSimpleReply(metaform, "test 2", ReplyMode.CUMULATIVE);

      Assertions.assertNotNull(reply1.getOwnerKey());
      Assertions.assertNotNull(reply2.getOwnerKey());
      Assertions.assertNotEquals(reply1.getOwnerKey(), reply2.getOwnerKey());

      testBuilder.anonymousToken().replies().assertReplyOwnerKeyFindForbidden(metaform, reply1, null);
      testBuilder.anonymousToken().replies().assertReplyOwnerKeyFindForbidden(metaform, reply1,
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgZEinFR6yjNnV4utVbvU9KQ8lZasbAWKQJXjp2VzcyQfE1WvH5dMJmI-sgmPyaZFcJLk9tgLfZilytGmeopfafkWp7yVa9zWYAfni0eJmCL9EcemRb_rDNw07Mf1Vb1lbLhLth8r6a2SVGk_dK14TXeGQSX9zROmiqjeT_FXe2Yz8EnvUweLQ5TobVE-azzyW0dqzjoSkBNfZ8r2hot4hQ2mQthU5xaAnOfDeCc95E7cci4-Dnx3B8U_UaHOn7Srf6DdsL_ZDvnSh1CvYiGNOYMpk-TLK6ixH-m83rweBjQ1hl1N_5I3cplmuJGCJLBxDauyjfBYIfR9WkBoau1eDQIDAQAB");
      testBuilder.anonymousToken().replies().assertReplyOwnerKeyFindForbidden(metaform, reply1, reply2.getOwnerKey());

      testBuilder.anonymousToken().replies().delete(metaform.getId(), reply1, reply1.getOwnerKey());
    }
    finally {
      testBuilder.metaformSuper().metaforms().delete(metaform);
    }
  }

  @Test
  public void testExportReplyPdf() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      ExportTheme theme = testBuilder.metaformSuper().exportThemes().createSimpleExportTheme();
      testBuilder.metaformSuper().exportfiles().createSimpleExportThemeFile(theme.getId(), "reply/pdf.ftl", "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></meta><title>title</title></head><body>content</body></html>");

      Metaform parsedMetaform = testBuilder.metaformAdmin().metaforms().readMetaform("simple");
      Metaform metaform = testBuilder.metaformAdmin().metaforms().create(parsedMetaform);

      Metaform newMetaform = new Metaform(metaform.getId(), metaform.getReplyStrategy(), theme.getId(), metaform.getAllowAnonymous(),
        metaform.getAllowDrafts(), metaform.getAllowReplyOwnerKeys(), metaform.getAllowInvitations(), metaform.getAutosave(),
        metaform.getTitle(), metaform.getSections(), metaform.getFilters(), metaform.getScripts());

      testBuilder.metaformAdmin().metaforms().updateMetaform(newMetaform.getId(), newMetaform);
      Reply reply = testBuilder.test1().replies().createSimpleReply(metaform, "test 1", ReplyMode.UPDATE);
      assertPdfDownloadStatus(200, testBuilder.metaformAdmin().token(), metaform, reply);
    }
  }
  
  @Test
  public void testExportReplyPdfFilesEmpty() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      ExportTheme theme = testBuilder.metaformSuper().exportThemes().createSimpleExportTheme();
      testBuilder.metaformSuper().exportfiles().createSimpleExportThemeFile(theme.getId(), "reply/pdf.ftl", "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></meta><title>title</title></head><body>content</body></html>");

      Metaform parsedMetaform = testBuilder.metaformAdmin().metaforms().readMetaform("simple-files");
      Metaform metaform = testBuilder.metaformAdmin().metaforms().create(parsedMetaform);

      Metaform newMetaform = new Metaform(metaform.getId(), metaform.getReplyStrategy(), theme.getId(), metaform.getAllowAnonymous(),
        metaform.getAllowDrafts(), metaform.getAllowReplyOwnerKeys(), metaform.getAllowInvitations(), metaform.getAutosave(),
        metaform.getTitle(), metaform.getSections(), metaform.getFilters(), metaform.getScripts());

      testBuilder.metaformAdmin().metaforms().updateMetaform(newMetaform.getId(), newMetaform);
      Reply reply = testBuilder.test1().replies().createSimpleReply(metaform, "test 1", ReplyMode.UPDATE);
      assertPdfDownloadStatus(200, testBuilder.metaformAdmin().token(), metaform, reply);
    }
  }
}
