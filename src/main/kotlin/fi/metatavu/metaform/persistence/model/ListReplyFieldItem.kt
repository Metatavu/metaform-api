package fi.metatavu.metaform.persistence.model

import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

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
  var field: ListReplyField? = null

  @Column(nullable = false)
  @NotNull
  @NotEmpty
  var value: String? = null
}