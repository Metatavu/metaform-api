package fi.metatavu.metaform.server.persistence.model

import org.hibernate.annotations.Type
import javax.persistence.Cacheable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Lob

/**
 * JPA entity representing string field in reply
 *
 * @author Antti Leppä
 */
@Entity
@Cacheable(true)
class StringReplyField : ReplyField() {
  @Lob
  @Type(type = "org.hibernate.type.TextType")
  @Column
  var value: String? = null
}