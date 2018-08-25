package fi.metatavu.metaform.server.persistence.model;

import java.util.UUID;

import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * JPA entity representing an attachment
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
@Entity
public class Attachment {

  @Id
  @Type(type="org.hibernate.type.PostgresUUIDType")
  private UUID id;

  @Column(nullable = false)
  @NotNull
  @Type(type="org.hibernate.type.PostgresUUIDType")
  private UUID userId;

  @Column (nullable = false)
  private OffsetDateTime createdAt;

  @Column (nullable = false)
  private OffsetDateTime modifiedAt;
  
  @NotEmpty
  @NotNull
  @Column(nullable = false)
  private String name;
  
  @NotEmpty
  @NotNull
  @Column(nullable = false)
  private String contentType;
  
  @NotEmpty
  @NotNull
  @Column(nullable = false)
  @Lob
  private byte[] content;
  
  /**
   * @return the id
   */
  public UUID getId() {
      return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(UUID id) {
      this.id = id;
  }

  /**
   * @return the userId
   */
  public UUID getUserId() {
      return userId;
  }

  /**
   * @param userId the userId to set
   */
  public void setUserId(UUID userId) {
      this.userId = userId;
  }

  /**
   * @return the createdAt
   */
  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  /**
   * @param createdAt the createdAt to set
   */
  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  /**
   * @return the modifiedAt
   */
  public OffsetDateTime getModifiedAt() {
      return modifiedAt;
  }

  /**
   * @param modifiedAt the modifiedAt to set
   */
  public void setModifiedAt(OffsetDateTime modifiedAt) {
      this.modifiedAt = modifiedAt;
  }

  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public byte[] getContent() {
    return content;
  }
  
  public void setContent(byte[] content) {
    this.content = content;
  }
  
  public String getContentType() {
    return contentType;
  }
  
  public void setContentType(String contentType) {
    this.contentType = contentType;
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
