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
import org.junit.jupiter.api.Test;

/**
 * Tests for testing anonymous user reply functionality
 */
@QuarkusTest
@QuarkusTestResource.List(value = {
  @QuarkusTestResource(MysqlResource.class),
  @QuarkusTestResource(KeycloakResource.class)
})
public class AnonymousReplyTestsIT extends AbstractIntegrationTest {

  @Test
  public void testAnonymousUpdateReplyOwnerKey() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform metaform1 = testBuilder.metaformAdmin().metaforms().createFromJsonFile("simple-owner-keys");
      Metaform metaform2 = testBuilder.metaformAdmin().metaforms().createFromJsonFile("simple-owner-keys");

      Reply reply1 = testBuilder.metaformAdmin().replies().createSimpleReply(metaform1, "TEST", ReplyMode.REVISION);
      Reply reply2 = testBuilder.metaformAdmin().replies().createSimpleReply(metaform1, "TEST", ReplyMode.REVISION);

      testBuilder.anon().replies().updateReply(metaform1.getId(), reply1.getId(), reply1, reply1.getOwnerKey());

      testBuilder.anon().replies().assertUpdateFailStatus(404, metaform2.getId(), reply1, reply1.getOwnerKey());
      testBuilder.anon().replies().assertUpdateFailStatus(404, metaform2.getId(), reply1, null);
      testBuilder.anon().replies().assertUpdateFailStatus(403, metaform1.getId(), reply2, reply1.getOwnerKey());
      testBuilder.anon().replies().assertUpdateFailStatus(403, metaform1.getId(), reply1, reply2.getOwnerKey());
      testBuilder.anon().replies().assertUpdateFailStatus(403, metaform1.getId(), reply1, null);
    }

  }
}
