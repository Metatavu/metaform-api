package fi.metatavu.metaform.server.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import fi.metatavu.metaform.server.exceptions.MalformedDraftDataException
import fi.metatavu.metaform.server.persistence.dao.DraftDAO
import fi.metatavu.metaform.server.persistence.model.Draft
import fi.metatavu.metaform.server.persistence.model.Metaform
import org.slf4j.Logger
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject


/**
 * Controller for Drafts
 */
@ApplicationScoped
class DraftController {

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var draftDAO: DraftDAO

    /**
     * Creates new draft
     *
     * @param metaform metaform
     * @param userId user id
     * @param data draft data
     * @return created draft
     */
    fun createDraft(
            metaform: Metaform,
            userId: UUID,
            data: String
    ): Draft {
        return draftDAO.create(
                id = UUID.randomUUID(),
                userId = userId,
                metaform = metaform,
                data = data,
        )
    }

    /**
     * Finds a draft by id
     *
     * @param id id
     * @return found draft or null if not found
     */
    fun findDraftById(id: UUID): Draft? {
        return draftDAO.findById(id)
    }

    /**
     * Updates a draft
     *
     * @param draft draft
     * @param data data
     * @return updated draft
     */
    fun updateDraft(draft: Draft, data: String): Draft {
        return draftDAO.updateData(draft, data)
    }

    /**
     * Deletes an draft
     *
     * @param draft draft to be deleted
     */
    fun deleteDraft(draft: Draft) {
        draftDAO.delete(draft)
    }

    /**
     * Serializes data as string
     *
     * @param data data
     * @return data as string
     */
    fun serializeData(data: Map<String, Any>): String {
        try {
            val objectMapper = ObjectMapper()
            return objectMapper.writeValueAsString(data)
        } catch (e: Exception) {
            logger.error("Failed to serialize draft data", e)
            throw MalformedDraftDataException("Failed to serialize draft data", e)
        }
    }
}