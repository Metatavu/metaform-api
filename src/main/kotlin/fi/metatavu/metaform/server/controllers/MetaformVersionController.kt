package fi.metatavu.metaform.server.controllers

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import fi.metatavu.metaform.api.spec.model.MetaformVersionType
import fi.metatavu.metaform.server.exceptions.MalformedVersionJsonException
import fi.metatavu.metaform.server.persistence.dao.MetaformVersionDAO
import fi.metatavu.metaform.server.persistence.model.Metaform
import fi.metatavu.metaform.server.persistence.model.MetaformVersion
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Metaform version controller
 *
 * @author Tianxing Wu
 */
@ApplicationScoped
class MetaformVersionController {
    @Inject
    lateinit var metaformVersionDAO: MetaformVersionDAO

    /**
     * Creates new Metaform version
     *
     * @param metaform Metaform
     * @param type Metaform version type
     * @param data Metaform form JSON
     * @param userId user id
     */
    @Throws(MalformedVersionJsonException::class)
    fun create(
            metaform: Metaform,
            type: MetaformVersionType,
            data: Any?,
            userId: UUID
    ): MetaformVersion {
        val objectMapper = ObjectMapper()
        return try {
            val formDataString = objectMapper.writeValueAsString(data)
            metaformVersionDAO.create(
                    UUID.randomUUID(),
                    metaform,
                    type,
                    formDataString,
                    userId,
                    userId
            )
        } catch (e: JsonProcessingException) {
            throw MalformedVersionJsonException("failed to serialize the json", e)
        }
    }

    /**
     * Finds metaform version by Id
     *
     * @param metaformVersionId Metaform version id
     * @return item if found
     */
    fun findMetaformVersionById(metaformVersionId: UUID): MetaformVersion? {
        return metaformVersionDAO.findById(metaformVersionId)
    }

    /**
     * Lists versions by Metaforms
     *
     * @param metaform Metaform
     * @return item if found
     */
    fun listMetaformVersionsByMetaform(metaform: Metaform): List<MetaformVersion> {
        return metaformVersionDAO.listByMetaform(metaform)
    }

    /**
     * Deletes Metaform version
     *
     * @param metaformVersion Metaform version
     */
    fun deleteMetaformVersion(metaformVersion: MetaformVersion) {
        metaformVersionDAO.delete(metaformVersion)
    }
}