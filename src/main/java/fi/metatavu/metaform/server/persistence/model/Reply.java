package fi.metatavu.metaform.server.persistence.model;

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
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * JPA entity representing reply
 * 
 * @author Antti Leppä
 */
@Entity
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
public class Reply {

  @Id
  private UUID id;

  @Column(nullable = false)
  @NotNull
  private UUID userId;

  @Lob
  private byte[] privateKey;
  
  @Column
  private OffsetDateTime revision;
  
  @Column (nullable = false)
  private OffsetDateTime createdAt;

  @Column (nullable = false)
  private OffsetDateTime modifiedAt;
  
  @ManyToOne(optional = false)
  private Metaform metaform;

  private UUID resourceId;
  
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
  
  public OffsetDateTime getRevision() {
    return revision;
  }
  
  public void setRevision(OffsetDateTime revision) {
    this.revision = revision;
  }

  public Metaform getMetaform() {
    return metaform;
  }

  public void setMetaform(Metaform metaform) {
    this.metaform = metaform;
  }
  
  public UUID getResourceId() {
    return resourceId;
  }
  
  public void setResourceId(UUID resourceId) {
    this.resourceId = resourceId;
  }

  public byte[] getPrivateKey() {
    return privateKey;
  }

  public void setPrivateKey(byte[] privateKey) {
    this.privateKey = privateKey;
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
