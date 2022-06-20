package fi.metatavu.metaform.persistence.model

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
  var slug: String? = null

  @Lob
  @Column(nullable = false)
  @NotNull
  @NotEmpty
  var data: String? = null

  @Column(nullable = false)
  @NotNull
  var allowAnonymous: Boolean? = null

  @ManyToOne
  var exportTheme: ExportTheme? = null
}