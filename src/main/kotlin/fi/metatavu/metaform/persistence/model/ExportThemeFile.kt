package fi.metatavu.metaform.persistence.model

import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.annotations.Type
import java.time.OffsetDateTime
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

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
  var path: String? = null

  @Lob
  @Column(nullable = false)
  @Type(type = "org.hibernate.type.TextType")
  @NotEmpty
  @NotNull
  var content: String? = null

  override var creatorId: UUID? = null

  override var lastModifierId: UUID? = null
}