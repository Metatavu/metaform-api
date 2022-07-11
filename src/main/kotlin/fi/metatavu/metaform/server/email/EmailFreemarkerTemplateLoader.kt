package fi.metatavu.metaform.server.email

import fi.metatavu.metaform.server.persistence.dao.EmailNotificationDAO
import freemarker.cache.TemplateLoader
import org.slf4j.Logger
import java.io.IOException
import java.io.Reader
import java.io.StringReader
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Freemarker template loader for loading templates from database
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class EmailFreemarkerTemplateLoader : TemplateLoader {
    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var emailNotificationDAO: EmailNotificationDAO

    override fun findTemplateSource(name: String): Any {
        return name
    }

    override fun getLastModified(templateSource: Any): Long {
        val name = templateSource as String
        val source = EmailTemplateSource.resolve(name) ?: return 0L
        val localeIndex = name.indexOf('_')
        val id = UUID.fromString(name.substring(source.prefix.length, localeIndex))
        when (source) {
            EmailTemplateSource.EMAIL_SUBJECT, EmailTemplateSource.EMAIL_CONTENT -> {
                val notificationEmail = emailNotificationDAO.findById(id)
                if (notificationEmail != null) {
                    return notificationEmail.modifiedAt!!.toInstant().toEpochMilli()
                }
            }
            else -> logger.error("Unknown source {}", source)
        }
        return 0
    }

    @Throws(IOException::class)
    override fun getReader(templateSource: Any, encoding: String): Reader? {
        val name = templateSource as String
        val source = EmailTemplateSource.resolve(name) ?: return null
        val localeIndex = name.indexOf('_')
        val id = UUID.fromString(name.substring(source.prefix.length, localeIndex))
        when (source) {
            EmailTemplateSource.EMAIL_SUBJECT, EmailTemplateSource.EMAIL_CONTENT -> {
                val notificationEmail = emailNotificationDAO.findById(id)
                if (notificationEmail != null) {
                    return StringReader(if (source == EmailTemplateSource.EMAIL_SUBJECT) notificationEmail.subjectTemplate else notificationEmail.contentTemplate)
                }
            }
            else -> logger.error("Unknown source {}", source)
        }
        return null
    }

    @Throws(IOException::class)
    override fun closeTemplateSource(templateSource: Any) {
        // Template loader is id, so no need to close
    }
}