package fi.metatavu.metaform.server.persistence.model

import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.util.*
import javax.persistence.*

/**
 * JPA entity linking a script to a metaform
 */
@Entity
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Table
class MetaformScript: Metadata() {
  @Id
  lateinit var id: UUID

  @ManyToOne
  var metaform: Metaform? = null

  @ManyToOne
  var script: Script? = null

  @Column(nullable = false)
  lateinit var creatorId: UUID

  @Column(nullable = false)
  lateinit var lastModifierId: UUID
}