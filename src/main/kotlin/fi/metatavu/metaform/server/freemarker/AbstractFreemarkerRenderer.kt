package fi.metatavu.metaform.server.freemarker

import freemarker.template.Configuration
import freemarker.template.Template
import freemarker.template.TemplateException
import freemarker.template.Version
import jakarta.inject.Inject
import org.slf4j.Logger
import java.io.IOException
import java.io.StringWriter
import java.io.Writer
import java.util.*

/**
 * Abstract freemarker renderer
 */
abstract class AbstractFreemarkerRenderer {

    @Inject
    lateinit var logger: Logger

    /**
     * Renders a freemarker template
     *
     * @param configuration freemarker configuration     *
     * @param templateName name of the template
     * @param dataModel data model
     * @param locale locale
     * @return rendered template
     */
    fun render(configuration: Configuration, templateName: String, dataModel: Any?, locale: Locale?): String? {
        val template = getTemplate(configuration, templateName)
        if (template == null) {
            if (logger.isErrorEnabled) {
                logger.error(String.format("Could not find template %s", templateName))
            }
            return null
        }
        val out: Writer = StringWriter()
        if (locale != null) template.locale = locale
        try {
            template.process(dataModel, out)
        } catch (e: TemplateException) {
            logger.error("Failed to render template", e)
        } catch (e: IOException) {
            logger.error("Failed to render template", e)
        }
        return out.toString()
    }

    private fun getTemplate(configuration: Configuration, name: String): Template? {
        try {
            return configuration.getTemplate(name)
        } catch (e: IOException) {
            logger.error("Failed to load template", e)
        }
        return null
    }

    companion object {
        val VERSION: Version = Configuration.VERSION_2_3_23
    }
}