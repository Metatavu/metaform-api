package fi.metatavu.metaform.server.notifications;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.metaform.server.persistence.model.Metaform;
import fi.metatavu.metaform.server.rest.model.Reply;

/**
 * Controller for notfications
 * 
 * @author Heikki Kurhinen
 * @author Antti Leppä
 *
 */
@ApplicationScoped
public class NotificationController {
  
  @Inject
  private EmailNotificationController emailNotificationController;

  /**
   * Sends all notifications for a new reply
   * 
   * @param metaform metaform JPA object
   * @param reply reply REST object
   */
  public void notifyNewReply(Metaform metaform, Reply reply) {
    emailNotificationController.sendEmailNotifications(metaform,reply);
  }
  
}
