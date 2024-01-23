package fi.metatavu.metaform.server.persistence.dao

import fi.metatavu.metaform.server.persistence.model.ReplyField
import jakarta.enterprise.context.ApplicationScoped

/**
 * DAO class for handling any reply field entity
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class AnyReplyFieldDAO : ReplyFieldDAO<ReplyField>()