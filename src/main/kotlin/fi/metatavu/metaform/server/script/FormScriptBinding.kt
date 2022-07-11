package fi.metatavu.metaform.server.script

import fi.metatavu.metaform.api.spec.model.Reply
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

@ApplicationScoped
class FormScriptBinding {

    @Inject
    lateinit var formRuntimeContext: FormRuntimeContext

    @Inject
    lateinit var pdfServices: PdfServices

    @Inject
    lateinit var xlsxServices: XlsxServices

    @Inject
    lateinit var encodingServices: EncodingServices

    /**
     * Returns reply object
     *
     * @return reply object
     */
    fun getReply(): Reply {
        return formRuntimeContext.reply
    }

    /**
     * Returns reply data
     *
     * @return reply data
     */
    fun getReplyData(): Map<String, Any?>? {
        return getReply().data
    }

    /**
     * Sets variable value
     *
     * @param name variable name
     * @param value variable value
     */
    fun setVariableValue(name: String, value: Any?) {
        formRuntimeContext.setVariableValue(name, value)
    }

    /**
     * Returns variable value
     *
     * @param name variable name
     * @return variable value
     */
    fun getVariableValue(name: String): Any? {
        return formRuntimeContext.getVariableValue(name)
    }

}