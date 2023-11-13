package fi.metatavu.metaform.server.controllers

import java.util.*

/**
 * @param metaformId Metaform id
 * @param replyId Reply id
  */
data class ReplyUpdatedEvent(val metaformId: UUID, val replyId: UUID)