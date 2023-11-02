package fi.metatavu.metaform.server.persistence.model

import javax.persistence.Cacheable
import javax.persistence.Column
import javax.persistence.Entity

/**
 * JPA entity representing number field in reply
 *
 * @author Antti Leppä
 */
@Entity
@Cacheable(true)
class NumberReplyField : ReplyField() {
  @Column
  var value: Double? = null
}