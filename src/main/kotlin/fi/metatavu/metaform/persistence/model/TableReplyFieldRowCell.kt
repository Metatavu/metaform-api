package fi.metatavu.metaform.persistence.model

import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

/**
 * JPA entity representing single row in table reply field
 *
 * @author Antti Leppä
 */
@Entity
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Inheritance(strategy = InheritanceType.JOINED)
class TableReplyFieldRowCell {
  @Id
  var id: UUID? = null

  @ManyToOne(optional = false)
  var row: TableReplyFieldRow? = null

  @Column(nullable = false)
  @NotNull
  @NotEmpty
  var name: String? = null
}