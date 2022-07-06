package fi.metatavu.metaform.server.exportTheme

import fi.metatavu.metaform.api.spec.model.Attachment
import fi.metatavu.metaform.api.spec.model.Metaform
import fi.metatavu.metaform.api.spec.model.Reply
import java.util.Date


/**
 * Export data model
 *
 * @author Antti Leppä
 */
data class ReplyExportDataModel (
    val metaform: Metaform,
    var reply: Reply,
    var attachments: Map<String, Attachment>,
    var createdAt : Date?,
    var modifiedAt: Date?
)
