package fi.metatavu.metaform.server.persistence.model

import jakarta.persistence.Cacheable
import jakarta.persistence.Entity

/**
 * JPA entity representing list field in reply
 *
 * @author Antti Leppä
 */
@Entity
@Cacheable(true)
class ListReplyField : ReplyField()