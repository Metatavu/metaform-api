package fi.metatavu.metaform.server.metaforms;

import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

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
  
  /**
   * Creates new Metaform
   * 
   * @param exportTheme export theme
   * @param realmId realm id
   * @param data form JSON
   * @return Metaform
   */
  public Metaform createMetaform(ExportTheme exportTheme, String realmId, Boolean allowAnonymous, String title, String data) {
    UUID id = UUID.randomUUID();
    String slug = createSlug(realmId, title);
    return metaformDAO.create(id, slug, exportTheme, realmId, allowAnonymous, data);    
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
   * Lists Metaform from realm
   * 
   * @param realmId realm
   * @return list of Metaforms
   */
  public List<Metaform> listMetaforms(String realmId) {
     return metaformDAO.listByRealmId(realmId);
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
    
    metaformDAO.delete(metaform);
  }

  /**
   * Generates unique slug within a realm for a Metaform
   * 
   * @param realmId realm id
   * @param title title
   * @return unique slug
   */
  private String createSlug(String realmId, String title) {
    Slugify slugify = new Slugify();
    String prefix = StringUtils.isNotBlank(title) ? slugify.slugify(title) : "form";
    int count = 0;
    do {
      String slug = count > 0 ? String.format("%s-%d", prefix, count) : prefix;
      if (metaformDAO.findByRealmIdAndSlug(realmId, slug) == null) {
        return slug;
      }
      
      count++;
    } while (true);
  }
  
}
