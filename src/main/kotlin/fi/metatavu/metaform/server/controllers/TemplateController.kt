package fi.metatavu.metaform.server.controllers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.metatavu.metaform.api.spec.model.TemplateData
import fi.metatavu.metaform.api.spec.model.TemplateVisibility
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
     * @param template template
     * @param userId user id
     * @param data template data
     * @return created template
     */
    fun createTemplate(
            templateData: TemplateData,
            visibility: TemplateVisibility,
            creatorId: UUID
    ): Template {

        val serializedTemplateData = try {
            serializeTemplateData(templateData)
        } catch (e: Exception) {
           throw Exception(e.message)
        }

        return templateDAO.create(
                id = UUID.randomUUID(),
                data = serializedTemplateData,
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
     * @param data data
     * @return updated template
     */
    fun updateTemplate(
        template: Template,
        data: String,
        templateVisibility: TemplateVisibility,
        lastModifier: UUID
    ): Template {
        return templateDAO.updateData(
            template = template,
            data = data,
            templateVisibility = templateVisibility,
            lastModifier = lastModifier
        )
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
        return templateDAO.listByVisibility(visibility)
    }

    /**
     * Serializes TemplateData as string
     *
     * @param data data
     * @return data as string
     */

    @Throws(Exception::class)
    fun serializeTemplateData(templateData: TemplateData): String {
        try {
            return jacksonObjectMapper().writeValueAsString(templateData)
        } catch (e: Exception) {
            throw Exception("Failed to serialize template data", e)
        }
    }

    @Throws(Exception::class)
    fun serializeTemplate(template: fi.metatavu.metaform.api.spec.model.Template): String {
        try {
            return jacksonObjectMapper().writeValueAsString(template)
        } catch (e: Exception) {
            throw Exception("Failed to serialize template data", e)
        }
    }
}