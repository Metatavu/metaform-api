package fi.metatavu.metaform.server.notifications;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.metaform.server.persistence.model.Reply;

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
   * @param reply reply
   */
  public void notifyNewReply(Reply reply) {
    emailNotificationController.sendEmailNotifications(reply);
  }
  
}
