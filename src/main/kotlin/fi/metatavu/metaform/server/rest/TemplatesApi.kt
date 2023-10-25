package fi.metatavu.metaform.server.rest

import fi.metatavu.metaform.api.spec.model.*
import fi.metatavu.metaform.server.controllers.*
import fi.metatavu.metaform.api.spec.TemplatesApi
import fi.metatavu.metaform.server.rest.translate.TemplateTranslator
import org.slf4j.Logger
import java.util.*
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.core.Response

@RequestScoped
@Transactional
@Suppress("unused")
class TemplatesApi : TemplatesApi, AbstractApi() {

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var exportThemeController: ExportThemeController

    @Inject
    lateinit var templateController: TemplateController

    @Inject
    lateinit var templateTranslator: TemplateTranslator

    /**
     * Creates a new template
     *
     * @param template template
     */
    override fun createTemplate(template: Template): Response {

        val userId = loggedUserId ?: return createForbidden(UNAUTHORIZED)

        if (!isMetatavuAdmin && !isRealmSystemAdmin) {
            return createForbidden(createNotAllowedMessage(CREATE, TEMPLATE))
        }

        val createdTemplate = templateController.createTemplate(
                templateData = template.data,
                visibility = template.visibility,
                creatorId = userId
        )

        return createOk(templateTranslator.translateTemplate(template = createdTemplate))
    }

    /**
     * Deletes a template
     *
     * @param templateId template id
     */
    override fun deleteTemplate(templateId: UUID): Response {
        loggedUserId ?: return createForbidden(UNAUTHORIZED)

        if (!isMetatavuAdmin && !isRealmSystemAdmin) {
            return createForbidden(createNotAllowedMessage(CREATE, TEMPLATE))
        }

        val template = templateController.findTemplateById(templateId)
                ?: return createNotFound(createNotFoundMessage(TEMPLATE, templateId))

        templateController.deleteTemplate(template)

        return createNoContent()
    }

    /**
     * Finds a template by template id
     *
     * @param templateId template id
     */
    override fun findTemplate(templateId: UUID): Response {
        loggedUserId ?: return createForbidden(UNAUTHORIZED)

        if (!isMetatavuAdmin && !isRealmSystemAdmin) {
            return createForbidden(createNotAllowedMessage(CREATE, TEMPLATE))
        }

        val template = templateController.findTemplateById(templateId)
                ?: return createNotFound(createNotFoundMessage("template", templateId))

        return createOk(templateTranslator.translateTemplate(template))
    }

    /**
     * Updates a template
     *
     * @param templateId template id
     * @param template template
     */
    override fun updateTemplate(templateId: UUID, template: Template): Response {
        val userId = loggedUserId
                ?: return createForbidden(UNAUTHORIZED)

        if (!isMetatavuAdmin && !isRealmSystemAdmin) {
            return createForbidden(createNotAllowedMessage(CREATE, TEMPLATE))
        }

        val foundTemplate = templateController.findTemplateById(templateId)
                ?: return createNotFound(createNotFoundMessage(TEMPLATE, templateId))

        val updatedTemplate = templateController.updateTemplate(
                template = foundTemplate,
                templateData = template.data,
                templateVisibility = template.visibility,
                lastModifier = userId
        )

        return createOk(templateTranslator.translateTemplate(updatedTemplate))
    }

    /**
     * Lists templates.
     *
     * @param visibility template visibility
     */
    override fun listTemplates(visibility: TemplateVisibility?): Response {
        loggedUserId ?: return createForbidden(UNAUTHORIZED)

        if (!isMetatavuAdmin && !isRealmSystemAdmin) {
          return createForbidden(createNotAllowedMessage(LIST, TEMPLATE))
        }

        val templates = templateController.listTemplates(visibility = visibility)

        return createOk(templates.map(templateTranslator::translateTemplate))
    }
}