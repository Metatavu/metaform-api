package fi.metatavu.metaform.server.test.functional.tests;

import fi.metatavu.metaform.api.client.models.Metaform;
import fi.metatavu.metaform.api.client.models.Reply;
import fi.metatavu.metaform.server.rest.ReplyMode;
import fi.metatavu.metaform.server.rest.ReplyMode;
import fi.metatavu.metaform.server.test.functional.AbstractIntegrationTest;
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder;
import fi.metatavu.metaform.server.test.functional.builder.resources.KeycloakResource;
import fi.metatavu.metaform.server.test.functional.builder.resources.MysqlResource;
import fi.metatavu.metaform.server.test.functional.AbstractIntegrationTest;
import fi.metatavu.metaform.server.test.functional.builder.TestBuilder;
import fi.metatavu.metaform.server.test.functional.builder.resources.KeycloakResource;
import fi.metatavu.metaform.server.test.functional.builder.resources.MysqlResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Tests for testing anonymous user form functionality
 */
@QuarkusTest
@QuarkusTestResource.List(value = {
  @QuarkusTestResource(MysqlResource.class),
  @QuarkusTestResource(KeycloakResource.class)
})
@TestProfile(GeneralTestProfile.class)
public class AnonymousFormTestsIT extends AbstractIntegrationTest {

  @Test
  public void testAnonymousCreateForm() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform payload = new Metaform();
      testBuilder.anon.metaforms().assertCreateFailStatus(403, payload);
    }
  }

  @Test
  public void testAnonymousFindForm() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform metaform = testBuilder.metaformAdmin.metaforms().createFromJsonFile("simple");
      testBuilder.anon.metaforms().assertFindFailStatus(403, metaform.getId());
    }
  }

  @Test
  public void testAnonymousFindFormOwnerKey() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform metaform1 = testBuilder.metaformAdmin.metaforms().createFromJsonFile("simple-owner-keys");
      Metaform metaform2 = testBuilder.metaformAdmin.metaforms().createFromJsonFile("simple-owner-keys");

      testBuilder.anon.metaforms().assertFindFailStatus(403, metaform1.getId());
      Reply reply1 = testBuilder.metaformAdmin.replies().createSimpleReply(metaform1, "TEST", ReplyMode.REVISION);
      Reply reply2 = testBuilder.metaformAdmin.replies().createSimpleReply(metaform1, "TEST", ReplyMode.REVISION);
      Reply reply3 = testBuilder.metaformAdmin.replies().createSimpleReply(metaform2, "TEST", ReplyMode.REVISION);

      testBuilder.anon.metaforms().assertFindFailStatus(403, metaform1.getId(), reply1.getId(), reply2.getOwnerKey());
      testBuilder.anon.metaforms().assertFindFailStatus(403, metaform2.getId(), reply1.getId(), reply1.getOwnerKey());
      testBuilder.anon.metaforms().assertFindFailStatus(403, metaform1.getId(), reply3.getId(), reply3.getOwnerKey());
      testBuilder.anon.metaforms().assertFindFailStatus(403, metaform1.getId(), null, reply1.getOwnerKey());
      testBuilder.anon.metaforms().assertFindFailStatus(403, metaform1.getId(), reply1.getId(), null);
      Assertions.assertNotNull(testBuilder.anon.metaforms().findMetaform(metaform1.getId(), reply1.getId(), reply1.getOwnerKey()));
    }
  }

  @Test
  public void testAnonymousUpdateForm() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform metaform = testBuilder.metaformAdmin.metaforms().createFromJsonFile("simple");
      testBuilder.anon.metaforms().assertUpdateFailStatus(403, metaform.getId(), metaform);
    }
  }

  @Test
  public void testAnonymousDeletForm() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform metaform = testBuilder.metaformAdmin.metaforms().createFromJsonFile("simple");
      testBuilder.anon.metaforms().assertDeleteFailStatus(403, metaform);
    }
  }
}
