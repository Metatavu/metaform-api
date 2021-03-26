package fi.metatavu.metaform.test.functional.tests;

import fi.metatavu.metaform.api.client.models.Reply;
import fi.metatavu.metaform.api.spec.model.Metaform;
import fi.metatavu.metaform.test.functional.builder.TestBuilder;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Tests for testing anonymous user reply functionality
 */
public class AnonymousReplyTestsIT {

  /*@Test
  public void testAnonymousUpdateReplyOwnerKey() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform metaform1 = testBuilder.admin().metaforms().createFromJsonFile("simple-owner-keys");
      Metaform metaform2 = testBuilder.admin().metaforms().createFromJsonFile("simple-owner-keys");

      Reply reply1 = testBuilder.admin().replies().createSimpleReply(metaform1, "TEST");
      Reply reply2 = testBuilder.admin().replies().createSimpleReply(metaform1, "TEST");

      testBuilder.anon().replies().updateReply(metaform1.getId(), reply1, reply1.getOwnerKey());

      testBuilder.anon().replies().assertUpdateFailStatus(404, metaform2.getId(), reply1, reply1.getOwnerKey());
      testBuilder.anon().replies().assertUpdateFailStatus(404, metaform2.getId(), reply1, null);
      testBuilder.anon().replies().assertUpdateFailStatus(403, metaform1.getId(), reply2, reply1.getOwnerKey());
      testBuilder.anon().replies().assertUpdateFailStatus(403, metaform1.getId(), reply1, reply2.getOwnerKey());
      testBuilder.anon().replies().assertUpdateFailStatus(403, metaform1.getId(), reply1, null);
    }

  }*/

}
