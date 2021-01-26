package fi.metatavu.metaform.server.metaforms;

import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.metaform.server.logentry.AuditLogEntryController;
import fi.metatavu.metaform.server.persistence.dao.AuditLogEntryDAO;
import fi.metatavu.metaform.server.persistence.model.AuditLogEntry;
import org.apache.commons.lang3.StringUtils;

import com.github.slugify.Slugify;

import fi.metatavu.metaform.server.persistence.dao.MetaformDAO;
import fi.metatavu.metaform.server.persistence.dao.ReplyDAO;
import fi.metatavu.metaform.server.persistence.model.ExportTheme;
import fi.metatavu.metaform.server.persistence.model.Metaform;
import fi.metatavu.metaform.server.persistence.model.Reply;

/**
 * Metaform controller
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class MetaformController {

  @Inject
  private ReplyController replyController;
 
  @Inject
  private MetaformDAO metaformDAO;

  @Inject
  private ReplyDAO replyDAO;

  @Inject
	private AuditLogEntryDAO auditLogEntryDAO;

  @Inject
	private AuditLogEntryController auditLogEntryController;
  
  /**
   * Creates new Metaform
   * 
   * @param exportTheme export theme
   * @param allowAnonymous allow anonymous
   * @param title title
   * @param data form JSON
   * @return Metaform
   */
  public Metaform createMetaform(ExportTheme exportTheme, Boolean allowAnonymous, String title, String data) {
    UUID id = UUID.randomUUID();
    String slug = createSlug(title);
    return metaformDAO.create(id, slug, exportTheme, allowAnonymous, data);    
  }

  /**
   * Finds Metaform by id
   * 
   * @param id Metaform id
   * @return Metaform
   */
  public Metaform findMetaformById(UUID id) {
    return metaformDAO.findById(id);
  }
  
  /**
   * Lists Metaforms
   * 
   * @return list of Metaforms
   */
  public List<Metaform> listMetaforms() {
     return metaformDAO.listAll();
  }
  
  /**
   * Updates Metaform
   * 
   * @param metaform Metaform
   * @param data form JSON
   * @param allowAnonymous allow anonymous 
   * @return Updated Metaform
   */
  public Metaform updateMetaform(Metaform metaform, ExportTheme exportTheme, String data, Boolean allowAnonymous) {
    metaformDAO.updateData(metaform, data);
    metaformDAO.updateAllowAnonymous(metaform, allowAnonymous);
    metaformDAO.updateExportTheme(metaform, exportTheme);
    return metaform;
  }

  /**
   * Delete Metaform
   * 
   * @param metaform Metaform
   */
  public void deleteMetaform(Metaform metaform) {
    List<Reply> replies = replyDAO.listByMetaform(metaform);

    replies.stream().forEach(replyController::deleteReply);
    List<AuditLogEntry> auditLogEntries = auditLogEntryDAO.listByMetaform(metaform);

    auditLogEntries.stream().forEach(auditLogEntryController::deleteAuditLogEntry);
    metaformDAO.delete(metaform);
  }

  /**
   * Generates unique slug within a realm for a Metaform
   * 
   * @param title title
   * @return unique slug
   */
  private String createSlug(String title) {
    Slugify slugify = new Slugify();
    String prefix = StringUtils.isNotBlank(title) ? slugify.slugify(title) : "form";
    int count = 0;
    do {
      String slug = count > 0 ? String.format("%s-%d", prefix, count) : prefix;
      if (metaformDAO.findBySlug(slug) == null) {
        return slug;
      }
      
      count++;
    } while (true);
  }
  
}
