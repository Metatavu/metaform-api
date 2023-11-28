package fi.metatavu.metaform.server.persistence.model.notifications

import fi.metatavu.metaform.server.persistence.model.Metaform
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.util.*
import jakarta.persistence.*
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

/**
 * JPA entity representing Metaform email notications
 *
 * @author Antti Leppä
 */
@Entity
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
class EmailNotification : fi.metatavu.metaform.server.persistence.model.Metadata() {
  @Id
  var id: UUID? = null

  @Lob
  @Column
  @NotNull
  @NotEmpty
  lateinit var subjectTemplate: String

  @Lob
  @Column
  @NotNull
  @NotEmpty
  lateinit var contentTemplate: String

  @Lob
  var notifyIf: String? = null

  @ManyToOne(optional = false)
  var metaform: Metaform? = null

}