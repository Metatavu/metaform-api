package fi.metatavu.metaform.server.metaforms;

import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.metaform.server.persistence.dao.AnyReplyFieldDAO;
import fi.metatavu.metaform.server.persistence.dao.MetaformDAO;
import fi.metatavu.metaform.server.persistence.dao.ReplyDAO;
import fi.metatavu.metaform.server.persistence.model.Metaform;
import fi.metatavu.metaform.server.persistence.model.Reply;
import fi.metatavu.metaform.server.persistence.model.ReplyField;

/**
 * Metaform controller
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class MetaformController {
 
  @Inject
  private MetaformDAO metaformDAO;

  @Inject
  private ReplyDAO replyDAO;

  @Inject
  private AnyReplyFieldDAO anyReplyFieldDAO;
  
  /**
   * Creates new Metaform
   * 
   * @param realmId realm id
   * @param data form JSON
   * @return Metaform
   */
  public Metaform createMetaform(String realmId, Boolean allowAnonymous, String data) {
    UUID id = UUID.randomUUID();
    return metaformDAO.create(id, realmId, allowAnonymous, data);    
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
  public Metaform updateMetaform(Metaform metaform, String data, Boolean allowAnonymous) {
    metaformDAO.updateData(metaform, data);
    metaformDAO.updateAllowAnonymous(metaform, allowAnonymous);
    return metaform;
  }

  /**
   * Delete Metaform
   * 
   * @param metaform Metaform
   */
  public void deleteMetaform(Metaform metaform) {
    List<Reply> replies = replyDAO.listByMetaform(metaform);
    
    replies.stream().forEach(reply -> {
      List<ReplyField> fields = anyReplyFieldDAO.listByReply(reply);
      fields.stream().forEach(field -> anyReplyFieldDAO.delete(field));
      replyDAO.delete(reply);
    });
    
    metaformDAO.delete(metaform);
  }
  
}
