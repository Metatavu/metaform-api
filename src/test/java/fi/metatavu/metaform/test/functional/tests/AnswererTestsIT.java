package fi.metatavu.metaform.test.functional.tests;

import fi.metatavu.metaform.api.client.models.Metaform;
import fi.metatavu.metaform.api.client.models.Reply;
import fi.metatavu.metaform.server.rest.ReplyMode;
import fi.metatavu.metaform.test.functional.AbstractIntegrationTest;
import fi.metatavu.metaform.test.functional.builder.TestBuilder;
import fi.metatavu.metaform.test.functional.builder.resources.KeycloakResource;
import fi.metatavu.metaform.test.functional.builder.resources.MysqlResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Tests for testing answerer user form functionality
 */
@QuarkusTest
@QuarkusTestResource.List(value = {
  @QuarkusTestResource(MysqlResource.class),
  @QuarkusTestResource(KeycloakResource.class)
})
@TestProfile(GeneralTestProfile.class)
public class AnswererTestsIT extends AbstractIntegrationTest {

  @Test
  public void testAnswererCreateForm() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform payload = new Metaform();
      testBuilder.answerer1.metaforms().assertCreateFailStatus(403, payload);
    }
  }

  @Test
  public void testAnswererFindForm() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform metaform = testBuilder.metaformAdmin.metaforms().createFromJsonFile("simple");
      Metaform foundMetaform = testBuilder.answerer1.metaforms().findMetaform(metaform.getId(), null, null);
      assertNotNull(foundMetaform);
      assertEquals("Simple", foundMetaform.getTitle());
    }
  }

  @Test
  public void testAnswererUpdateForm() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform metaform = testBuilder.metaformAdmin.metaforms().createFromJsonFile("simple");
      testBuilder.answerer1.metaforms().assertUpdateFailStatus(403, metaform);
    }
  }

  @Test
  public void testAnswererDeleteForm() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform metaform = testBuilder.metaformAdmin.metaforms().createFromJsonFile("simple");
      testBuilder.answerer1.metaforms().assertDeleteFailStatus(403, metaform);
    }
  }

  @Test
  public void testAllowAnonymousReply() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform metaform = testBuilder.metaformAdmin.metaforms().createFromJsonFile("simple-allow-anonymous");
      Reply reply = testBuilder.answerer1.replies().createSimpleReply(metaform, "TEST", ReplyMode.REVISION);
      assertNotNull(reply);
      testBuilder.metaformAdmin.replies().delete(metaform.getId(), reply, null);
    }
  }

  @Test
  public void testDisallowAnonymousReply() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform metaform = testBuilder.metaformAdmin.metaforms().createFromJsonFile("simple");
      Reply reply = testBuilder.answerer1.replies().createSimpleReply(metaform, "TEST", ReplyMode.REVISION);
      assertNotNull(reply);
      testBuilder.metaformAdmin.replies().delete(metaform.getId(), reply, null);
    }
  }

}
