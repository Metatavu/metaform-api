package fi.metatavu.metaform.server.exporttheme;

import fi.metatavu.metaform.api.spec.model.Attachment;
import fi.metatavu.metaform.api.spec.model.Metaform;
import fi.metatavu.metaform.api.spec.model.Reply;

import java.util.Date;
import java.util.Map;

/**
 * Export data model
 * 
 * @author Antti Leppä
 */
public class ReplyExportDataModel {

  private Metaform metaform;
  private Reply reply;
  private Date createdAt;
  private Date modifiedAt;
  private Map<String, Attachment> attachments;
  
  /**
   * Constructor
   */
  public ReplyExportDataModel() {
  }
  
  /**
   * Constructor
   * 
   * @param metaform metaform
   * @param reply reply
   * @param attachments attachment map
   * @param createdAt createdAt
   * @param modifiedAt modifiedAt
   */
  public ReplyExportDataModel(Metaform metaform, Reply reply, Map<String, Attachment> attachments, Date createdAt, Date modifiedAt) {
    super();

    this.attachments = attachments;
    this.createdAt = createdAt;
    this.modifiedAt = modifiedAt;
    this.metaform = metaform;
    this.reply = reply;
  }

  public Metaform getMetaform() {
    return metaform;
  }
  
  public void setMetaform(Metaform metaform) {
    this.metaform = metaform;
  }
  
  public Reply getReply() {
    return reply;
  }
  
  public void setReply(Reply reply) {
    this.reply = reply;
  }

  public Date getCreatedAt() {
    return createdAt;
  }
  
  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }
  
  public Date getModifiedAt() {
    return modifiedAt;
  }
  
  public void setModifiedAt(Date modifiedAt) {
    this.modifiedAt = modifiedAt;
  }
  
  public Map<String, Attachment> getAttachments() {
    return attachments;
  }
  
  public void setAttachments(Map<String, Attachment> attachments) {
    this.attachments = attachments;
  }
  
}
