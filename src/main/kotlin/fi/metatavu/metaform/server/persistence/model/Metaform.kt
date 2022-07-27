package fi.metatavu.metaform.server.persistence.model

import fi.metatavu.metaform.api.spec.model.MetaformVisibility
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

/**
 * JPA entity representing single Metaform
 *
 * @author Antti Leppä
 */
@Entity
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["slug"])])
class Metaform {
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
}