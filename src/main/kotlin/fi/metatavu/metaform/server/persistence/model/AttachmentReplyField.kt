package fi.metatavu.metaform.server.persistence.model

import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import jakarta.persistence.Cacheable
import jakarta.persistence.Entity

/**
 * JPA entity representing attachment field in reply
 *
 * @author Antti Leppä
 */
@Entity
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
class AttachmentReplyField : ReplyField()