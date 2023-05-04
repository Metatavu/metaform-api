package fi.metatavu.metaform.server.controllers

import fi.metatavu.metaform.api.spec.model.ScriptType
import fi.metatavu.metaform.server.persistence.dao.ScriptDAO
import fi.metatavu.metaform.server.persistence.model.Script
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Scripts controller
 */
@ApplicationScoped
class ScriptsController {

  @Inject
  lateinit var scriptDAO: ScriptDAO

  /**
   * Creates a script
   *
   * @param name name
   * @param type type
   * @param content content
   * @param creatorId creator id
   */
  fun createScript(
    name: String,
    language: String,
    type: ScriptType,
    content: String,
    creatorId: UUID
  ): Script {
    return scriptDAO.createScript(
      UUID.randomUUID(),
      name,
      language,
      type,
      content,
      creatorId
    )
  }

  /**
   * Updates a script
   *
   * @param script script to update
   * @param name new name
   * @param language new language
   * @param type new type
   * @param content new content
   * @param lastModifierId last modifier id
   */
  fun updateScript(
    script: Script,
    name: String,
    language: String,
    type: ScriptType,
    content: String,
    lastModifierId: UUID
  ): Script {
    scriptDAO.updateName(script, name, lastModifierId)
    scriptDAO.updateLanguage(script, language, lastModifierId)
    scriptDAO.updateType(script, type, lastModifierId)
    scriptDAO.updateContent(script, content, lastModifierId)

    return script
  }

  /**
   * Lists scripts
   */
  fun listScripts(): List<Script> {
    return scriptDAO.listAll()
  }

  /**
   * Finds a script
   *
   * @param scriptId script id
   */
  fun findScript(scriptId: UUID): Script? {
    return scriptDAO.findById(scriptId)
  }

  /**
   * Deletes a script
   *
   * @param script to delete
   */
  fun deleteScript(script: Script) {
    scriptDAO.delete(script)
  }
}