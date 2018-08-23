package fi.metatavu.metaform.server.persistence.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * JPA entity representing an export theme file
 * 
 * @author Antti Leppä
 */
@Entity
public class ExportThemeFile {

  @Id
  private UUID id;
  
  @ManyToOne (optional = false)
  private ExportTheme theme;
  
  @NotEmpty
  @NotNull
  @Column(nullable = false)
  private String path;

  @Column(nullable = false)
  @NotNull
  private UUID creator;
  
  @Column(nullable = false)
  @NotNull
  private UUID lastModifier;

  @Column (nullable = false)
  private OffsetDateTime createdAt;

  @Column (nullable = false)
  private OffsetDateTime modifiedAt;
  
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
  
  public String getPath() {
    return path;
  }
  
  public void setPath(String path) {
    this.path = path;
  }
  
  public ExportTheme getTheme() {
    return theme;
  }
  
  public void setTheme(ExportTheme theme) {
    this.theme = theme;
  }

  public UUID getCreator() {
    return creator;
  }
  public void setCreator(UUID creator) {
    this.creator = creator;
  }
  
  public UUID getLastModifier() {
    return lastModifier;
  }
  
  public void setLastModifier(UUID lastModifier) {
    this.lastModifier = lastModifier;
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
