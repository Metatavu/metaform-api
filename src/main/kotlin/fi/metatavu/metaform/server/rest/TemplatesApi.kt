package fi.metatavu.metaform.server.rest

import fi.metatavu.metaform.api.spec.model.Template
import fi.metatavu.metaform.api.spec.model.TemplateVisibility
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
    val userId = loggedUserId
        ?: return createForbidden(UNAUTHORIZED)

    val foundTemplate = templateController.findTemplateById(templateId)
        ?: return createNotFound(createNotFoundMessage(TEMPLATE, templateId))

    val data = try {
        templateController.serializeTemplateData(templateData = template.data!!)
    } catch (e: MalformedMetaformJsonException) {
        createInvalidMessage(createInvalidMessage(TEMPLATE))
    }

    /* TODO PERMISSIONS AND DUPLICATE FIELDS
    if (!metaformController.validateMetaform(metaform)) {
        return createBadRequest("Duplicate field names")
    }

    val permissionGroups = MetaformUtils.getPermissionGroups(metaform = metaform)

    if (!metaformController.validatePermissionGroups(permissionGroups = permissionGroups)) {
        return createBadRequest("Invalid permission groups")
    }
    */

    val updatedTemplate = templateController.updateTemplate(
        template = foundTemplate,
        templateVisibility = template.visibility ?: foundTemplate.visibility!!,
        data = data,
        lastModifier = userId
    )

    return try {
        createOk(templateTranslator.translateTemplate(updatedTemplate))
    } catch (e: MalformedMetaformJsonException) {
        createInternalServerError(e.message)
    }
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

            /*
            .filter { template ->
              val templateId = template.id!!
              when (memberRole) {
                MetaformMemberRole.ADMINISTRATOR -> isMetaformAdmin(metaformId)
                MetaformMemberRole.MANAGER -> isMetaformManager(metaformId)
                else -> true
              }
            }
            */

    return createOk(templates.map(templateTranslator::translateTemplate))
  }
}