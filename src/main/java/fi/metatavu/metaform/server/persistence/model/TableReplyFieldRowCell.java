package fi.metatavu.metaform.server.persistence.model;

import java.util.UUID;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * JPA entity representing single row in table reply field
 * 
 * @author Antti Leppä
 */
@Entity
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Inheritance (strategy = InheritanceType.JOINED)
public class TableReplyFieldRowCell {

  @Id
  @Type(type="org.hibernate.type.PostgresUUIDType")
  private UUID id;

  @ManyToOne(optional = false)
  private TableReplyFieldRow row;
  
  @NotNull
  @NotEmpty
  private String name;
  
  public void setId(UUID id) {
    this.id = id;
  }
  
  public UUID getId() {
    return id;
  }

  public TableReplyFieldRow getRow() {
    return row;
  }
  
  public void setRow(TableReplyFieldRow row) {
    this.row = row;
  }
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
}
