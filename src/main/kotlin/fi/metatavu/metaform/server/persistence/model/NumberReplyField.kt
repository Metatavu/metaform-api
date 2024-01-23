package fi.metatavu.metaform.server.persistence.model

import jakarta.persistence.Cacheable
import jakarta.persistence.Column
import jakarta.persistence.Entity

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