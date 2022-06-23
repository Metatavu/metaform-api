package fi.metatavu.metaform.server.persistence.dao

import fi.metatavu.metaform.server.persistence.model.Draft
import fi.metatavu.metaform.server.persistence.model.Metaform
import java.util.*
import javax.enterprise.context.ApplicationScoped

/**
 * DAO class for draft entity
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class DraftDAO : AbstractDAO<Draft>() {

  /**
   * Creates new draft
   *
   * @param id id
   * @param userId user id
   * @param metaform Metaform
   * @param data data
   * @param creatorId creator id
   * @param lastModifierId creator id
   * @return created Metaform
   */
  fun create(
    id: UUID?,
    userId: UUID?,
    metaform: Metaform?,
    data: String?,
    creatorId: UUID,
    lastModifierId: UUID
  ): Draft {
    val draft = Draft()
    draft.id = id
    draft.metaform = metaform
    draft.userId = userId
    draft.data = data
    draft.creatorId = creatorId
    draft.lastModifierId = lastModifierId
    return persist(draft)
  }

  /**
   * Updates data
   *
   * @param draft draft
   * @param lastModifierId modifier id
   * @param data data
   * @return updated draft
   */
  fun updateData(draft: Draft, lastModifierId: UUID, data: String?): Draft {
    draft.data = data
    draft.lastModifierId = lastModifierId
    return persist(draft)
  }
}