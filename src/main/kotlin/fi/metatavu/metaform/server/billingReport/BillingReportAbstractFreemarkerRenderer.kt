package fi.metatavu.metaform.server.billingReport

import fi.metatavu.metaform.server.freemarker.AbstractFreemarkerRenderer
import freemarker.ext.beans.BeansWrapperBuilder
import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import jakarta.annotation.PostConstruct
import jakarta.enterprise.context.ApplicationScoped
import java.io.File

/**
 * Freemarker renderer
 */
@ApplicationScoped
class BillingReportAbstractFreemarkerRenderer : AbstractFreemarkerRenderer() {

    lateinit var configuration: Configuration

    /**
     * Initializes renderer
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

}