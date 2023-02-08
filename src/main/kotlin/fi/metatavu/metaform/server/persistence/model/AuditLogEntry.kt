package fi.metatavu.metaform.server.persistence.model

import fi.metatavu.metaform.api.spec.model.AuditLogEntryType
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.time.OffsetDateTime
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
class AuditLogEntry {

  @Id
  var id: UUID? = null

  @Column(nullable = false)
  @NotNull
  lateinit var userId: UUID

  @Column
  var replyId: UUID? = null

  @Column
  var attachmentId: UUID? = null

  @Column
  var message: String? = null

  @Column(nullable = false)
  @NotNull
  @Enumerated(EnumType.STRING)
  lateinit var logEntryType: AuditLogEntryType

  @ManyToOne(optional = false)
  lateinit var metaform: Metaform

  @Column(nullable = false)
  lateinit var createdAt: OffsetDateTime

  /**
   * JPA pre-persist event handler
   */
  @PrePersist
  fun onCreate() {
    createdAt = OffsetDateTime.now()
  }

}