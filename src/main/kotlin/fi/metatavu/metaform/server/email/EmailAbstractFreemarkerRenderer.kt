package fi.metatavu.metaform.server.email

import fi.metatavu.metaform.server.freemarker.AbstractFreemarkerRenderer
import freemarker.ext.beans.BeansWrapperBuilder
import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import jakarta.annotation.PostConstruct
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

/**
 * Freemarker renderer
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class EmailAbstractFreemarkerRenderer: AbstractFreemarkerRenderer() {

    @Inject
    lateinit var freemarkerTemplateLoader: EmailFreemarkerTemplateLoader

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
    }

}