package fi.metatavu.metaform.persistence.dao

import fi.metatavu.metaform.persistence.model.ReplyField
import javax.enterprise.context.ApplicationScoped

/**
 * DAO class for handling any reply field entity
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class AnyReplyFieldDAO : ReplyFieldDAO<ReplyField>()