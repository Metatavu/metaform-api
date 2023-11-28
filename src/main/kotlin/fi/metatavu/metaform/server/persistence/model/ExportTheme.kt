package fi.metatavu.metaform.server.persistence.model

import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.util.*
import jakarta.persistence.*
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

/**
 * JPA entity representing an export theme
 *
 * @author Antti Leppä
 */
@Entity
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
class ExportTheme : Metadata() {

  @Id
  var id: UUID? = null

  @Column(nullable = false, unique = true)
  @NotNull
  @NotEmpty
  lateinit var name: String

  @ManyToOne
  var parent: ExportTheme? = null

  @Lob
  var locales: String? = null

  @Column(nullable = false)
  @NotNull
  lateinit var creator: UUID

  @Column(nullable = false)
  @NotNull
  lateinit var lastModifier: UUID
}