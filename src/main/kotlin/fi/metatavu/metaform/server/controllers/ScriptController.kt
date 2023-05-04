package fi.metatavu.metaform.server.controllers

import fi.metatavu.metaform.server.persistence.model.Script
import fi.metatavu.metaform.server.script.RunnableScript
import fi.metatavu.metaform.server.script.ScriptProcessor
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for Metaform scripts
 */
@ApplicationScoped
class ScriptController {

    @Inject
    lateinit var scriptProcessor: ScriptProcessor

    /**
     * Runs given scripts
     *
     * @param scripts scripts
     */
    fun runScripts(scripts: List<Script>?) {
        scripts?.forEach { runScript(it) }
    }

    /**
     * Runs given script
     *
     * @param script
     */
    private fun runScript(script: Script?) {
        if (script != null) {
            scriptProcessor.processScript(RunnableScript(script.language, script.content, script.name), mapOf())
        }
    }
}