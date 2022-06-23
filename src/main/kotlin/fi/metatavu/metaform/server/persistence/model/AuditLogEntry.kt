package fi.metatavu.metaform.server.persistence.model

import fi.metatavu.metaform.api.spec.model.AuditLogEntryType
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull

/**
 * JPA entity representing audit log entry
 *
 * @author Katja Danilova
 */
@Entity
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
class AuditLogEntry : Metadata() {

  @Id
  var id: UUID? = null

  @Column(nullable = false)
  @NotNull
  var userId: UUID? = null

  @Column
  var replyId: UUID? = null

  @Column
  var attachmentId: UUID? = null

  @Column
  var message: String? = null

  @Column(nullable = false)
  @NotNull
  var logEntryType: AuditLogEntryType? = null

  @ManyToOne(optional = false)
  var metaform: Metaform? = null
  override var creatorId: UUID? = null

  override var lastModifierId: UUID? = null
}