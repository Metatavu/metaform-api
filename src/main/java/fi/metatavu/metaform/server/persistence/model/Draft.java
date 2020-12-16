package fi.metatavu.metaform.server.persistence.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Lob;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * JPA entity representing reply draft
 * 
 * @author Antti Leppä
 */
@Entity
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
public class Draft {

  @Id
  private UUID id;

  @Column(nullable = false)
  @NotNull
  private UUID userId;
  
  @Column (nullable = false)
  private OffsetDateTime createdAt;

  @Column (nullable = false)
  private OffsetDateTime modifiedAt;
  
  @ManyToOne(optional = false)
  private Metaform metaform;

  @Lob
  @NotNull
  @Column (nullable = false)
  private String data;
  
  /**
   * Returns id
   * 
   * @return id
   */
  public UUID getId() {
    return id;
  }

  /**
   * Sets an id
   * 
   * @param id id
   */
  public void setId(UUID id) {
    this.id = id;
  }

  /**
   * Returns user id
   * 
   * @return user id
   */
  public UUID getUserId() {
    return userId;
  }

  /**
   * Sets the user id
   * 
   * @param userId user id
   */
  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  /**
   * Returns metaform
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
  
  /**
   * Returns creation time
   * 
   * @return creation time
   */
  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }
  
  /**
   * Sets the creation time
   * 
   * @param createdAt creation time
   */
  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }
  
  /**
   * Returns last modification time
   * 
   * @return last modification time
   */
  public OffsetDateTime getModifiedAt() {
    return modifiedAt;
  }
  
  /**
   * Sets last modification time. 
   * 
   * @param modifiedAt last modification time. 
   */
  public void setModifiedAt(OffsetDateTime modifiedAt) {
    this.modifiedAt = modifiedAt;
  }

  /**
   * Returns data
   * 
   * @return data
   */
  public String getData() {
    return data;
  }

  /**
   * Sets data
   * 
   * @param data
   */
  public void setData(String data) {
    this.data = data;
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
