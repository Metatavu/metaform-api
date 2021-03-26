package fi.metatavu.metaform.test.functional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import fi.metatavu.metaform.api.client.models.Draft;
import fi.metatavu.metaform.api.client.models.Metaform;
import fi.metatavu.metaform.test.functional.builder.TestBuilder;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


@SuppressWarnings ("squid:S1192")@QuarkusTest
@QuarkusTestResource.List(value = {
  @QuarkusTestResource(MysqlResource.class),
  @QuarkusTestResource(KeycloakResource.class)
})
public class DraftTestsIT extends AbstractIntegrationTest{

  @Test
  public void createDraft() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform parsedMetaform =  testBuilder.metaformAdmin().metaforms().readMetaform("simple");
      Metaform metaform = testBuilder.metaformAdmin().metaforms().create(parsedMetaform);

      Map<String, Object> draftData = new HashMap<>();
      draftData.put("text", "draft value");

      Draft createdDraft = testBuilder.test1().drafts().createDraft(metaform, draftData);

      Assertions.assertNotNull(createdDraft);
      Assertions.assertNotNull(createdDraft.getId());
      Assertions.assertEquals("draft value", createdDraft.getData().get("text"));

      Draft foundDraft = testBuilder.test1().drafts().findDraft(metaform.getId(), createdDraft.getId());

      Assertions.assertNotNull(foundDraft);
      Assertions.assertEquals(createdDraft.getId(), foundDraft.getId());
      Assertions.assertEquals(createdDraft.getData().get("text"), foundDraft.getData().get("text"));
    }
  }

  @Test
  public void updateDraft() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform parsedMetaform =  testBuilder.metaformAdmin().metaforms().readMetaform("simple");
      Metaform metaform = testBuilder.metaformAdmin().metaforms().create(parsedMetaform);

      Map<String, Object> draftData = new HashMap<>();
      draftData.put("text", "draft value");

      Draft createdDraft = testBuilder.test1().drafts().createDraft(metaform, draftData);
      Assertions.assertNotNull(createdDraft);
      Assertions.assertNotNull(createdDraft.getId());
      Assertions.assertEquals("draft value", createdDraft.getData().get("text"));

      draftData.put("text", "updated value");
      Draft updatePayload = new Draft(draftData, createdDraft.getId(), createdDraft.getCreatedAt(), createdDraft.getModifiedAt());

      Draft updateDraft = testBuilder.test1().drafts().updateDraft(metaform.getId(), createdDraft.getId(), updatePayload);
      Assertions.assertNotNull(updateDraft);
      Assertions.assertEquals(createdDraft.getId(), updateDraft.getId());
      Assertions.assertEquals("updated value", updateDraft.getData().get("text"));

      Draft foundDraft = testBuilder.test1().drafts().findDraft(metaform.getId(), createdDraft.getId());
      Assertions.assertNotNull(foundDraft);
      Assertions.assertEquals(updateDraft.getId(), foundDraft.getId());
      Assertions.assertEquals(updateDraft.getData().get("text"), foundDraft.getData().get("text"));
    }
  }
}
