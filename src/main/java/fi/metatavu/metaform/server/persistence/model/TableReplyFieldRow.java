package fi.metatavu.metaform.server.persistence.model;

import java.util.UUID;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;

/**
 * JPA entity representing single row in table reply field
 * 
 * @author Antti Leppä
 */
@Entity
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
public class TableReplyFieldRow {

  @Id
  @Type(type="org.hibernate.type.PostgresUUIDType")
  private UUID id;

  @ManyToOne(optional = false)
  private TableReplyField field;
  
  public void setId(UUID id) {
    this.id = id;
  }
  
  public UUID getId() {
    return id;
  }

  public TableReplyField getField() {
    return field;
  }
  
  public void setField(TableReplyField field) {
    this.field = field;
  }
  
}
