package fi.metatavu.metaform.server.rest

import fi.metatavu.metaform.api.spec.model.Script
import fi.metatavu.metaform.server.controllers.ScriptsController
import fi.metatavu.metaform.server.rest.translate.ScriptsTranslator
import org.slf4j.Logger
import java.util.*
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.core.Response

@RequestScoped
@Transactional
@Suppress("unused")
class ScriptsApi: fi.metatavu.metaform.api.spec.ScriptsApi, AbstractApi(){
  @Inject
  lateinit var logger: Logger

  @Inject
  lateinit var scriptsController: ScriptsController

  @Inject
  lateinit var scriptsTranslator: ScriptsTranslator

  override fun createScript(script: Script): Response {
    val userId = loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isMetatavuAdmin) {
      return createForbidden(createNotAllowedMessage(CREATE, SCRIPT))
    }

    val createdScript = scriptsController.createScript(
      name = script.name,
      language = script.language,
      type = script.type,
      content = script.content,
      creatorId = userId
    )

    return createOk(scriptsTranslator.translate(createdScript))
  }

  override fun deleteScript(scriptId: UUID): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isMetatavuAdmin) {
      return createForbidden(createNotAllowedMessage(DELETE, SCRIPT))
    }

    val script = scriptsController.findScript(scriptId)
      ?: return createNotFound(createNotFoundMessage(SCRIPT, scriptId))

    scriptsController.deleteScript(script)

    return createNoContent()
  }

  override fun findScript(scriptId: UUID): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isMetatavuAdmin && !isRealmSystemAdmin && !isMetaformAdminAny) {
      return createForbidden(createNotAllowedMessage(LIST, SCRIPT))
    }

    val script = scriptsController.findScript(scriptId)
      ?: return createNotFound(createNotFoundMessage(SCRIPT, scriptId))

    return createOk(scriptsTranslator.translate(script))
  }

  override fun listScripts(): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isMetatavuAdmin && !isRealmSystemAdmin && !isMetaformAdminAny) {
      return createForbidden(createNotAllowedMessage(LIST, SCRIPT))
    }

    return createOk(scriptsController.listScripts().map(scriptsTranslator::translate))
  }

  override fun updateScript(scriptId: UUID, script: Script): Response {
    val userId = loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isMetatavuAdmin) {
      return createForbidden(createNotAllowedMessage(UPDATE, SCRIPT))
    }

    val foundScript = scriptsController.findScript(scriptId)
      ?: return createNotFound(createNotFoundMessage(SCRIPT, scriptId))

    val updatedScript = scriptsController.updateScript(
      script = foundScript,
      name = script.name,
      language = script.language,
      type = script.type,
      content = script.content,
      lastModifierId = userId
    )

    return createOk(scriptsTranslator.translate(updatedScript))
  }
}