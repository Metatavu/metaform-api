package fi.metatavu.metaform.server.persistence.model;

import java.util.UUID;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * JPA entity representing single Metaform
 * 
 * @author Antti Leppä
 */
@Entity
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
public class Metaform {

  @Id
  private UUID id;

  @NotNull
  @NotEmpty
  private String realmId;

  @Lob
  private String data;

  @NotNull
  @Column (nullable = false)
  private Boolean allowAnonymous;
  
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
  
}
