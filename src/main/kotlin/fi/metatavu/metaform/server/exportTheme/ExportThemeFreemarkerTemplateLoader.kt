package fi.metatavu.metaform.server.exportTheme

import fi.metatavu.metaform.server.controllers.ExportThemeController
import fi.metatavu.metaform.server.persistence.model.ExportThemeFile
import freemarker.cache.TemplateLoader
import org.slf4j.Logger
import java.io.IOException
import java.io.Reader
import java.io.StringReader
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Freemarker template loader for loading templates from database
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class ExportThemeFreemarkerTemplateLoader: TemplateLoader {
    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var exportThemeController: ExportThemeController

    override fun findTemplateSource(name: String?): Any? {
        return name
    }

    override fun getLastModified(templateSource: Any?): Long {
        val path = templateSource as String?
        val exportThemeFile = findExportThemeFile(path)
        return if (exportThemeFile != null) {
            exportThemeFile.modifiedAt!!.toInstant().toEpochMilli()
        } else 0
    }

    @Throws(IOException::class)
    override fun getReader(templateSource: Any?, encoding: String?): Reader? {
        val path = templateSource as String?
        val exportThemeFile = findExportThemeFile(path)
        if (exportThemeFile != null) {
            return StringReader(exportThemeFile.content)
        }
        logger.warn("Could not find export theme file {}", path)
        return StringReader(String.format("!! export theme file %s not found !!", path))
    }

    private fun findExportThemeFile(path: String?): ExportThemeFile? {
        return exportThemeController.findExportThemeFile(path)
    }

    override fun closeTemplateSource(templateSource: Any?) {
        // Template loader is id, so no need to close
    }

}