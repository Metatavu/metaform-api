package fi.metatavu.metaform.persistence.model

import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
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
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
class NumberReplyField : ReplyField() {
  @Column
  var value: Double? = null
}