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
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * JPA entity representing an export theme
 * 
 * @author Antti Leppä
 */
@Entity
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
public class ExportTheme {

  @Id
  @Type(type="org.hibernate.type.PostgresUUIDType")
  private UUID id;
  
  @NotEmpty
  @NotNull
  @Column(nullable = false, unique = true)
  private String name;
  
  @ManyToOne
  private ExportTheme parent;

  @Lob
  @Type(type = "org.hibernate.type.TextType")
  private String locales;

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
  
  public String getLocales() {
    return locales;
  }
  
  public void setLocales(String locales) {
    this.locales = locales;
  }
  
  public ExportTheme getParent() {
    return parent;
  }
  
  public void setParent(ExportTheme parent) {
    this.parent = parent;
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
