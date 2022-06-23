package fi.metatavu.metaform.server.persistence.model

import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.util.*
import javax.persistence.Cacheable
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.ManyToOne

/**
 * JPA entity representing single row in table reply field
 *
 * @author Antti Leppä
 */
@Entity
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
class TableReplyFieldRow {
  @Id
  var id: UUID? = null

  @ManyToOne(optional = false)
  var field: TableReplyField? = null
}