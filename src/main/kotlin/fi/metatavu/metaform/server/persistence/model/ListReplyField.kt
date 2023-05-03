package fi.metatavu.metaform.server.persistence.model

import javax.persistence.Cacheable
import javax.persistence.Entity

/**
 * JPA entity representing list field in reply
 *
 * @author Antti Leppä
 */
@Entity
@Cacheable(true)
class ListReplyField : ReplyField()