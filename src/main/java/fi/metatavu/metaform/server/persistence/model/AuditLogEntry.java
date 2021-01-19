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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public OffsetDateTime getTime() {
        return time;
    }

    public void setTime(OffsetDateTime time) {
        this.time = time;
    }

    public UUID getReplyId() {
        return replyId;
    }

    public void setReplyId(UUID replyId) {
        this.replyId = replyId;
    }

    public UUID getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(UUID attachmentId) {
        this.attachmentId = attachmentId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public AuditLogEntryType getLogEntryType() {
        return logEntryType;
    }

    public void setLogEntryType(AuditLogEntryType logEntryType) {
        this.logEntryType = logEntryType;
    }

}
