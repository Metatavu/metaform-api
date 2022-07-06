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
   * @return created Metaform
   */
  fun create(
    id: UUID,
    userId: UUID,
    metaform: Metaform,
    data: String
  ): Draft {
    val draft = Draft()
    draft.id = id
    draft.metaform = metaform
    draft.userId = userId
    draft.data = data
    return persist(draft)
  }

  /**
   * Updates data
   *
   * @param draft draft
   * @param data data
   * @param lastModifierId modifier id
   * @return updated draft
   */
  fun updateData(draft: Draft, data: String?): Draft {
    draft.data = data
    return persist(draft)
  }
}