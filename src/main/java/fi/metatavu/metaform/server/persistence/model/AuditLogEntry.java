package fi.metatavu.metaform.server.persistence.model;

import fi.metatavu.metaform.client.model.AuditLogEntryType;
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

  @Column(nullable = false)
  @NotNull
  private OffsetDateTime time;

  @Column
  private UUID replyId;

  @Column
  private UUID attachmentId;

  @Column
  private String message;

  @Column(nullable = false)
  @NotNull
  private AuditLogEntryType logEntryType;

  /**
   * @return id
   */
  public UUID getId() {
    return id;
}

  public void setId(UUID id) {
      this.id = id;
  }

  /**
   * @return userId
   */
  public UUID getUserId() {
      return userId;
  }

  public void setUserId(UUID userId) {
      this.userId = userId;
  }

  /**
   * @return time
   */
  public OffsetDateTime getTime() {
      return time;
  }

  public void setTime(OffsetDateTime time) {
      this.time = time;
  }

  /**
   * @return replyId
   */
  public UUID getReplyId() {
      return replyId;
  }

  public void setReplyId(UUID replyId) {
      this.replyId = replyId;
  }

  /**
   * @return attachmentId
   */
  public UUID getAttachmentId() {
      return attachmentId;
  }

  public void setAttachmentId(UUID attachmentId) {
      this.attachmentId = attachmentId;
  }

  /**
   * @return message
   */
  public String getMessage() {
      return message;
  }

  public void setMessage(String message) {
      this.message = message;
  }

  /**
   * @return logEntryType
   */
  public AuditLogEntryType getLogEntryType() {
      return logEntryType;
  }

  public void setLogEntryType(AuditLogEntryType logEntryType) {
      this.logEntryType = logEntryType;
  }

	@Override
	public String toString() {
		return "AuditLogEntry{" +
			"id=" + id +
			", userId=" + userId +
			", time=" + time +
			", replyId=" + replyId +
			", attachmentId=" + attachmentId +
			", message='" + message + '\'' +
			", logEntryType=" + logEntryType +
			'}';
	}
}
