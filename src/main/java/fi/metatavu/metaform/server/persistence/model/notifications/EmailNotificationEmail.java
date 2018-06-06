package fi.metatavu.metaform.server.persistence.model.notifications;

import java.util.UUID;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * JPA entity representing email notification email
 * 
 * @author Antti Leppä
 */
@Entity
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
public class EmailNotificationEmail {

  @Id
  private UUID id;

  @ManyToOne(optional = false)
  private EmailNotification emailNotification;
  
  @Email
  @NotNull
  @NotEmpty
  private String email;
  
  public UUID getId() {
    return id;
  }
  
  public void setId(UUID id) {
    this.id = id;
  }
  
  public EmailNotification getEmailNotification() {
    return emailNotification;
  }
  
  public void setEmailNotification(EmailNotification emailNotification) {
    this.emailNotification = emailNotification;
  }
  
  public String getEmail() {
    return email;
  }
  
  public void setEmail(String email) {
    this.email = email;
  }
  
}
