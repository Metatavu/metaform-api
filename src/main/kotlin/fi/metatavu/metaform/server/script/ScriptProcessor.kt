package fi.metatavu.metaform.server.script

import fi.metatavu.polyglot.xhr.XMLHttpRequest
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Source
import org.slf4j.Logger
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

@ApplicationScoped
class ScriptProcessor {


    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var formScriptBinding: FormScriptBinding

    /**
     * Processes a script
     *
     * @param script script
     * @param params parameters
     * @return processed output
     */
    fun processScript(script: RunnableScript, params: Map<String, String?>): String? {
        try {
            Context.create(script.language).use { scriptingContext ->
                val scriptArgs: MutableMap<String, String?> = HashMap()
                params.keys.stream().forEach { param: String ->
                    if (!RESERVED_PARAMS.contains(param)) {
                        scriptArgs[param] = params[param]
                    }
                }
                val bindings = scriptingContext.getBindings(script.language)
                bindings.putMember("XMLHttpRequest", XMLHttpRequest::class.java)
                bindings.putMember("form", formScriptBinding)
                bindings.putMember("args", scriptArgs)
                val source = Source.newBuilder(script.language, script.content, script.name).build()
                val returnValue = scriptingContext.eval(source)
                if (returnValue.isString) {
                    return returnValue.asString()
                }
            }
        } catch (e: Exception) {
            logger.error("Error running script", e)
        }
        return ""
    }

    companion object {
        private val RESERVED_PARAMS: Set<String> = HashSet(listOf("name", "version", "module", "function", "target"))

    }
}