package fi.metatavu.metaform.server.rest

import fi.metatavu.metaform.api.spec.model.Script
import fi.metatavu.metaform.server.controllers.ScriptsController
import fi.metatavu.metaform.server.rest.translate.ScriptsTranslator
import org.slf4j.Logger
import java.util.*
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.core.Response

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

    if (!isRealmSystemAdmin) {
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

    if (!isRealmSystemAdmin) {
      return createForbidden(createNotAllowedMessage(DELETE, SCRIPT))
    }

    val script = scriptsController.findScript(scriptId)
      ?: return createNotFound(createNotFoundMessage(SCRIPT, scriptId))

    scriptsController.deleteScript(script)

    return createNoContent()
  }

  override fun findScript(scriptId: UUID): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isRealmSystemAdmin && !isMetaformAdminAny) {
      return createForbidden(createNotAllowedMessage(LIST, SCRIPT))
    }

    val script = scriptsController.findScript(scriptId)
      ?: return createNotFound(createNotFoundMessage(SCRIPT, scriptId))

    return createOk(scriptsTranslator.translate(script))
  }

  override fun listScripts(): Response {
    loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isRealmSystemAdmin && !isMetaformAdminAny) {
      return createForbidden(createNotAllowedMessage(LIST, SCRIPT))
    }

    return createOk(scriptsController.listScripts().map(scriptsTranslator::translate))
  }

  override fun updateScript(scriptId: UUID, script: Script): Response {
    val userId = loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isRealmSystemAdmin) {
      return createForbidden(createNotAllowedMessage(UPDATE, SCRIPT))
    }

    val foundScript = scriptsController.findScript(scriptId)
      ?: return createNotFound(createNotFoundMessage(SCRIPT, UUID.fromString(script.id)))

    val updatedScript = scriptsController.updateScript(
      script = foundScript,
      name = foundScript.name,
      language = foundScript.language,
      type = foundScript.type,
      content = foundScript.content,
      lastModifierId = userId
    )

    return createOk(scriptsTranslator.translate(updatedScript))
  }
}