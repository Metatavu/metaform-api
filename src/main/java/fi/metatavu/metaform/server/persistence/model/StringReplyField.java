package fi.metatavu.metaform.server.persistence.model;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Lob;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;

/**
 * JPA entity representing string field in reply
 * 
 * @author Antti Leppä
 */
@Entity
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
public class StringReplyField extends ReplyField {

  @Lob
  @Type(type = "org.hibernate.type.TextType")
  private String value;

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

}
