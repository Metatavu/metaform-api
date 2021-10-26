package fi.metatavu.metaform.test.functional.tests;

import com.github.tomakehurst.wiremock.client.WireMock;
import fi.metatavu.metaform.api.client.models.EmailNotification;
import fi.metatavu.metaform.api.client.models.FieldRule;
import fi.metatavu.metaform.api.client.models.Metaform;
import fi.metatavu.metaform.server.rest.ReplyMode;
import fi.metatavu.metaform.test.functional.AbstractIntegrationTest;
import fi.metatavu.metaform.test.functional.MailgunMocker;
import fi.metatavu.metaform.test.functional.builder.TestBuilder;
import fi.metatavu.metaform.test.functional.builder.resources.KeycloakResource;
import fi.metatavu.metaform.test.functional.builder.resources.MysqlResource;
import fi.metatavu.metaform.test.functional.builder.resources.MailgunResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Quarkus tests for intents API
 */
@QuarkusTest
@QuarkusTestResource.List(value = {
  @QuarkusTestResource(MysqlResource.class),
  @QuarkusTestResource(KeycloakResource.class),
  @QuarkusTestResource(MailgunResource.class)
})
@TestProfile(GeneralTestProfile.class)
public class EmailNotificationTestsIT extends AbstractIntegrationTest {

  @BeforeAll
  public static void setMocker() {
    String host = ConfigProvider.getConfig().getValue("wiremock.host", String.class);

    int port =  Integer.parseInt(ConfigProvider.getConfig().getValue("wiremock.port", String.class));
    WireMock.configureFor(host, port);
  }

