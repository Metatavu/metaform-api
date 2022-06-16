package fi.metatavu.metaform.server.persistence.model

import fi.metatavu.metaform.api.spec.model.MetaformVersionType
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.util.*
import javax.persistence.*

/**
 * JPA entity representing single Metaform version
 *
 * @author Tianxing Wu
 */
@Entity
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Table
class MetaformVersion : Metadata() {
  @Id
  var id: UUID? = null

  @ManyToOne
  var metaform: Metaform? = null

  @Enumerated(EnumType.STRING)
  var type: MetaformVersionType? = null

  @Lob
  @Column(nullable = false)
  var data: String? = null

  override lateinit var creatorId: UUID

  override lateinit var lastModifierId: UUID
}