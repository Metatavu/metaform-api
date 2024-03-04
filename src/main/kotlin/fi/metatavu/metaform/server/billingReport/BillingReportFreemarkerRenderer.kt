package fi.metatavu.metaform.server.billingReport

import freemarker.ext.beans.BeansWrapperBuilder
import freemarker.template.Configuration
import freemarker.template.Template
import freemarker.template.TemplateException
import freemarker.template.TemplateExceptionHandler
import jakarta.annotation.PostConstruct
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.slf4j.Logger
import java.io.File
import java.io.IOException
import java.io.StringWriter

/**
 * Freemarker renderer
 */
@ApplicationScoped
class BillingReportFreemarkerRenderer {

    @Inject
    lateinit var logger: Logger

    lateinit var configuration: Configuration

    /**
     * Initializes renderer
     * todo unite with other rederer
     */
    @PostConstruct
    fun init() {
        configuration = Configuration(VERSION)
        configuration.defaultEncoding = "UTF-8"
        configuration.templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
        configuration.logTemplateExceptions = false
        configuration.setDirectoryForTemplateLoading(File("src/main/resources/templates"))
        configuration.objectWrapper = BeansWrapperBuilder(VERSION).build()
    }

    /**
     * Renders a freemarker template
     *
     * @param templateName name of the template
     * @param dataModel data model
     * @return rendered template
     */
    fun render(templateName: String, dataModel: Any?): String? {
        val template = getTemplate(templateName)
        if (template == null) {
            logger.error("Could not find template $templateName")
            return null
        }
        val out = StringWriter()

        try {
            template.process(dataModel, out)
        } catch (e: TemplateException) {
            logger.error("Failed to render template $templateName", e)
        } catch (e: IOException) {
            logger.error("Failed to render template $templateName", e)
        }

        return out.toString()
    }

    /**
     * Gets freemarker template
     *
     * @param templateName name of the template
     * @return found template
     */
    private fun getTemplate(templateName: String): Template? {
        return try {
            configuration.getTemplate(templateName)
        } catch (e: IOException) {
            logger.error("Failed to load template $templateName", e)
            null
        }
    }

    companion object {
        private val VERSION = Configuration.VERSION_2_3_23
    }
}