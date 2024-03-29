package fi.metatavu.metaform.server.persistence.model

import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.time.OffsetDateTime
import java.util.*
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull

/**
 * JPA entity representing reply
 *
 * @author Antti Leppä
 */
@Entity
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
class Reply {

  @Id
  var id: UUID? = null

  @Column(nullable = false)
  @NotNull
  lateinit var userId: UUID

  @Lob
  @Column
  var privateKey: ByteArray? = null

  @Column
  var revision: OffsetDateTime? = null

  @ManyToOne(optional = false)
  lateinit var metaform: Metaform

  @Column
  var resourceId: UUID? = null

  @Column(nullable = false)
  lateinit var lastModifierId: UUID

  @Column(nullable = false)
  lateinit var createdAt: OffsetDateTime

  @Column(nullable = false)
  lateinit var modifiedAt: OffsetDateTime

  @Column
  var firstViewedAt: OffsetDateTime? = null

  @Column
  var lastViewedAt: OffsetDateTime? = null

}