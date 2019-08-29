package fi.metatavu.metaform.server.persistence.model;

import java.util.UUID;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * JPA entity representing field in reply
 * 
 * @author Antti Leppä
 */
@Entity
@Cacheable(true)
@Table(uniqueConstraints = { 
    @UniqueConstraint(columnNames = { "reply_id", "name" })    
})
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Inheritance (strategy = InheritanceType.JOINED)
public class ReplyField {

  @Id
  private UUID id;

  @ManyToOne(optional = false)
  private Reply reply;

  @NotNull
  @NotEmpty
  private String name;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Reply getReply() {
    return reply;
  }

  public void setReply(Reply reply) {
    this.reply = reply;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
