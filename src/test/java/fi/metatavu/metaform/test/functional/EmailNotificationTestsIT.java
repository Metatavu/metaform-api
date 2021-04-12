package fi.metatavu.metaform.test.functional;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

/**
 * Quarkus tests for intents API
 */
@QuarkusTest
@QuarkusTestResource.List(value = {
  @QuarkusTestResource(MysqlResource.class),
  @QuarkusTestResource(KeycloakResource.class)
})
@SuppressWarnings("squid:S1192")
public class EmailNotificationTestsIT {
/*
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
  public void notifyIfEqualsTest() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      MailgunMocker mailgunMocker = startMailgunMocker();
      try {
        Metaform metaform = dataBuilder.createMetaform("simple");
        
        dataBuilder.createEmailNotification(metaform, "subject", "content", Arrays.asList("val1@example.com"), createFieldRule("text", "val 1", null));
        dataBuilder.createEmailNotification(metaform, "subject", "content", Arrays.asList("val2@example.com"), createFieldRule("text", "val 2", null));

        dataBuilder.createSimpleReply(metaform, "val 1", ReplyMode.CUMULATIVE);
        dataBuilder.createSimpleReply(metaform, "val 3", ReplyMode.CUMULATIVE);
        mailgunMocker.verifyHtmlMessageSent(1, "Metaform Test", "metaform-test@example.com", "val1@example.com", "subject", "content");
        mailgunMocker.verifyHtmlMessageSent(0, "Metaform Test", "metaform-test@example.com", "val2@example.com", "subject", "content");
      } finally {
        stopMailgunMocker(mailgunMocker);
      }
    } finally {
      dataBuilder.clean();
    }
  }
  
  @Test
  public void notifyIfNotEqualsTest() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      MailgunMocker mailgunMocker = startMailgunMocker();
      try {
        Metaform metaform = dataBuilder.createMetaform("simple");
        
        dataBuilder.createEmailNotification(metaform, "subject", "content", Arrays.asList("val1@example.com"), createFieldRule("text", null, "val 1"));
        dataBuilder.createEmailNotification(metaform, "subject", "content", Arrays.asList("val2@example.com"), createFieldRule("text", null, "val 2"));

        dataBuilder.createSimpleReply(metaform, "val 1", ReplyMode.CUMULATIVE);
        dataBuilder.createSimpleReply(metaform, "val 3", ReplyMode.CUMULATIVE);
        mailgunMocker.verifyHtmlMessageSent(1, "Metaform Test", "metaform-test@example.com", "val1@example.com", "subject", "content");
        mailgunMocker.verifyHtmlMessageSent(2, "Metaform Test", "metaform-test@example.com", "val2@example.com", "subject", "content");
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
  public void testCreateEmailNotification() throws IOException, URISyntaxException {
    TestDataBuilder dataBuilder = new TestDataBuilder(this, REALM_1, "test1.realm1", "test");
    try {
      EmailNotificationsApi adminEmailNotificationsApi = dataBuilder.getAdminEmailNotificationsApi();
      
      Metaform metaform = dataBuilder.createMetaform("simple");
      EmailNotification createdEmailNotification = dataBuilder.createEmailNotification(metaform, "Simple subject ${data.text}", "Simple content ${data.text}", Arrays.asList("user@example.com"), createFieldRule("field", "eq", "neq"));
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
  

   * Creates a field rule
   * 
   * @param field field name
   * @param equals equals value
   * @param notEquals not equals value
   * @return created rule

  private FieldRule createFieldRule(String field, String equals, String notEquals) {
    return createFieldRule(field, equals, notEquals, null, null);
  }
  

   * Creates a field rule
   * 
   * @param field field name
   * @param equals equals value
   * @param notEquals not equals value
   * @param ands list of ands or null if none defined
   * @param ors list of ors or null if none defined
   * @return created rule

  private FieldRule createFieldRule(String field, String equals, String notEquals, List<FieldRule> ands, List<FieldRule> ors) {
    FieldRule rule = new FieldRule();
    rule.setField(field);
    rule.setEquals(equals);
    rule.setNotEquals(notEquals);
    rule.setAnd(ands);
    rule.setOr(ors);
    return rule;
  }
  */
}
