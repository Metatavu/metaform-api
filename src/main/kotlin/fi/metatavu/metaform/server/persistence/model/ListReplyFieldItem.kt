package fi.metatavu.metaform.server.persistence.model

import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.util.*
import jakarta.persistence.*
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

/**
 * JPA entity representing single item in list reply field
 *
 * @author Antti Leppä
 */
@Entity
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["field_id", "value"])])
class ListReplyFieldItem {
  @Id
  var id: UUID? = null

  @ManyToOne(optional = false)
  lateinit var field: ListReplyField

  @Column(nullable = false)
  @NotNull
  @NotEmpty
  lateinit var value: String
}