package fi.metatavu.metaform.server.rest

import fi.metatavu.metaform.api.spec.model.*
import fi.metatavu.metaform.server.controllers.*
import fi.metatavu.metaform.server.exceptions.DeserializationFailedException
import fi.metatavu.metaform.server.exceptions.MalformedMetaformJsonException
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

    if (!isMetatavuAdmin && !isRealmSystemAdmin) {
      return createForbidden(createNotAllowedMessage(CREATE, TEMPLATE))
    }

    val templateData = template.data
            ?: return createBadRequest("Template data is required")

    val templateVisibility = template.visibility
            ?: return createBadRequest("Template visibility is required")

    val createdTemplate = templateController.createTemplate(
        templateData = templateData,
        visibility = templateVisibility,
        creatorId = userId
    )

    return createOk(templateTranslator.translateTemplate(template = createdTemplate))
  }

  override fun deleteTemplate(templateId: UUID): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isMetatavuAdmin && !isRealmSystemAdmin) {
      return createForbidden(createNotAllowedMessage(CREATE, TEMPLATE))
    }

    val template = templateController.findTemplateById(templateId)
      ?: return createNotFound(createNotFoundMessage("template", templateId))

    templateController.deleteTemplate(template)

    return createNoContent()
  }

  override fun findTemplate(templateId: UUID): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isMetatavuAdmin && !isRealmSystemAdmin) {
      return createForbidden(createNotAllowedMessage(CREATE, TEMPLATE))
    }

    val template = templateController.findTemplateById(templateId)
      ?: return createNotFound(createNotFoundMessage("template", templateId))

    return createOk(templateTranslator.translateTemplate(template))
  }

  override fun updateTemplate(templateId: UUID, template: Template): Response {
    val userId = loggedUserId
        ?: return createForbidden(UNAUTHORIZED)

    if (!isMetatavuAdmin && !isRealmSystemAdmin) {
      return createForbidden(createNotAllowedMessage(CREATE, TEMPLATE))
    }

    val foundTemplate = templateController.findTemplateById(templateId)
        ?: return createNotFound(createNotFoundMessage(TEMPLATE, templateId))

    val data = try {
        templateController.serializeTemplateData(templateData = template.data!!)
    } catch (e: MalformedMetaformJsonException) {
        createInvalidMessage(createInvalidMessage(TEMPLATE))
    }

    val updatedTemplate = templateController.updateTemplate(
        template = foundTemplate,
        templateVisibility = template.visibility ?: foundTemplate.visibility!!,
        data = data,
        lastModifier = userId
    )

    return createOk(templateTranslator.translateTemplate(updatedTemplate))
  }

  override fun listTemplates(visibility: TemplateVisibility?): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (visibility == TemplateVisibility.PRIVATE) {
      return createForbidden(createNotAllowedMessage(LIST, TEMPLATE))
    }

    val templates = templateController.listTemplates(visibility = visibility)

            .filter { template ->
              //val templateId = template.id!!
              template.visibility == TemplateVisibility.PUBLIC
            }

    return createOk(templates.map(templateTranslator::translateTemplate))
  }
}