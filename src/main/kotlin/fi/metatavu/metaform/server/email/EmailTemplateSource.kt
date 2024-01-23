package fi.metatavu.metaform.server.email

import org.apache.commons.lang3.StringUtils
import java.util.*

/**
 * Freemarker template source
 *
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
enum class EmailTemplateSource(val prefix: String) {

    EMAIL_CONTENT("email-content-"),
    EMAIL_SUBJECT("email-subject-");

    /**
     * Returns template name for id
     *
     * @param id id
     * @return template name for id
     */
    fun getName(id: UUID): String {
        return String.format("%s%s", prefix, id)
    }

    companion object {
        /**
         * Resolve source from template name
         *
         * @param name name
         * @return source
         */
        fun resolve(name: String): EmailTemplateSource? {
            return values().firstOrNull() { templateSource ->
                StringUtils.startsWith(name, templateSource.prefix)
            }
        }
    }
}