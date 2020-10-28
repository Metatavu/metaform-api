package fi.metatavu.metaform.test.functional;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import fi.metatavu.metaform.client.model.EmailNotification;
import fi.metatavu.metaform.client.api.EmailNotificationsApi;
import fi.metatavu.metaform.client.model.Metaform;
import fi.metatavu.metaform.server.rest.ReplyMode;

@SuppressWarnings ("squid:S1192")
public class EmailNotificationTestsIT extends AbstractIntegrationTest {

  @Test
  public void singleEmailNotificationTest() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      MailgunMocker mailgunMocker = startMailgunMocker();
      try {
        Metaform metaform = dataBuilder.createMetaform("simple");
        dataBuilder.createEmailNotification(metaform, "Simple subject", "Simple content", Arrays.asList("user@example.com"));
        dataBuilder.createSimpleReply(metaform, "val 1", ReplyMode.CUMULATIVE);
        mailgunMocker.verifyHtmlMessageSent("Metaform Test", "metaform-test@example.com", "user@example.com", "Simple subject", "Simple content");
      } finally {
        stopMailgunMocker(mailgunMocker);
      }
    } finally {
      dataBuilder.clean();
    }
  }
  
  @Test
  public void multipleEmailNotificationTest() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      MailgunMocker mailgunMocker = startMailgunMocker();
      try {
        Metaform metaform = dataBuilder.createMetaform("simple");
        dataBuilder.createEmailNotification(metaform, "Simple subject", "Simple content", Arrays.asList("user-1@example.com", "user-2@example.com"));
        dataBuilder.createSimpleReply(metaform, "val 1", ReplyMode.CUMULATIVE);
        mailgunMocker.verifyHtmlMessageSent("Metaform Test", "metaform-test@example.com", "user-1@example.com", "Simple subject", "Simple content");
        mailgunMocker.verifyHtmlMessageSent("Metaform Test", "metaform-test@example.com", "user-2@example.com", "Simple subject", "Simple content");
      } finally {
        stopMailgunMocker(mailgunMocker);
      }
    } finally {
      dataBuilder.clean();
    }
  }
  
  @Test
  public void replacedEmailNotificationTest() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      MailgunMocker mailgunMocker = startMailgunMocker();
      try {
        Metaform metaform = dataBuilder.createMetaform("simple");
        dataBuilder.createEmailNotification(metaform, "Replaced ${data.text} subject", "Replaced ${data.text} content", Arrays.asList("user@example.com"));
        dataBuilder.createSimpleReply(metaform, "val 1", ReplyMode.CUMULATIVE);
        mailgunMocker.verifyHtmlMessageSent("Metaform Test", "metaform-test@example.com", "user@example.com", "Replaced val 1 subject", "Replaced val 1 content");
      } finally {
        stopMailgunMocker(mailgunMocker);
      }
    } finally {
      dataBuilder.clean();
    }
  }
  
  @Test
  public void multipleRepliesEmailNotificationTest() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      MailgunMocker mailgunMocker = startMailgunMocker();
      try {
        Metaform metaform = dataBuilder.createMetaform("simple");
        dataBuilder.createEmailNotification(metaform, "Simple subject", "Simple content ${data.text}", Arrays.asList("user@example.com"));
        dataBuilder.createSimpleReply(metaform, "val 1", ReplyMode.CUMULATIVE);
        dataBuilder.createSimpleReply(metaform, "val 2", ReplyMode.CUMULATIVE);
        mailgunMocker.verifyHtmlMessageSent("Metaform Test", "metaform-test@example.com", "user@example.com", "Simple subject", "Simple content val 1");
        mailgunMocker.verifyHtmlMessageSent("Metaform Test", "metaform-test@example.com", "user@example.com", "Simple subject", "Simple content val 2");
      } finally {
        stopMailgunMocker(mailgunMocker);
      }
    } finally {
      dataBuilder.clean();
    }
  }
  
  @Test
  public void unicodeEmailNotificationTest() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      MailgunMocker mailgunMocker = startMailgunMocker();
      try {
        Metaform metaform = dataBuilder.createMetaform("simple");
        dataBuilder.createEmailNotification(metaform, "Simple subject ${data.text}", "Simple content ${data.text}", Arrays.asList("user@example.com"));
        dataBuilder.createSimpleReply(metaform, "ääkköset", ReplyMode.CUMULATIVE);
        dataBuilder.createSimpleReply(metaform, "Правда.Ру", ReplyMode.CUMULATIVE);
        mailgunMocker.verifyHtmlMessageSent("Metaform Test", "metaform-test@example.com", "user@example.com", "Simple subject ääkköset", "Simple content ääkköset");
        mailgunMocker.verifyHtmlMessageSent("Metaform Test", "metaform-test@example.com", "user@example.com", "Simple subject Правда.Ру", "Simple content Правда.Ру");
      } finally {
        stopMailgunMocker(mailgunMocker);
      }
    } finally {
      dataBuilder.clean();
    }
  }
  
  @Test
  public void testFindEmailNotification() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      EmailNotificationsApi adminEmailNotificationsApi = dataBuilder.getAdminEmailNotificationsApi();
      
      Metaform metaform = dataBuilder.createMetaform("simple");
      EmailNotification createdEmailNotification = dataBuilder.createEmailNotification(metaform, "Simple subject ${data.text}", "Simple content ${data.text}", Arrays.asList("user@example.com"));
      EmailNotification foundEmailNotification = adminEmailNotificationsApi.findEmailNotification(metaform.getId(), createdEmailNotification.getId());
      
      assertEquals(createdEmailNotification.toString(), foundEmailNotification.toString());
    } finally {
      dataBuilder.clean();
    }
  }
  
  @Test
  public void testListEmailNotifications() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      EmailNotificationsApi adminEmailNotificationsApi = dataBuilder.getAdminEmailNotificationsApi();
      
      Metaform metaform = dataBuilder.createMetaform("simple");
      
      EmailNotification notification1 = dataBuilder.createEmailNotification(metaform, "Subject 1", "Content 2", Arrays.asList("user@example.com"));
      EmailNotification notification2 = dataBuilder.createEmailNotification(metaform, "Subject 2", "Content 2", Arrays.asList("user@example.com"));
      
      List<EmailNotification> list = adminEmailNotificationsApi.listEmailNotifications(metaform.getId());
      
      assertEquals(2, list.size());

      EmailNotification listNotification1 = list.stream().filter(item -> item.getId().equals(notification1.getId())).findFirst().get();
      EmailNotification listNotification2 = list.stream().filter(item -> item.getId().equals(notification2.getId())).findFirst().get();
      
      assertEquals(notification1.toString(), listNotification1.toString());
      assertEquals(notification2.toString(), listNotification2.toString());
    } finally {
      dataBuilder.clean();
    }
  }
}
