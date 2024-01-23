package fi.metatavu.metaform.server.script

import fi.metatavu.metaform.api.spec.model.Attachment
import fi.metatavu.metaform.api.spec.model.Metaform
import fi.metatavu.metaform.api.spec.model.Reply
import fi.metatavu.metaform.server.xlsx.XlsxBuilder
import java.util.*
import jakarta.annotation.PostConstruct
import jakarta.enterprise.context.RequestScoped

@RequestScoped
class FormRuntimeContext {

    lateinit var loggedUserId: UUID
    lateinit var metaform: Metaform
    lateinit var reply: Reply
    lateinit var xlsxBuilder: XlsxBuilder
    private lateinit var variableValues: MutableMap<String, Any?>
    lateinit var attachmentMap: Map<String, Attachment>
    lateinit var exportThemeName: String
    lateinit var locale: Locale

    /**
     * Post construct method
     */
    @PostConstruct
    fun init() {
        variableValues = HashMap()
    }
    /**
     * Sets variable value
     *
     * @param name variable name
     * @param value variable value
     */
    fun setVariableValue(name: String, value: Any?) {
        variableValues[name] = value
    }

    /**
     * Returns variable value
     *
     * @param name variable name
     * @return variable value
     */
    fun getVariableValue(name: String): Any? {
        return variableValues[name]
    }
}