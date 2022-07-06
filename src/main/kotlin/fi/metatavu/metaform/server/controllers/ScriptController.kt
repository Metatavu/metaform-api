package fi.metatavu.metaform.server.controllers

import fi.metatavu.metaform.api.spec.model.MetaformScript
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
    fun runScripts(scripts: List<MetaformScript?>?) {
        scripts?.stream()?.forEach { script: MetaformScript? -> runScript(script) }
    }

    /**
     * Runs given script
     *
     * @param script
     */
    private fun runScript(script: MetaformScript?) {
        if (script != null) {
            scriptProcessor.processScript(RunnableScript(script.language, script.content, script.name), HashMap())
        }
    }
}