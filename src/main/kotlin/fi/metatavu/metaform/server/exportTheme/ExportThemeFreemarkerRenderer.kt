package fi.metatavu.metaform.server.exportTheme

import fi.metatavu.metaform.server.controllers.SystemSettingController
import freemarker.cache.NullCacheStorage
import freemarker.ext.beans.BeansWrapperBuilder
import freemarker.template.Configuration
import freemarker.template.Template
import freemarker.template.TemplateException
import freemarker.template.TemplateExceptionHandler
import org.slf4j.Logger
import java.io.IOException
import java.io.StringWriter
import java.io.Writer
import java.util.*
import jakarta.annotation.PostConstruct
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

/**
 * Freemarker renderer
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class ExportThemeFreemarkerRenderer {

    @Inject
    lateinit var  logger: Logger

    @Inject
    lateinit var  freemarkerTemplateLoader: ExportThemeFreemarkerTemplateLoader

    @Inject
    lateinit var  systemSettingController: SystemSettingController

    lateinit var configuration: Configuration

    /**
     * Initializes renderer
     */
    @PostConstruct
    fun init() {
        configuration = Configuration(VERSION)
        configuration.templateLoader = freemarkerTemplateLoader
        configuration.defaultEncoding = "UTF-8"
        configuration.templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
        configuration.logTemplateExceptions = false
        configuration.objectWrapper = BeansWrapperBuilder(VERSION).build()
        configuration.localizedLookup = false

        if (systemSettingController.inTestMode()) {
            configuration.cacheStorage = NullCacheStorage()
        }
    }

    /**
     * Renders a freemarker template
     *
     * @param templateName name of the template
     * @param dataModel data model
     * @param locale locale
     * @return rendered template
     */
    fun render(templateName: String, dataModel: Any?, locale: Locale): String? {
        val template = getTemplate(templateName, locale)
        if (template == null) {
            if (logger.isErrorEnabled) {
                logger.error(String.format("Could not find template %s", templateName))
            }
            return null
        }
        val out: Writer = StringWriter()
        template.locale = locale
        try {
            template.process(dataModel, out)
        } catch (e: TemplateException) {
            logger.error("Failed to render template", e)
        } catch (e: IOException) {
            logger.error("Failed to render template", e)
        }
        return out.toString()
    }

    private fun getTemplate(name: String, locale: Locale): Template? {
        try {
            return configuration.getTemplate(name, locale)
        } catch (e: IOException) {
            logger.error("Failed to load template", e)
        }
        return null
    }

    companion object {
        private val VERSION = Configuration.VERSION_2_3_23
    }
}