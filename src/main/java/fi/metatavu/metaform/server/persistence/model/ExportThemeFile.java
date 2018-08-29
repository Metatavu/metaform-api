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
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * JPA entity representing an export theme file
 * 
 * @author Antti Leppä
 */
@Entity
@Table (
  uniqueConstraints = {
    @UniqueConstraint(name="UN_EXPORTTHEME_THEME_PATH",columnNames = {"theme_id", "path"})
  }
)
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
public class ExportThemeFile {

  @Id
  @Type(type="org.hibernate.type.PostgresUUIDType")
  private UUID id;
  
  @ManyToOne (optional = false)
  private ExportTheme theme;
  
  @NotEmpty
  @NotNull
  @Column(nullable = false)
  private String path;
  
  @NotEmpty
  @NotNull
  @Lob
  @Column(nullable = false)
  @Type(type = "org.hibernate.type.TextType")
  private String content;
  
  @Column(nullable = false)
  @NotNull
  @Type(type="org.hibernate.type.PostgresUUIDType")
  private UUID creator;
  
  @Column(nullable = false)
  @NotNull
  @Type(type="org.hibernate.type.PostgresUUIDType")
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
  
  public String getContent() {
    return content;
  }
  
  public void setContent(String content) {
    this.content = content;
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
