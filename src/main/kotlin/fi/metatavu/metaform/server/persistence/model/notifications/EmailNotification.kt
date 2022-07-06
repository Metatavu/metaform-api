package fi.metatavu.metaform.server.persistence.model.notifications

import fi.metatavu.metaform.server.persistence.model.Metaform
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.annotations.Type
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

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
  @Type(type = "org.hibernate.type.TextType")
  @Column
  @NotNull
  @NotEmpty
  lateinit var subjectTemplate: String

  @Lob
  @Type(type = "org.hibernate.type.TextType")
  @Column
  @NotNull
  @NotEmpty
  lateinit var contentTemplate: String

  @Lob
  var notifyIf: String? = null

  @ManyToOne(optional = false)
  var metaform: Metaform? = null

}