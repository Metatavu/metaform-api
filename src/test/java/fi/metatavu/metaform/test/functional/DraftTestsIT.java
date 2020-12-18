package fi.metatavu.metaform.test.functional;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import fi.metatavu.metaform.client.model.Metaform;
import fi.metatavu.metaform.client.api.MetaformsApi;
import fi.metatavu.metaform.client.api.DraftsApi;
import fi.metatavu.metaform.client.model.Draft;

@SuppressWarnings ("squid:S1192")
public class DraftTestsIT extends AbstractIntegrationTest {
  
  @Test
  public void createDraft() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      Metaform metaform = dataBuilder.createMetaform("simple");
      
      Map<String, Object> draftData = new HashMap<>();
      draftData.put("text", "draft value");

      Draft createdDraft = dataBuilder.createDraft(metaform, draftData);
      assertNotNull(createdDraft);
      assertNotNull(createdDraft.getId());
      assertEquals("draft value", createdDraft.getData().get("text"));      

      Draft foundDraft = dataBuilder.getDraftsApi().findDraft(metaform.getId(), createdDraft.getId());
      assertNotNull(foundDraft);
      assertEquals(createdDraft.getId(), foundDraft.getId());
      assertEquals(createdDraft.getData().get("text"), foundDraft.getData().get("text"));
    } finally {
      dataBuilder.clean();
    }
  }

  @Test
  public void updateDraft() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      Metaform metaform = dataBuilder.createMetaform("simple");
      
      Map<String, Object> draftData = new HashMap<>();
      draftData.put("text", "draft value");

      Draft createdDraft = dataBuilder.createDraft(metaform, draftData);
      assertNotNull(createdDraft);
      assertNotNull(createdDraft.getId());
      assertEquals("draft value", createdDraft.getData().get("text"));

      Draft updatePayload = createdDraft;
      draftData.put("text", "updated value");
      updatePayload.setData(draftData);
      
      Draft updateDraft = dataBuilder.getDraftsApi().updateDraft(metaform.getId(), createdDraft.getId(), updatePayload);
      assertNotNull(updateDraft);
      assertEquals(createdDraft.getId(), updateDraft.getId());
      assertEquals("updated value", updateDraft.getData().get("text"));

      Draft foundDraft = dataBuilder.getDraftsApi().findDraft(metaform.getId(), createdDraft.getId());
      assertNotNull(foundDraft);
      assertEquals(updateDraft.getId(), foundDraft.getId());
      assertEquals(updateDraft.getData().get("text"), foundDraft.getData().get("text"));
    } finally {
      dataBuilder.clean();
    }
  }

  
  
}
