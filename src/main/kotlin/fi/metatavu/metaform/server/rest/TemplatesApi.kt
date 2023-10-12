package fi.metatavu.metaform.server.rest

import fi.metatavu.metaform.api.spec.model.Template
import fi.metatavu.metaform.api.spec.model.TemplateVisibility
import fi.metatavu.metaform.server.controllers.*
import fi.metatavu.metaform.server.exceptions.DeserializationFailedException
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
class TemplatesApi: fi.metatavu.metaform.api.spec.TemplatesApi, AbstractApi() {

  @Inject
  lateinit var logger: Logger

  @Inject
  lateinit var exportThemeController: ExportThemeController

  @Inject
  lateinit var templateController: TemplateController

  @Inject
  lateinit var templateTranslator: TemplateTranslator

  override fun createTemplate(template: Template): Response {
    val userId = loggedUserId ?: return createForbidden(UNAUTHORIZED)

    val templateData = template.data
            ?: return createBadRequest("Template data is required")

    val templateVisibility = template.visibility
            ?: return createBadRequest("Template visibility is required")

    val createdTemplate = templateController.createTemplate(
        templateData = templateData,
        visibility = templateVisibility,
        creatorId = userId
    )

    val templateEntity: Template = try {
      templateTranslator.translateTemplate(template = createdTemplate)
    } catch (e: DeserializationFailedException) {
      return createInternalServerError(e.message)
    }

    return createOk(templateEntity)
  }

  override fun deleteTemplate(templateId: UUID): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    val template = templateController.findTemplateById(templateId)
      ?: return createNotFound(createNotFoundMessage("template", templateId))

    templateController.deleteTemplate(template)

    return createNoContent()
  }

  override fun findTemplate(templateId: UUID): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    val template = templateController.findTemplateById(templateId)
      ?: return createNotFound(createNotFoundMessage("template", templateId))

    return try {
      createOk(templateTranslator.translateTemplate(template))
    } catch (e: DeserializationFailedException) {
      createInternalServerError(e.message)
    }
  }

  override fun updateTemplate(templateId: UUID, template: Template): Response {
    TODO("Not yet implemented")
    /*
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    val foundTemplate = templateController.findTemplateById(templateId)
      ?: return createNotFound(createNotFoundMessage("template", templateId))


    val serializedTemplate = try {

    }

    val updatedTemplate = templateController.updateTemplate(foundTemplate, serializedTemplate)
    */
  }

  override fun listTemplates(visibility: TemplateVisibility?): Response {
    TODO("Not yet implemented")
  }
}