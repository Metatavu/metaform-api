package fi.metatavu.metaform.server.persistence.model

import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.util.*
import jakarta.persistence.*
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

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
  lateinit var name: String
}