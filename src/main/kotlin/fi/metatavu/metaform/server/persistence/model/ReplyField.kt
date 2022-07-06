package fi.metatavu.metaform.server.persistence.model

import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

/**
 * JPA entity representing field in reply
 *
 * @author Antti Leppä
 */
@Entity
@Cacheable(true)
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["reply_id", "name"])])
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Inheritance(strategy = InheritanceType.JOINED)
class ReplyField {
  @Id
  var id: UUID? = null

  @ManyToOne(optional = false)
  lateinit var reply: Reply

  @Column (nullable = false)
  @NotNull
  @NotEmpty
  lateinit var name: String
}