package fi.metatavu.metaform.server.persistence.model;

import fi.metatavu.metaform.api.spec.model.AuditLogEntryType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA entity representing audit log entry
 *
 * @author Katja Danilova
 */
@Entity
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
public class AuditLogEntry {

  @Id
  private UUID id;

  @Column(nullable = false)
  @NotNull
  private UUID userId;

  @Column (nullable = false)
  private OffsetDateTime createdAt;

  @Column
  private UUID replyId;

  @Column
  private UUID attachmentId;

  @Column
  private String message;

  @Column(nullable = false)
  @NotNull
  private AuditLogEntryType logEntryType;

  @ManyToOne(optional = false)
  private Metaform metaform;

  /**
   * Gets audit log entry id
   *
   * @return id
   */
  public UUID getId() {
    return id;
}

  /**
   * Sets audit log entry id
   *
   * @param id id
   */
  public void setId(UUID id) {
      this.id = id;
  }

  /**
   * Gets id of user who caused creation of the audit log entry
   *
   * @return id of user who caused creation of the audit log entry
   */
  public UUID getUserId() {
      return userId;
  }

  /**
   * Sets userId
   *
   * @param userId userId
   */
  public void setUserId(UUID userId) {
      this.userId = userId;
  }

  /**
   * Gets time of the log creation
   *
   * @return time
   */
  public OffsetDateTime getCreatedAt() {
      return createdAt;
  }

  /**
   * Sets log creation time
   *
   * @param createdAt time
   */
  public void setCreatedAt(OffsetDateTime createdAt) {
      this.createdAt = createdAt;
  }

  /**
   * Gets replyId
   *
   * @return replyId
   */
  public UUID getReplyId() {
      return replyId;
  }

  /**
   * Sets replyId
   *
   * @param replyId replyId
   */
  public void setReplyId(UUID replyId) {
      this.replyId = replyId;
  }

  /**
   * Gets attachmentId
   *
   * @return attachmentId
   */
  public UUID getAttachmentId() {
      return attachmentId;
  }

  /**
   * Sets attachmentId
   *
   * @param attachmentId attachmentId
   */
  public void setAttachmentId(UUID attachmentId) {
      this.attachmentId = attachmentId;
  }

  /**
   * Gets log message
   *
   * @return message
   */
  public String getMessage() {
      return message;
  }

  /**
   * Sets log message
   *
   * @param message message
   */
  public void setMessage(String message) {
      this.message = message;
  }

  /**
   * Gets log entry type
   *
   * @return logEntryType
   */
  public AuditLogEntryType getLogEntryType() {
      return logEntryType;
  }

  /**
   * Sets log entry type
   *
   * @param logEntryType logEntryType
   */
  public void setLogEntryType(AuditLogEntryType logEntryType) {
      this.logEntryType = logEntryType;
  }

  /**
   * Gets metaform
   *
   * @return metaform
   */
  public Metaform getMetaform() {
    return metaform;
  }

  /**
   * Sets metaform
   *
   * @param metaform metaform
   */
  public void setMetaform(Metaform metaform) {
    this.metaform = metaform;
  }

  @PrePersist
  public void onCreate() {
    setCreatedAt(OffsetDateTime.now());
  }
}
