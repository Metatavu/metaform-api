package fi.metatavu.metaform.server.rest.translate;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.metatavu.metaform.server.persistence.model.AuditLogEntry;
import fi.metatavu.metaform.server.rest.model.Metaform;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;


/**
 * Translator for Audit Log Entries
 *
 * @author Antti Leppä
 */
@ApplicationScoped
public class AulditLogEntryTranslator {

    @Inject
    private Logger logger;

    /**
     * Translates JPA Metaform into REST Metaform
     *
     * @param entity JPA Metaform
     * @return REST Metaform
     */
    public fi.metatavu.metaform.server.rest.model.AuditLogEntry translateAuditLogEntry(fi.metatavu.metaform.server.persistence.model.AuditLogEntry entity) {
        if (entity == null) {
            return null;
        }

        fi.metatavu.metaform.server.rest.model.AuditLogEntry result = null;




        return result;
    }
}
