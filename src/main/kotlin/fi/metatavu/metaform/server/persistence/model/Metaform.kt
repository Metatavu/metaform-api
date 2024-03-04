package fi.metatavu.metaform.server.persistence.model

import fi.metatavu.metaform.api.spec.model.MetaformVisibility
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.time.OffsetDateTime
import java.util.*
import jakarta.persistence.*
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

/**
 * JPA entity representing single Metaform
 *
 * @author Antti Leppä
 */
@Entity
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["slug"])])
class Metaform: Metadata() {
  @Id
  var id: UUID? = null

  @Column(nullable = false)
  @NotNull
  @NotEmpty
  lateinit var slug: String

  @Lob
  @Column(nullable = false)
  @NotNull
  @NotEmpty
  lateinit var data: String

  @Column(nullable = false)
  @NotNull
  var allowAnonymous: Boolean? = null

  @NotNull
  @Enumerated(EnumType.STRING)
  var visibility: MetaformVisibility? = null

  @ManyToOne
  var exportTheme: ExportTheme? = null

  @Column(nullable = false)
  lateinit var creatorId: UUID

  @Column(nullable = false)
  lateinit var lastModifierId: UUID

  @Column(nullable = false)
  @NotNull
  var active: Boolean = true

  @OneToMany(mappedBy = "metaform", targetEntity = MetaformReplyViewed::class)
  lateinit var metaformReplyViewed: List<MetaformReplyViewed>

  @OneToMany(mappedBy = "metaform", targetEntity = MetaformReplyCreated::class)
  lateinit var metaformReplyCreated: List<MetaformReplyCreated>
}