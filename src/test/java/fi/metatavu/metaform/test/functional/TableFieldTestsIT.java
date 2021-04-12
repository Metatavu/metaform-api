package fi.metatavu.metaform.test.functional;

import fi.metatavu.metaform.api.client.models.Metaform;
import fi.metatavu.metaform.api.client.models.Reply;
import fi.metatavu.metaform.server.rest.ReplyMode;
import fi.metatavu.metaform.test.functional.builder.TestBuilder;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


@QuarkusTest
@QuarkusTestResource.List(value = {
  @QuarkusTestResource(MysqlResource.class),
  @QuarkusTestResource(KeycloakResource.class)
})
public class TableFieldTestsIT extends AbstractIntegrationTest {

  @Test
  public void createTableReply() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform parsedMetaform = testBuilder.metaformAdmin().metaforms().readMetaform("simple-table");
      Metaform metaform = testBuilder.metaformAdmin().metaforms().create(parsedMetaform);

      List<Map<String, Object>> tableData = Arrays.asList(createSimpleTableRow("Text 1", 10d), createSimpleTableRow("Text 2", 20d));
      Map<String, Object> replyData = new HashMap<>();
      replyData.put("table", tableData);

      Reply reply = testBuilder.test1().replies().createReplyWithData(replyData);
      Reply createdReply = testBuilder.test1().replies().create(metaform.getId(), null, ReplyMode.REVISION.toString(), reply);

      Reply foundReply = testBuilder.test1().replies().findReply(metaform.getId(), createdReply.getId(), null);
      Assertions.assertNotNull(foundReply);
      Assertions.assertNotNull(foundReply.getId());
      testBuilder.test1().replies().assertTableDataEquals(replyData, foundReply.getData());

    }
  }

  @Test
  public void updateTableReply() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform parsedMetaform = testBuilder.metaformAdmin().metaforms().readMetaform("simple-table");
      Metaform metaform = testBuilder.metaformAdmin().metaforms().create(parsedMetaform);

      Map<String, Object> createReplyData = new HashMap<>();
      createReplyData.put("table", Arrays.asList(createSimpleTableRow("Text 1", 10d), createSimpleTableRow("Text 2", 20d)));

      Reply reply = testBuilder.test1().replies().createReplyWithData(createReplyData);
      Reply createdReply = testBuilder.test1().replies().create(metaform.getId(), null, ReplyMode.REVISION.toString(), reply);

      testBuilder.test1().replies().assertTableDataEquals(createReplyData, createdReply.getData());

      Reply foundReply = testBuilder.test1().replies().findReply(metaform.getId(), createdReply.getId(), null);
      assertNotNull(foundReply);
      assertNotNull(foundReply.getId());

      testBuilder.test1().replies().assertTableDataEquals(createReplyData, foundReply.getData());

      Map<String, Object> updateReplyData = new HashMap<>();
      updateReplyData.put("table", Arrays.asList(createSimpleTableRow("Added new text", -210d), createSimpleTableRow("Text 1", 10d), createSimpleTableRow("Updated Text 2", 45.5d)));
      Reply updateReply = testBuilder.test1().replies().createReplyWithData(updateReplyData);
      testBuilder.test1().replies().updateReply(metaform.getId(), createdReply.getId(), updateReply, null);

      testBuilder.test1().replies().assertTableDataEquals(updateReplyData, updateReply.getData());

      Reply foundUpdatedReply = testBuilder.test1().replies().findReply(metaform.getId(), createdReply.getId(), null);
      assertNotNull(foundUpdatedReply);
      assertNotNull(foundUpdatedReply.getId());
      testBuilder.test1().replies().assertTableDataEquals(updateReplyData, foundUpdatedReply.getData());
    }
  }

  @Test
  public void nulledTableReply() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform parsedMetaform = testBuilder.metaformAdmin().metaforms().readMetaform("simple-table");
      Metaform metaform = testBuilder.metaformAdmin().metaforms().create(parsedMetaform);

      List<Map<String, Object>> tableData = Arrays.asList(createSimpleTableRow(null, null));
      Map<String, Object> replyData = new HashMap<>();
      replyData.put("table", tableData);

      Reply reply = testBuilder.test1().replies().createReplyWithData(replyData);
      Reply createdReply = testBuilder.test1().replies().create(metaform.getId(), null, ReplyMode.REVISION.toString(), reply);
      Reply foundReply = testBuilder.test1().replies().findReply(metaform.getId(), createdReply.getId(), null);

      assertNotNull(foundReply);
      assertNotNull(foundReply.getId());
      testBuilder.test1().replies().assertTableDataEquals(replyData, foundReply.getData());
    }
  }

  @Test
  public void nullTableReply() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform parsedMetaform = testBuilder.metaformAdmin().metaforms().readMetaform("simple-table");
      Metaform metaform = testBuilder.metaformAdmin().metaforms().create(parsedMetaform);

      Map<String, Object> replyData = new HashMap<>();
      replyData.put("table", null);

      Reply reply = testBuilder.test1().replies().createReplyWithData(replyData);
      Reply createdReply = testBuilder.test1().replies().create(metaform.getId(), null, ReplyMode.REVISION.toString(), reply);

      Reply foundReply = testBuilder.test1().replies().findReply(metaform.getId(), createdReply.getId(), null);
      assertNotNull(foundReply);
      assertNotNull(foundReply.getId());
      assertNull(foundReply.getData().get("table"));
    }
  }

  @Test
  public void invalidTableReply() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform parsedMetaform = testBuilder.metaformAdmin().metaforms().readMetaform("simple-table");
      Metaform metaform = testBuilder.metaformAdmin().metaforms().create(parsedMetaform);

      Map<String, Object> replyData = new HashMap<>();
      replyData.put("table", "table data");

      Reply reply = testBuilder.test1().replies().createReplyWithData(replyData);
      testBuilder.test1().replies().assertCreateFailStatus(400, metaform.getId(), null, ReplyMode.REVISION.toString(), reply);
    }
  }

  @Test
  public void deleteTableReply() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform parsedMetaform = testBuilder.metaformAdmin().metaforms().readMetaform("simple-table");
      Metaform metaform = testBuilder.metaformAdmin().metaforms().create(parsedMetaform);

      List<Map<String, Object>> tableData = Arrays.asList(createSimpleTableRow("Text 1", 10d), createSimpleTableRow("Text 2", 20d));
      Map<String, Object> replyData = new HashMap<>();
      replyData.put("table", tableData);

      Reply reply = testBuilder.test1().replies().createReplyWithData(replyData);
      Reply createdReply = testBuilder.test1().replies().create(metaform.getId(), null, ReplyMode.REVISION.toString(), reply);
      Reply foundReply = testBuilder.test1().replies().findReply(metaform.getId(), createdReply.getId(), null);
      assertNotNull(foundReply);
      testBuilder.metaformAdmin().replies().delete(metaform.getId(), createdReply, null);
      testBuilder.test1().replies().assertFindFailStatus(404, metaform.getId(), createdReply.getId(), null);
    }
  }
}
