package fi.metatavu.metaform.server.persistence.model;

import java.util.UUID;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * JPA entity representing single item in attachment reply field
 * 
 * @author Antti Leppä
 */
@Entity
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Table(uniqueConstraints = { 
  @UniqueConstraint(columnNames = { "field_id", "attachment_id" })    
})
public class AttachmentReplyFieldItem {

  @Id
  private UUID id;

  @ManyToOne(optional = false)
  private AttachmentReplyField field;

  @ManyToOne
  private Attachment attachment;
  
  public void setId(UUID id) {
    this.id = id;
  }
  
  public UUID getId() {
    return id;
  }

  public Attachment getAttachment() {
    return attachment;
  }
  
  public void setAttachment(Attachment attachment) {
    this.attachment = attachment;
  }

  public AttachmentReplyField getField() {
    return field;
  }
  
  public void setField(AttachmentReplyField field) {
    this.field = field;
  }
  
}
