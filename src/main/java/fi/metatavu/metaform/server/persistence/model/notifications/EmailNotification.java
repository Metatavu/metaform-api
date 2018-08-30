package fi.metatavu.metaform.server.persistence.model.notifications;

import java.time.OffsetDateTime;
import java.util.UUID;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;

import fi.metatavu.metaform.server.persistence.model.Metaform;

/**
 * JPA entity representing Metaform email notications
 * 
 * @author Antti Leppä
 */
@Entity
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
public class EmailNotification {

  @Id
  @Type(type="org.hibernate.type.PostgresUUIDType")
  private UUID id;

  @Lob
  @Type(type = "org.hibernate.type.TextType")
  private String subjectTemplate;

  @Lob
  @Type(type = "org.hibernate.type.TextType")
  private String contentTemplate;

  @ManyToOne(optional = false)
  private Metaform metaform;
  
  @Column (nullable = false)
  private OffsetDateTime createdAt;

  @Column (nullable = false)
  private OffsetDateTime modifiedAt;
  
  public UUID getId() {
    return id;
  }
  
  public void setId(UUID id) {
    this.id = id;
  }
  
  public Metaform getMetaform() {
    return metaform;
  }
  
  public void setMetaform(Metaform metaform) {
    this.metaform = metaform;
  }
  
  public String getContentTemplate() {
    return contentTemplate;
  }
  
  public void setContentTemplate(String contentTemplate) {
    this.contentTemplate = contentTemplate;
  }
  
  public String getSubjectTemplate() {
    return subjectTemplate;
  }
  
  public void setSubjectTemplate(String subjectTemplate) {
    this.subjectTemplate = subjectTemplate;
  }
  
  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }
  
  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }
  
  public OffsetDateTime getModifiedAt() {
    return modifiedAt;
  }
  
  public void setModifiedAt(OffsetDateTime modifiedAt) {
    this.modifiedAt = modifiedAt;
  }

  @PrePersist
  public void onCreate() {
    setCreatedAt(OffsetDateTime.now());
    setModifiedAt(OffsetDateTime.now());
  }
  
  @PreUpdate
  public void onUpdate() {
    setModifiedAt(OffsetDateTime.now());
  }
  
}
