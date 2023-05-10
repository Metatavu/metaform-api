package fi.metatavu.metaform.server.controllers

import fi.metatavu.metaform.server.persistence.dao.MetaformScriptDAO
import fi.metatavu.metaform.server.persistence.model.Metaform
import fi.metatavu.metaform.server.persistence.model.Script
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for scripts that are linked to Metaforms
 */
@ApplicationScoped
class MetaformScriptController {
  @Inject
  lateinit var metaformScriptDAO: MetaformScriptDAO

  @Inject
  lateinit var scriptsController: ScriptsController

  /**
   * Links a script to a metaform
   *
   * @param metaform metaform
   * @param script script
   * @param creatorId creator id
   */
  fun createMetaformScript(metaform: Metaform, script: Script, creatorId: UUID) {
    metaformScriptDAO.createMetaformScript(id = UUID.randomUUID(), metaform = metaform, script = script, creatorId = creatorId)
  }

  /**
   * Deletes links between a metaform and its scripts
   *
   * @param metaform metaform
   */
  fun deleteMetaformScriptsByMetaform (metaform: Metaform) {
    metaformScriptDAO.listByMetaform(metaform).forEach { metaformScript ->
      metaformScriptDAO.delete(metaformScript)
    }
  }

  /**
   * Updates links between a metaform and its scripts
   *
   * @param metaform metaform
   */
  fun updateMetaformScripts(metaform: fi.metatavu.metaform.api.spec.model.Metaform, updatedMetaform: Metaform, creatorId: UUID) {
    if (metaform.scripts != null) {
      val existingMetaformScripts = metaformScriptDAO.listByMetaform(updatedMetaform)
      existingMetaformScripts.forEach { metaformScript ->
        if (!metaform.scripts.contains(metaformScript.script.id)) {
          metaformScriptDAO.delete(metaformScript)
        }
      }

      metaform.scripts.forEach { scriptId ->
        if (existingMetaformScripts.none { metaformScript -> metaformScript.script?.id == scriptId }) {
          val script = scriptsController.findScript(scriptId)
          metaformScriptDAO.createMetaformScript(id = UUID.randomUUID(), metaform = updatedMetaform, script = script!!, creatorId = creatorId)
        }
      }
    }

  }
}