package fi.metatavu.metaform.server.persistence.model

import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.util.*
import javax.persistence.*

/**
 * JPA entity representing single item in attachment reply field
 *
 * @author Antti Leppä
 */
@Entity
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["field_id", "attachment_id"])])
class AttachmentReplyFieldItem {

  @Id
  var id: UUID? = null

  @ManyToOne(optional = false)
  var field: AttachmentReplyField? = null

  @ManyToOne
  var attachment: Attachment? = null
}