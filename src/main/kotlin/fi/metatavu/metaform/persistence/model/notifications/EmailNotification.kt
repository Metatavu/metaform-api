package fi.metatavu.metaform.persistence.model.notifications

import fi.metatavu.metaform.persistence.model.Metaform
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.annotations.Type
import java.util.*
import javax.persistence.*

/**
 * JPA entity representing Metaform email notications
 *
 * @author Antti Leppä
 */
@Entity
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
class EmailNotification : fi.metatavu.metaform.persistence.model.Metadata() {
  @Id
  var id: UUID? = null

  @Lob
  @Type(type = "org.hibernate.type.TextType")
  var subjectTemplate: String? = null

  @Lob
  @Type(type = "org.hibernate.type.TextType")
  var contentTemplate: String? = null

  @Lob
  var notifyIf: String? = null

  @ManyToOne(optional = false)
  var metaform: Metaform? = null

  override var creatorId: UUID? = null

  override var lastModifierId: UUID? = null
}