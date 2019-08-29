package fi.metatavu.metaform.server.persistence.model;

import java.util.UUID;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * JPA entity representing single item in list reply field
 * 
 * @author Antti Leppä
 */
@Entity
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Table(uniqueConstraints = { 
  @UniqueConstraint(columnNames = { "field_id", "value" })    
})
public class ListReplyFieldItem {

  @Id
  private UUID id;

  @ManyToOne(optional = false)
  private ListReplyField field;

  @Column (nullable = false)
  @NotNull
  @NotEmpty
  private String value;
  
  public void setId(UUID id) {
    this.id = id;
  }
  
  public UUID getId() {
    return id;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public ListReplyField getField() {
    return field;
  }
  
  public void setField(ListReplyField field) {
    this.field = field;
  }
  
}
