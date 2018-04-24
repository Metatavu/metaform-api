package fi.metatavu.metaform.server.persistence.model;

import javax.persistence.Cacheable;
import javax.persistence.Entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * JPA entity representing boolean field in reply
 * 
 * @author Antti Leppä
 */
@Entity
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
public class BooleanReplyField extends ReplyField {

  private Boolean value;

  public Boolean getValue() {
    return value;
  }

  public void setValue(Boolean value) {
    this.value = value;
  }

}
