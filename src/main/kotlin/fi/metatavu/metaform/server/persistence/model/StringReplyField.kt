package fi.metatavu.metaform.server.persistence.model

import jakarta.persistence.Cacheable
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Lob

/**
 * JPA entity representing string field in reply
 *
 * @author Antti Leppä
 */
@Entity
@Cacheable(true)
class StringReplyField : ReplyField() {
  @Lob
  @Column
  var value: String? = null
}