package fi.metatavu.metaform.server.email.mailgun;

/**
 * Mailgun email provider constants
 * 
 * @author Heikki Kurhinen
 * @author Antti Leppä
 */
public class MailgunConsts {

  public static final String API_URL_SETTING_KEY = "mailgun.api_url";
  public static final String DOMAIN_SETTING_KEY = "mailgun.domain";
  public static final String API_KEY_SETTING_KEY = "mailgun.api_key";
  public static final String SENDER_EMAIL_SETTING_KEY = "mailgun.sender_email";
  public static final String SENDER_NAME_SETTING_KEY = "mailgun.sender_name";

  private MailgunConsts() {
  }
  
}
