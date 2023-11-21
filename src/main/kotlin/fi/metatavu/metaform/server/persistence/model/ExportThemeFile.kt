package fi.metatavu.metaform.server.persistence.model

import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.annotations.Type
import java.time.OffsetDateTime
import java.util.*
import jakarta.persistence.*
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

/**
 * JPA entity representing an export theme file
 *
 * @author Antti Leppä
 */
@Entity
@Table(
  uniqueConstraints = [UniqueConstraint(
    name = "UN_EXPORTTHEME_THEME_PATH",
    columnNames = ["theme_id", "path"]
  )]
)
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
class ExportThemeFile : Metadata() {

  @Id
  var id: UUID? = null

  @ManyToOne(optional = false)
  var theme: ExportTheme? = null

  @Column(nullable = false)
  @NotEmpty
  @NotNull
  lateinit var path: String

  @Lob
  @Column(nullable = false)
  /*@Type(type = "org.hibernate.type.TextType")*/
  @NotEmpty
  @NotNull
  lateinit var content: String

  @Column(nullable = false)
  @NotNull
  lateinit var creator: UUID

  @Column(nullable = false)
  @NotNull
  lateinit var lastModifier: UUID
}