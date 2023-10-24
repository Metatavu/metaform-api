package fi.metatavu.metaform.server.controllers

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import fi.metatavu.metaform.api.spec.model.TemplateData
import fi.metatavu.metaform.api.spec.model.TemplateVisibility
import fi.metatavu.metaform.server.exceptions.MalformedTemplateJsonException
import fi.metatavu.metaform.server.persistence.dao.TemplateDAO
import fi.metatavu.metaform.server.persistence.model.*
import org.slf4j.Logger
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Template controller
 *
 * @author Harri Häkkinen
 */
@ApplicationScoped
class TemplateController {

    @Inject
    lateinit var templateDAO: TemplateDAO

    @Inject
    lateinit var logger: Logger

    /**
     * Creates new template
     *
     * @param templateData TemplateData
     * @param visibility TemplateVisibility
     * @param creatorId UUID
     * @return created template
     */
    fun createTemplate(
            templateData: TemplateData,
            visibility: TemplateVisibility,
            creatorId: UUID
    ): Template {
        return templateDAO.create(
                id = UUID.randomUUID(),
                data = serializeTemplateData(templateData),
                visibility = visibility,
                creatorId = creatorId,
                lastModifierId = creatorId
        )
    }

    /**
     * Finds a template by id
     *
     * @param id id
     * @return found template or null if not found
     */
    fun findTemplateById(id: UUID): Template? {
        return templateDAO.findById(id)
    }

    /**
     * Updates a template
     *
     * @param template template
     * @param templateData template data
     * @param lastModifier last modifier UUID
     * @return updated template
     */
    fun updateTemplate(
            template: Template,
            templateData: TemplateData,
            templateVisibility: TemplateVisibility,
            lastModifier: UUID
    ): Template {
        templateDAO.updateData(
                template = template,
                data = serializeTemplateData(templateData),
                lastModifier = lastModifier
        )
        templateDAO.updateVisibility(
                template = template,
                templateVisibility = templateVisibility,
                lastModifierId = lastModifier
        )
        return template
    }

    /**
     * Deletes an template
     *
     * @param template template to be deleted
     */
    fun deleteTemplate(template: Template) {
        templateDAO.delete(template)
    }

    /**
     * Lists templates by visibility
     * @param visibility TemplateVisibility
     */
    fun listTemplates(visibility: TemplateVisibility? = null): List<Template> {
        visibility ?: return templateDAO.listAll()
        return templateDAO.list(visibility)
    }

    /**
     * Serializes TemplateData as string
     *
     * @param templateData TemplateData
     * @return data as string
     */
    @Throws(Exception::class)
    private fun serializeTemplateData(templateData: TemplateData): String {
        val objectMapper = ObjectMapper()
        objectMapper.registerModule(JavaTimeModule())

        try {
            return objectMapper.writeValueAsString(templateData)
        } catch (e: JsonProcessingException) {
            throw MalformedTemplateJsonException("Failed to serialize draft data", e)
        }
    }


}