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

  @ManyToOne(optional = false)
  lateinit var metaform: Metaform

  @ManyToOne(optional = false)
  lateinit var script: Script

  @Column(nullable = false)
  lateinit var creatorId: UUID

  @Column(nullable = false)
  lateinit var lastModifierId: UUID
}