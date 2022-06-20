package fi.metatavu.metaform.persistence.model

import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.annotations.Type
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

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
  var name: String? = null

  @ManyToOne
  var parent: ExportTheme? = null

  @Lob
  @Type(type = "org.hibernate.type.TextType")
  var locales: String? = null

  override var creatorId: UUID? = null

  override var lastModifierId: UUID? = null
}