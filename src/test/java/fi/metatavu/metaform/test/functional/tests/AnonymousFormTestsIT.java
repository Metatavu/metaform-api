package fi.metatavu.metaform.test.functional.tests;

import fi.metatavu.metaform.client.model.Metaform;
import fi.metatavu.metaform.client.model.Reply;
import fi.metatavu.metaform.test.functional.builder.TestBuilder;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for testing anonymous user form functionality
 */
public class AnonymousFormTestsIT {

  @Test
  public void testAnonymousCreateForm() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform payload = new Metaform();
      testBuilder.anon().metaforms().assertCreateFailStatus(403, payload);
    }
  }

  @Test
  public void testAnonymousFindForm() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform metaform = testBuilder.admin().metaforms().createFromJsonFile("simple");
      testBuilder.anon().metaforms().assertFindFailStatus(403, metaform.getId());
    }
  }

  @Test
  public void testAnonymousFindFormOwnerKey() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform metaform1 = testBuilder.admin().metaforms().createFromJsonFile("simple-owner-keys");
      Metaform metaform2 = testBuilder.admin().metaforms().createFromJsonFile("simple-owner-keys");

      testBuilder.anon().metaforms().assertFindFailStatus(403, metaform1.getId());
      Reply reply1 = testBuilder.admin().replies().createSimpleReply(metaform1, "TEST");
      Reply reply2 = testBuilder.admin().replies().createSimpleReply(metaform1, "TEST");
      Reply reply3 = testBuilder.admin().replies().createSimpleReply(metaform2, "TEST");

      testBuilder.anon().metaforms().assertFindFailStatus(403, metaform1.getId(), reply1.getId(), reply2.getOwnerKey());
      testBuilder.anon().metaforms().assertFindFailStatus(403, metaform2.getId(), reply1.getId(), reply1.getOwnerKey());
      testBuilder.anon().metaforms().assertFindFailStatus(403, metaform1.getId(), reply3.getId(), reply3.getOwnerKey());
      testBuilder.anon().metaforms().assertFindFailStatus(403, metaform1.getId(), null, reply1.getOwnerKey());
      testBuilder.anon().metaforms().assertFindFailStatus(403, metaform1.getId(), reply1.getId(), null);
      assertNotNull(testBuilder.anon().metaforms().findMetaform(metaform1.getId(), reply1.getId(), reply1.getOwnerKey()));
    }
  }

  @Test
  public void testAnonymousUpdateForm() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform metaform = testBuilder.admin().metaforms().createFromJsonFile("simple");
      testBuilder.anon().metaforms().assertUpdateFailStatus(403, metaform);
    }
  }

  @Test
  public void testAnonymousDeletForm() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform metaform = testBuilder.admin().metaforms().createFromJsonFile("simple");
      testBuilder.anon().metaforms().assertDeleteFailStatus(403, metaform);
    }
  }

}
