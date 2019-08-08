package fi.metatavu.metaform.server.persistence.model;

import java.util.UUID;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import javax.validation.constraints.NotEmpty;

/**
 * JPA entity representing single Metaform
 * 
 * @author Antti Leppä
 */
@Entity
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Table (
  uniqueConstraints = {
    @UniqueConstraint (columnNames = { "realmid", "slug" })  
  }    
)
public class Metaform {

  @Id
  @Type(type="org.hibernate.type.PostgresUUIDType")
  private UUID id;

  @NotNull
  @NotEmpty
  private String realmId;

  @NotNull
  @NotEmpty
  private String slug;

  @Lob
  private String data;

  @NotNull
  @Column (nullable = false)
  private Boolean allowAnonymous;
  
  @ManyToOne
  private ExportTheme exportTheme;
  
  public UUID getId() {
    return id;
  }
  
  public void setId(UUID id) {
    this.id = id;
  }
  
  public void setRealmId(String realmId) {
    this.realmId = realmId;
  }
  
  public String getRealmId() {
    return realmId;
  }
  
  public String getSlug() {
    return slug;
  }
  
  public void setSlug(String slug) {
    this.slug = slug;
  }
  
  public String getData() {
    return data;
  }
  
  public void setData(String data) {
    this.data = data;
  }
  
  public Boolean getAllowAnonymous() {
    return allowAnonymous;
  }
  
  public void setAllowAnonymous(Boolean allowAnonymous) {
    this.allowAnonymous = allowAnonymous;
  }
  
  public ExportTheme getExportTheme() {
    return exportTheme;
  }
  
  public void setExportTheme(ExportTheme exportTheme) {
    this.exportTheme = exportTheme;
  }
  
}