  @Test
  public void singleEmailNotificationTest() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform metaformData = testBuilder.metaformAdmin.metaforms().readMetaform("simple");
      Metaform metaform = testBuilder.metaformAdmin.metaforms().create(metaformData);
      MailgunMocker mailgunMocker = startMailgunMocker();
      try {
        testBuilder.metaformAdmin.emailNotifications().createEmailNotification(metaform, "Simple subject", "Simple content", Arrays.asList("user@example.com"));
        testBuilder.test1.replies().createSimpleReply(metaform, "val 1", ReplyMode.CUMULATIVE);
        mailgunMocker.verifyHtmlMessageSent("Metaform Test", "metaform-test@example.com", "user@example.com", "Simple subject", "Simple content");
      } finally {
        stopMailgunMocker(mailgunMocker);
      }
    }
  }

  @Test
  public void notifyIfEqualsTest() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform metaformData = testBuilder.metaformAdmin.metaforms().readMetaform("simple");
      Metaform metaform = testBuilder.metaformAdmin.metaforms().create(metaformData);
      MailgunMocker mailgunMocker = startMailgunMocker();

      try {
        testBuilder.metaformAdmin.emailNotifications().createEmailNotification(metaform.getId(), "subject", "content", Arrays.asList("val1@example.com"), createFieldRule("text", "val 1", null));
        testBuilder.metaformAdmin.emailNotifications().createEmailNotification(metaform.getId(), "subject", "content", Arrays.asList("val2@example.com"), createFieldRule("text", "val 2", null));

        testBuilder.test1.replies().createSimpleReply(metaform, "val 1", ReplyMode.CUMULATIVE);
        testBuilder.test1.replies().createSimpleReply(metaform, "val 3", ReplyMode.CUMULATIVE);

        mailgunMocker.verifyHtmlMessageSent(1, "Metaform Test", "metaform-test@example.com", "val1@example.com", "subject", "content");
        mailgunMocker.verifyHtmlMessageSent(0, "Metaform Test", "metaform-test@example.com", "val2@example.com", "subject", "content");

      } finally {
        stopMailgunMocker(mailgunMocker);
      }
    }
  }

  @Test
  public void notifyIfNotEqualsTest() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform metaformData = testBuilder.metaformAdmin.metaforms().readMetaform("simple");
      Metaform metaform = testBuilder.metaformAdmin.metaforms().create(metaformData);
      MailgunMocker mailgunMocker = startMailgunMocker();

      try {
        testBuilder.metaformAdmin.emailNotifications().createEmailNotification(metaform.getId(), "subject", "content", Arrays.asList("val1@example.com"), createFieldRule("text", null, "val 1"));
        testBuilder.metaformAdmin.emailNotifications().createEmailNotification(metaform.getId(), "subject", "content", Arrays.asList("val2@example.com"), createFieldRule("text", null, "val 2"));

        testBuilder.test1.replies().createSimpleReply(metaform, "val 1", ReplyMode.CUMULATIVE);
        testBuilder.test1.replies().createSimpleReply(metaform, "val 3", ReplyMode.CUMULATIVE);

        mailgunMocker.verifyHtmlMessageSent(1, "Metaform Test", "metaform-test@example.com", "val1@example.com", "subject", "content");
        mailgunMocker.verifyHtmlMessageSent(2, "Metaform Test", "metaform-test@example.com", "val2@example.com", "subject", "content");

      } finally {
        stopMailgunMocker(mailgunMocker);
      }
    }
  }

  @Test
  public void multipleEmailNotificationTest() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform metaformData = testBuilder.metaformAdmin.metaforms().readMetaform("simple");
      Metaform metaform = testBuilder.metaformAdmin.metaforms().create(metaformData);
      MailgunMocker mailgunMocker = startMailgunMocker();

      try {
        testBuilder.metaformAdmin.emailNotifications().createEmailNotification(metaform, "Simple subject", "Simple content", Arrays.asList("user-1@example.com", "user-2@example.com"));
        testBuilder.test1.replies().createSimpleReply(metaform, "val 1", ReplyMode.CUMULATIVE);

        mailgunMocker.verifyHtmlMessageSent("Metaform Test", "metaform-test@example.com", "user-1@example.com", "Simple subject", "Simple content");
        mailgunMocker.verifyHtmlMessageSent("Metaform Test", "metaform-test@example.com", "user-2@example.com", "Simple subject", "Simple content");
      } finally {
        stopMailgunMocker(mailgunMocker);
      }
    }
  }

  @Test
  public void replacedEmailNotificationTest() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform metaformData = testBuilder.metaformAdmin.metaforms().readMetaform("simple");
      Metaform metaform = testBuilder.metaformAdmin.metaforms().create(metaformData);
      MailgunMocker mailgunMocker = startMailgunMocker();

      try {
        testBuilder.metaformAdmin.emailNotifications().createEmailNotification(metaform, "Replaced ${data.text} subject", "Replaced ${data.text} content", Arrays.asList("user@example.com"));
        testBuilder.test1.replies().createSimpleReply(metaform, "val 1", ReplyMode.CUMULATIVE);

        mailgunMocker.verifyHtmlMessageSent("Metaform Test", "metaform-test@example.com", "user@example.com", "Replaced val 1 subject", "Replaced val 1 content");
      } finally {
        stopMailgunMocker(mailgunMocker);
      }
    }
  }

  @Test
  public void multipleRepliesEmailNotificationTest() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform metaformData = testBuilder.metaformAdmin.metaforms().readMetaform("simple");
      Metaform metaform = testBuilder.metaformAdmin.metaforms().create(metaformData);
      MailgunMocker mailgunMocker = startMailgunMocker();

      try {
        testBuilder.metaformAdmin.emailNotifications().createEmailNotification(metaform, "Simple subject", "Simple content ${data.text}", Arrays.asList("user@example.com"));
        testBuilder.test1.replies().createSimpleReply(metaform, "val 1", ReplyMode.CUMULATIVE);
        testBuilder.test1.replies().createSimpleReply(metaform, "val 2", ReplyMode.CUMULATIVE);

        mailgunMocker.verifyHtmlMessageSent("Metaform Test", "metaform-test@example.com", "user@example.com", "Simple subject", "Simple content val 1");
        mailgunMocker.verifyHtmlMessageSent("Metaform Test", "metaform-test@example.com", "user@example.com", "Simple subject", "Simple content val 2");
      } finally {
        stopMailgunMocker(mailgunMocker);
      }
    }
  }

  @Test
  public void unicodeEmailNotificationTest() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform metaform = testBuilder.metaformAdmin.metaforms().readMetaform("simple");
      Metaform createdMetaform = testBuilder.metaformAdmin.metaforms().create(metaform);
      MailgunMocker mailgunMocker = startMailgunMocker();

      try {
        testBuilder.metaformAdmin.emailNotifications().createEmailNotification(createdMetaform, "Simple subject ${data.text}", "Simple content ${data.text}", Arrays.asList("user@example.com"));
        testBuilder.test1.replies().createSimpleReply(createdMetaform, "ääkköset", ReplyMode.CUMULATIVE);
        testBuilder.test1.replies().createSimpleReply(createdMetaform, "Правда.Ру", ReplyMode.CUMULATIVE);

        mailgunMocker.verifyHtmlMessageSent("Metaform Test", "metaform-test@example.com", "user@example.com", "Simple subject ääkköset", "Simple content ääkköset");
        mailgunMocker.verifyHtmlMessageSent("Metaform Test", "metaform-test@example.com", "user@example.com", "Simple subject Правда.Ру", "Simple content Правда.Ру");
      } finally {
        stopMailgunMocker(mailgunMocker);
      }
    }
  }

  @Test
  public void testCreateEmailNotification() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform metaformData = testBuilder.metaformAdmin.metaforms().readMetaform("simple");
      Metaform metaform = testBuilder.metaformAdmin.metaforms().create(metaformData);
      MailgunMocker mailgunMocker = startMailgunMocker();

      try {
        EmailNotification createdEmailNotification = testBuilder.metaformAdmin.emailNotifications().createEmailNotification(metaform.getId(), "Simple subject ${data.text}", "Simple content ${data.text}", Arrays.asList("user@example.com"), createFieldRule("field", "eq", "neq"));
        EmailNotification foundEmailNotification = testBuilder.metaformAdmin.emailNotifications().findEmailNotification(metaform.getId(), createdEmailNotification.getId());

        Assertions.assertEquals(createdEmailNotification.toString(), foundEmailNotification.toString());
      } finally {
        stopMailgunMocker(mailgunMocker);
      }
    }
  }

  @Test
  public void testListEmailNotifications() throws Exception {
    try (TestBuilder testBuilder = new TestBuilder()) {
      Metaform metaform = testBuilder.metaformAdmin.metaforms().readMetaform("simple");
      Metaform createdMetaform = testBuilder.metaformAdmin.metaforms().create(metaform);
      MailgunMocker mailgunMocker = startMailgunMocker();

      try {
        EmailNotification notification1 = testBuilder.metaformAdmin.emailNotifications().createEmailNotification(createdMetaform, "Subject 1", "Content 2", Arrays.asList("user@example.com"));
        EmailNotification notification2 = testBuilder.metaformAdmin.emailNotifications().createEmailNotification(createdMetaform, "Subject 2", "Content 2", Arrays.asList("user@example.com"));

        List<EmailNotification> list = testBuilder.metaformAdmin.emailNotifications().listEmailNotifications(createdMetaform.getId());

        Assertions.assertEquals(2, list.size());

        EmailNotification listNotification1 = list.stream().filter(item -> item.getId().equals(notification1.getId())).findFirst().get();
        EmailNotification listNotification2 = list.stream().filter(item -> item.getId().equals(notification2.getId())).findFirst().get();

        Assertions.assertEquals(notification1.toString(), listNotification1.toString());
        Assertions.assertEquals(notification2.toString(), listNotification2.toString());
      } finally {
        stopMailgunMocker(mailgunMocker);
      }
    }
  }

  /*
   * Creates a field rule
   *
   * @param field field name
   * @param equals equals value
   * @param notEquals not equals value
   * @return created rule
   */
  private FieldRule createFieldRule(String field, String equals, String notEquals) {
    return createFieldRule(field, equals, notEquals, null, null);
  }

  /*
   * Creates a field rule
   *
   * @param field field name
   * @param equals equals value
   * @param notEquals not equals value
   * @param ands list of ands or null if none defined
   * @param ors list of ors or null if none defined
   * @return created rule
   */
  private FieldRule createFieldRule(String field, String equals, String notEquals, FieldRule[] ands, FieldRule[] ors) {
    return new FieldRule(field, equals, notEquals, ands, ors);
  }
}
