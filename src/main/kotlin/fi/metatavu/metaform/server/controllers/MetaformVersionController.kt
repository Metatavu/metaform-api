package fi.metatavu.metaform.server.controllers

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import fi.metatavu.metaform.api.spec.model.MetaformVersionType
import fi.metatavu.metaform.server.exceptions.MalformedDraftDataException
import fi.metatavu.metaform.server.exceptions.MalformedVersionJsonException
import fi.metatavu.metaform.server.persistence.dao.MetaformVersionDAO
import fi.metatavu.metaform.server.persistence.model.Metaform
import fi.metatavu.metaform.server.persistence.model.MetaformVersion
import java.util.*
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

/**
 * Metaform version controller
 *
 * @author Tianxing Wu
 */
@ApplicationScoped
class MetaformVersionController: AbstractMetaformResourceController<MetaformVersion>() {
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
            data: Map<String, Any>,
            userId: UUID
    ): MetaformVersion {
        return try {
            metaformVersionDAO.create(
                    UUID.randomUUID(),
                    metaform,
                    type,
                    serializeData(data),
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
    override fun delete (metaformVersion: MetaformVersion) {
        metaformVersionDAO.delete(metaformVersion)
    }

    /**
     * Updates Metaform version
     *
     * @param metaformVersion Metaform version
     * @param type Metaform version type
     * @param data form JSON
     * @param lastModifierId last modifier id
     */
    fun updateMetaformVersion(
        metaformVersion: MetaformVersion,
        type: MetaformVersionType,
        data: Map<String, Any>,
        lastModifierId: UUID
    ): MetaformVersion {
        metaformVersionDAO.updateType(metaformVersion, type, lastModifierId)
        metaformVersionDAO.updateData(metaformVersion, serializeData(data), lastModifierId)
        return metaformVersion
    }

    /**
     * Serializes data as string
     *
     * @param data data
     * @return data as string
     */
    @Throws(MalformedDraftDataException::class)
    fun serializeData(data: Map<String, Any>): String {
        try {
            val objectMapper = ObjectMapper()
            return objectMapper.writeValueAsString(data)
        } catch (e: JsonProcessingException) {
            throw MalformedDraftDataException("Failed to serialize draft data", e)
        }
    }
}