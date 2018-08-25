package fi.metatavu.metaform.server.rest;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.metatavu.metaform.server.attachments.AttachmentController;
import fi.metatavu.metaform.server.exporttheme.ExportThemeController;
import fi.metatavu.metaform.server.metaforms.FieldController;
import fi.metatavu.metaform.server.metaforms.FieldFilters;
import fi.metatavu.metaform.server.metaforms.MetaformController;
import fi.metatavu.metaform.server.metaforms.ReplyController;
import fi.metatavu.metaform.server.notifications.EmailNotificationController;
import fi.metatavu.metaform.server.notifications.NotificationController;
import fi.metatavu.metaform.server.persistence.model.Attachment;
import fi.metatavu.metaform.server.rest.model.EmailNotification;
import fi.metatavu.metaform.server.rest.model.ExportTheme;
import fi.metatavu.metaform.server.rest.model.ExportThemeFile;
import fi.metatavu.metaform.server.rest.model.Metaform;
import fi.metatavu.metaform.server.rest.model.MetaformFieldType;
import fi.metatavu.metaform.server.rest.model.Reply;
import fi.metatavu.metaform.server.rest.model.ReplyData;
import fi.metatavu.metaform.server.rest.translate.AttachmentTranslator;
import fi.metatavu.metaform.server.rest.translate.EmailNotificationTranslator;
import fi.metatavu.metaform.server.rest.translate.ExportThemeTranslator;
import fi.metatavu.metaform.server.rest.translate.MetaformTranslator;
import fi.metatavu.metaform.server.rest.translate.ReplyTranslator;

/**
 * Realms REST Service implementation
 * 
 * @author Antti Leppä
 */
@RequestScoped
@Stateful
public class RealmsApiImpl extends AbstractApi implements RealmsApi {
  
  private static final String YOU_ARE_NOT_ALLOWED_TO_UPDATE_METAFORMS = "You are not allowed to update Metaforms";
  private static final String ANONYMOUS_USERS_LIST_METAFORMS_MESSAGE = "Anonymous users are not allowed to list Metaforms";
  private static final String ANONYMOUS_USERS_FIND_METAFORM_MESSAGE = "Anonymous users are not allowed to find Metaforms";
  private static final String NOT_ALLOWED_TO_VIEW_THESE_REPLIES = "You are not allowed to view these replies";
  private static final String NOT_ALLOWED_TO_VIEW_REPLY_MESSAGE = "You are not allowed to view this reply";
  private static final String ANONYMOUS_USERS_MESSAGE = "Anonymous users are not allowed on this Metaform";

  @Inject
  private Logger logger;
  
  @Inject
  private MetaformController metaformController;

  @Inject
  private ReplyController replyController;
  
  @Inject
  private FieldController fieldController;

  @Inject
  private EmailNotificationController emailNotificationController;

  @Inject
  private NotificationController notificationController;

  @Inject
  private AttachmentController attachmentController;

  @Inject
  private ExportThemeController exportThemeController;

  @Inject
  private ExportThemeTranslator exportThemeTranslator;

  @Inject
  private AttachmentTranslator attachmentTranslator;

  @Inject
  private MetaformTranslator metaformTranslator;

  @Inject
  private ReplyTranslator replyTranslator;

  @Inject
  private EmailNotificationTranslator emailNotificationTranslator;
  
  @Override
  public Response createReply(String realmId, UUID metaformId, Reply payload, Boolean updateExisting, String replyModeParam) throws Exception {
    UUID loggedUserId = getLoggerUserId();
    if (loggedUserId == null) {
      return createForbidden(UNAUTHORIZED);
    }
    
    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    boolean anonymous = !isRealmUser();
    if (!metaform.getAllowAnonymous() && anonymous) {
      return createForbidden(ANONYMOUS_USERS_MESSAGE);
    }
    
    UUID userId = getReplyUserId(payload);
    
    ReplyMode replyMode = EnumUtils.getEnum(ReplyMode.class, replyModeParam);
    if (replyModeParam != null && replyMode == null) {
      return createBadRequest(String.format("Invalid reply mode %s", replyModeParam));
    }
    
    if (updateExisting != null) {
      replyMode = updateExisting ? ReplyMode.UPDATE : ReplyMode.REVISION;
    }
    
    if (replyMode == null) {
      replyMode = ReplyMode.UPDATE;
    }

    Metaform metaformEntity = metaformTranslator.translateMetaform(metaform);
    
    // TODO: Permission check
    // TODO: Support multiple
    
    fi.metatavu.metaform.server.persistence.model.Reply reply = createReplyResolveReply(replyMode, metaform, anonymous, userId);
    ReplyData data = payload.getData();
    if (data == null) {
      logger.warn("Received a reply with null data");
    } else {
      Map<String, MetaformFieldType> fieldTypeMap = fieldController.getFieldTypeMap(metaformEntity);
      for (Entry<String, Object> entry : data.entrySet()) {
        String fieldName = entry.getKey();
        Object fieldValue = entry.getValue();
        
        if (fieldValue != null) {
          replyController.setReplyField(fieldTypeMap.get(fieldName), reply, fieldName, fieldValue);
        }
      }
    }
    
    Reply replyEntity = replyTranslator.translateReply(metaformEntity, reply);
    notificationController.notifyNewReply(metaform, replyEntity);
    
    return createOk(replyEntity);
  }

  public Response findReply(String realmId, UUID metaformId, UUID replyId) throws Exception {
    if (!isRealmUser()) {
      return createForbidden(ANONYMOUS_USERS_MESSAGE);
    }
    
    // TODO: Permission check
    
    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    fi.metatavu.metaform.server.persistence.model.Reply reply = replyController.findReplyById(replyId);
    if (reply == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    if (!isRealmMetaformAdmin() && !getLoggerUserId().equals(reply.getUserId())) {
      return createForbidden(NOT_ALLOWED_TO_VIEW_REPLY_MESSAGE);
    }
    
    if (!reply.getMetaform().getId().equals(metaform.getId())) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    Metaform metaformEntity = metaformTranslator.translateMetaform(metaform);
    return createOk(replyTranslator.translateReply(metaformEntity, reply));
  }
  
  @Override
  public Response listReplies(String realmId, UUID metaformId, UUID userId, String createdBeforeParam, String createdAfterParam,
      String modifiedBeforeParam, String modifiedAfterParam, Boolean includeRevisions, List<String> fields) throws Exception {
    // TODO: Permission check
    
    OffsetDateTime createdBefore = parseTime(createdBeforeParam);
    OffsetDateTime createdAfter = parseTime(createdAfterParam);
    OffsetDateTime modifiedBefore = parseTime(modifiedBeforeParam);
    OffsetDateTime modifiedAfter = parseTime(modifiedAfterParam);

    if (!isRealmUser()) {
      return createForbidden(ANONYMOUS_USERS_MESSAGE);
    }
    
    if ((userId == null || !userId.equals(getLoggerUserId())) && (!hasRealmRole(ADMIN_ROLE, VIEW_ALL_REPLIES_ROLE))) {
      return createForbidden(NOT_ALLOWED_TO_VIEW_THESE_REPLIES);
    }
    
    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    Metaform metaformEntity = metaformTranslator.translateMetaform(metaform);
    
    FieldFilters fieldFilters = fieldController.parseFilters(metaformEntity, fields);

    List<fi.metatavu.metaform.server.persistence.model.Reply> replies = replyController.listReplies(metaform, 
        userId, 
        createdBefore, 
        createdAfter, 
        modifiedBefore, 
        modifiedAfter,
        includeRevisions == null ? false : includeRevisions,
        fieldFilters);
    
    List<Reply> result = replies.stream().map(entity -> 
     replyTranslator.translateReply(metaformEntity, entity)
    ).collect(Collectors.toList());
    
    return createOk(result);
  }

  public Response updateReply(String realmId, UUID metaformId, UUID replyId, Reply payload) throws Exception {
    if (!isRealmUser()) {
      return createForbidden(ANONYMOUS_USERS_MESSAGE);
    }
    
    UUID loggedUserId = getLoggerUserId();
    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    fi.metatavu.metaform.server.persistence.model.Reply reply = replyController.findReplyById(replyId);
    if (reply == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    if (!isRealmMetaformAdmin() && !reply.getUserId().equals(loggedUserId)) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    Metaform metaformEntity = metaformTranslator.translateMetaform(metaform);
    
    List<String> fieldNames = new ArrayList<>(replyController.listFieldNames(reply));
    ReplyData data = payload.getData();
    Map<String, MetaformFieldType> fieldTypeMap = fieldController.getFieldTypeMap(metaformEntity);

    for (Entry<String, Object> entry : data.entrySet()) {
      String fieldName = entry.getKey();
      replyController.setReplyField(fieldTypeMap.get(fieldName), reply, fieldName, entry.getValue());
      fieldNames.remove(fieldName);
    }
    
    replyController.deleteReplyFields(reply, fieldNames);
    
    return createNoContent();
  }

  @Override
  public Response deleteReply(String realmId, UUID metaformId, UUID replyId) throws Exception {
    if (!isRealmMetaformAdmin()) {
      return createForbidden("You are not allowed to delete replies");
    }

    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    fi.metatavu.metaform.server.persistence.model.Reply reply = replyController.findReplyById(replyId);
    if (reply == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    if (!reply.getMetaform().getId().equals(metaform.getId())) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    replyController.deleteReply(reply);
    
    return null;
  }

  @Override
  public Response replyExport(String realmId, UUID metaformId, UUID replyId, String format) throws Exception {
    // TODO: Add in issue #46 (https://github.com/Metatavu/metaform-api/issues/46)
    return null;
  }

  @Override
  public Response createMetaform(String realmId, Metaform payload) throws Exception {
    if (!isRealmMetaformAdmin()) {
      return createForbidden("You are not allowed to create Metaforms");
    }

    String data = serializeMetaform(payload);
    if (data == null) {
      return createBadRequest("Invalid Metaform JSON");  
    }
    
    Boolean allowAnonymous = payload.isAllowAnonymous();
    if (allowAnonymous == null) {
      allowAnonymous  = false;
    }
    
    // TODO: Permission check
    
    return createOk(metaformTranslator.translateMetaform(metaformController.createMetaform(realmId, allowAnonymous, data)));
  }

  public Response listMetaforms(String realmId) throws Exception {
    // TODO: Permission check
    if (!isRealmUser()) {
      return createForbidden(ANONYMOUS_USERS_LIST_METAFORMS_MESSAGE);
    }

    return createOk(metaformController.listMetaforms(realmId).stream().map((entity) -> {
      return metaformTranslator.translateMetaform(entity);
    }).collect(Collectors.toList()));
  }

  public Response findMetaform(String realmId, UUID metaformId) throws Exception {
    // TODO: Permission check
    
    UUID loggedUserId = getLoggerUserId();
    if (loggedUserId == null) {
      return createForbidden(UNAUTHORIZED);
    }
    
    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    boolean anonymous = !isRealmUser();
    if (!metaform.getAllowAnonymous() && anonymous) {
      return createForbidden(ANONYMOUS_USERS_FIND_METAFORM_MESSAGE);
    }
    
    if (!StringUtils.equals(metaform.getRealmId(), realmId)) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    return createOk(metaformTranslator.translateMetaform(metaform));
  }

  @Override
  public Response updateMetaform(String realmId, UUID metaformId, Metaform payload) throws Exception {
    if (!isRealmMetaformAdmin()) {
      return createForbidden(YOU_ARE_NOT_ALLOWED_TO_UPDATE_METAFORMS);
    }

    String data = serializeMetaform(payload);
    if (data == null) {
      return createBadRequest("Invalid Metaform JSON");  
    }
    
    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    Boolean allowAnonymous = payload.isAllowAnonymous();
    if (allowAnonymous == null) {
      allowAnonymous = metaform.getAllowAnonymous();
    }
    
    // TODO: Permission check
    
    return createOk(metaformTranslator.translateMetaform(metaformController.updateMetaform(metaform, data, allowAnonymous)));
  }
  
  @Override
  public Response deleteMetaform(String realmId, UUID metaformId) throws Exception {
    if (!isRealmMetaformAdmin()) {
      return createForbidden("You are not allowed to delete Metaforms");
    }

    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    // TODO: Permission check
    
    metaformController.deleteMetaform(metaform);
    
    return createNoContent();
  }

  public Response export(String realmId, UUID metaformId, String format) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  public Response findAttachment(String realmId, UUID attachmentId) throws Exception {
    if (!isRealmUser()) {
      return createForbidden(ANONYMOUS_USERS_MESSAGE);
    }
    
    Attachment attachment = attachmentController.findAttachmentById(attachmentId);
    if (attachment == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    return createOk(attachmentTranslator.translateAttachment(attachment));
  }

  @Override
  public Response findAttachmentData(String realmId, UUID attachmentId) throws Exception {
    if (!isRealmUser()) {
      return createForbidden(ANONYMOUS_USERS_MESSAGE);
    }
    
    Attachment attachment = attachmentController.findAttachmentById(attachmentId);
    if (attachment == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    return streamResponse(attachment.getContent(), attachment.getContentType());
  }

  /**
   * Resolves reply object when creating new reply
   * 
   * @param replyMode reply mode
   * @param metaform metaform
   * @param anonymous is user anonymous
   * @param userId user id
   * @return reply object
   */
  private fi.metatavu.metaform.server.persistence.model.Reply createReplyResolveReply(ReplyMode replyMode, fi.metatavu.metaform.server.persistence.model.Metaform metaform, boolean anonymous, UUID userId) {
    fi.metatavu.metaform.server.persistence.model.Reply reply = null;
    
    if (anonymous || replyMode == ReplyMode.CUMULATIVE) {
      reply = replyController.createReply(userId, metaform);
    } else {
      reply = replyController.findActiveReplyByMetaformAndUserId(metaform, userId);
      if (reply == null) {
        reply = replyController.createReply(userId, metaform);
      } else {
        if (replyMode == ReplyMode.REVISION) {
          // If there is already an existing reply but we are not updating it
          // We need to change the existing reply into a revision and create new reply
          replyController.convertToRevision(reply);
          reply = replyController.createReply(userId, metaform);
        }
      }
    }
    
    return reply;
  }

  @Override
  public Response createEmailNotification(String realmId, UUID metaformId, EmailNotification payload) throws Exception {
    if (!isRealmMetaformAdmin()) {
      return createForbidden(YOU_ARE_NOT_ALLOWED_TO_UPDATE_METAFORMS);
    }
    
    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    fi.metatavu.metaform.server.persistence.model.notifications.EmailNotification emailNotification = emailNotificationController.createEmailNotification(metaform, payload.getSubjectTemplate(), payload.getContentTemplate(), payload.getEmails());
    
    return createOk(emailNotificationTranslator.translateEmailNotification(emailNotification));    
  }

  @Override
  public Response deleteEmailNotification(String realmId, UUID metaformId, UUID emailNotificationId) throws Exception {
    if (!isRealmMetaformAdmin()) {
      return createForbidden(YOU_ARE_NOT_ALLOWED_TO_UPDATE_METAFORMS);
    }
    
    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    fi.metatavu.metaform.server.persistence.model.notifications.EmailNotification emailNotification = emailNotificationController.findEmailNotificationById(emailNotificationId);
    if (emailNotification == null || !emailNotification.getMetaform().getId().equals(metaform.getId())) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    emailNotificationController.deleteEmailNotification(emailNotification);
    
    return createNoContent();
  }

  @Override
  public Response findEmailNotification(String realmId, UUID metaformId, UUID emailNotificationId) throws Exception {
    if (!isRealmMetaformAdmin()) {
      return createForbidden(YOU_ARE_NOT_ALLOWED_TO_UPDATE_METAFORMS);
    }
    
    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    fi.metatavu.metaform.server.persistence.model.notifications.EmailNotification emailNotification = emailNotificationController.findEmailNotificationById(emailNotificationId);
    if (emailNotification == null || !emailNotification.getMetaform().getId().equals(metaform.getId())) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    return createOk(emailNotificationTranslator.translateEmailNotification(emailNotification));
  }

  @Override
  public Response listEmailNotifications(String realmId, UUID metaformId) throws Exception {
    if (!isRealmMetaformAdmin()) {
      return createForbidden(YOU_ARE_NOT_ALLOWED_TO_UPDATE_METAFORMS);
    }
    
    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    return createOk(emailNotificationController.listEmailNotificationByMetaform(metaform).stream().map(emailNotificationTranslator::translateEmailNotification).collect(Collectors.toList()));
  }

  @Override
  public Response updateEmailNotification(String realmId, UUID metaformId, UUID emailNotificationId, EmailNotification payload) throws Exception {
    if (!isRealmMetaformAdmin()) {
      return createForbidden(YOU_ARE_NOT_ALLOWED_TO_UPDATE_METAFORMS);
    }
    
    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    fi.metatavu.metaform.server.persistence.model.notifications.EmailNotification emailNotification = emailNotificationController.findEmailNotificationById(emailNotificationId);
    if (emailNotification == null || !emailNotification.getMetaform().getId().equals(metaform.getId())) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    emailNotificationController.updateEmailNotification(emailNotification, payload.getSubjectTemplate(), payload.getContentTemplate(), payload.getEmails());
    
    return createOk(emailNotificationTranslator.translateEmailNotification(emailNotification));
  }


  @Override
  public Response createExportTheme(String realmId, ExportTheme payload) throws Exception {
    if (!isRealmMetaformAdmin()) {
      return createForbidden(YOU_ARE_NOT_ALLOWED_TO_UPDATE_METAFORMS);
    }
    
    UUID parentId = payload.getParentId();
    fi.metatavu.metaform.server.persistence.model.ExportTheme parent = null;
    UUID loggedUserId = getLoggerUserId();
    
    if (parentId != null) {
      parent = exportThemeController.findExportTheme(parentId);
      if (parent == null) {
        return createBadRequest(String.format("Theme %s does not exist", parentId));
      }
    }
    
    fi.metatavu.metaform.server.persistence.model.ExportTheme exportTheme = exportThemeController.createExportTheme(payload.getLocales(), parent, payload.getName(), loggedUserId);
    
    return createOk(exportThemeTranslator.translateExportTheme(exportTheme));
  }

  @Override
  public Response createExportThemeFile(String realmId, UUID exportThemeId, ExportThemeFile payload) throws Exception {
    if (!isRealmMetaformAdmin()) {
      return createForbidden(YOU_ARE_NOT_ALLOWED_TO_UPDATE_METAFORMS);
    }
 
    UUID themeId = payload.getThemeId();
    UUID loggedUserId = getLoggerUserId();
    
    fi.metatavu.metaform.server.persistence.model.ExportTheme theme = exportThemeController.findExportTheme(themeId);
    if (theme == null) {
      return createBadRequest(String.format("Theme %s does not exist", themeId));
    }

    fi.metatavu.metaform.server.persistence.model.ExportThemeFile themeFile = exportThemeController.createExportThemeFile(theme, payload.getPath(), payload.getContent(), loggedUserId);
    
    return createOk(exportThemeTranslator.translateExportThemeFile(themeFile));
  }

  @Override
  public Response deleteExportTheme(String realmId, UUID exportThemeId) throws Exception {
    if (!isRealmMetaformAdmin()) {
      return createForbidden(YOU_ARE_NOT_ALLOWED_TO_UPDATE_METAFORMS);
    }

    fi.metatavu.metaform.server.persistence.model.ExportTheme theme = exportThemeController.findExportTheme(exportThemeId);
    if (theme == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    exportThemeController.deleteTheme(theme);

    return createNoContent();
  }

  @Override
  public Response deleteExportThemeFile(String realmId, UUID exportThemeId, UUID exportThemeFileId) throws Exception {
    if (!isRealmMetaformAdmin()) {
      return createForbidden(YOU_ARE_NOT_ALLOWED_TO_UPDATE_METAFORMS);
    }

    fi.metatavu.metaform.server.persistence.model.ExportTheme theme = exportThemeController.findExportTheme(exportThemeId);
    if (theme == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    fi.metatavu.metaform.server.persistence.model.ExportThemeFile exportThemeFile = exportThemeController.findExportThemeFile(exportThemeFileId);
    if (exportThemeFile == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    if (!exportThemeFile.getTheme().getId().equals(theme.getId())) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    exportThemeController.deleteTheme(theme);

    return createNoContent();
  }

  @Override
  public Response findExportTheme(String realmId, UUID exportThemeId) throws Exception {
    if (!isRealmMetaformAdmin()) {
      return createForbidden(YOU_ARE_NOT_ALLOWED_TO_UPDATE_METAFORMS);
    }

    fi.metatavu.metaform.server.persistence.model.ExportTheme exportTheme = exportThemeController.findExportTheme(exportThemeId);
    if (exportTheme == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    return createOk(exportThemeTranslator.translateExportTheme(exportTheme));
  }

  @Override
  public Response findExportThemeFile(String realmId, UUID exportThemeId, UUID exportThemeFileId) throws Exception {
    if (!isRealmMetaformAdmin()) {
      return createForbidden(YOU_ARE_NOT_ALLOWED_TO_UPDATE_METAFORMS);
    }

    fi.metatavu.metaform.server.persistence.model.ExportTheme theme = exportThemeController.findExportTheme(exportThemeId);
    if (theme == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    fi.metatavu.metaform.server.persistence.model.ExportThemeFile exportThemeFile = exportThemeController.findExportThemeFile(exportThemeFileId);
    if (exportThemeFile == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    if (!exportThemeFile.getTheme().getId().equals(theme.getId())) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    return createOk(exportThemeTranslator.translateExportThemeFile(exportThemeFile));
  }

  @Override
  public Response listExportThemeFiles(String realmId, UUID exportThemeId) throws Exception {
    if (!isRealmMetaformAdmin()) {
      return createForbidden(YOU_ARE_NOT_ALLOWED_TO_UPDATE_METAFORMS);
    }

    fi.metatavu.metaform.server.persistence.model.ExportTheme theme = exportThemeController.findExportTheme(exportThemeId);
    if (theme == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    return createOk(exportThemeController.listExportThemeFiles(theme).stream()
      .map(exportThemeTranslator::translateExportThemeFile)
      .collect(Collectors.toList()));
  }

  @Override
  public Response listExportThemes(String realmId) throws Exception {
    if (!isRealmMetaformAdmin()) {
      return createForbidden(YOU_ARE_NOT_ALLOWED_TO_UPDATE_METAFORMS);
    }

    return createOk(exportThemeController.listExportThemes().stream()
      .map(exportThemeTranslator::translateExportTheme)
      .collect(Collectors.toList()));
  }

  @Override
  public Response updateExportTheme(String realmId, UUID exportThemeId, ExportTheme payload) throws Exception {
    if (!isRealmMetaformAdmin()) {
      return createForbidden(YOU_ARE_NOT_ALLOWED_TO_UPDATE_METAFORMS);
    }

    fi.metatavu.metaform.server.persistence.model.ExportTheme theme = exportThemeController.findExportTheme(exportThemeId);
    if (theme == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    UUID parentId = payload.getParentId();
    fi.metatavu.metaform.server.persistence.model.ExportTheme parent = null;
    UUID loggedUserId = getLoggerUserId();
    
    if (parentId != null) {
      parent = exportThemeController.findExportTheme(parentId);
      if (parent == null) {
        return createBadRequest(String.format("Theme %s does not exist", parentId));
      }
    }

    return createOk(exportThemeTranslator.translateExportTheme(exportThemeController.updateExportTheme(theme, payload.getLocales(), parent, payload.getName(), loggedUserId)));
  }

  @Override
  public Response updateExportThemeFile(String realmId, UUID exportThemeId, UUID exportThemeFileId, ExportThemeFile payload) throws Exception {
    if (!isRealmMetaformAdmin()) {
      return createForbidden(YOU_ARE_NOT_ALLOWED_TO_UPDATE_METAFORMS);
    }

    fi.metatavu.metaform.server.persistence.model.ExportTheme theme = exportThemeController.findExportTheme(exportThemeId);
    UUID loggedUserId = getLoggerUserId();
    
    if (theme == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    fi.metatavu.metaform.server.persistence.model.ExportThemeFile exportThemeFile = exportThemeController.findExportThemeFile(exportThemeFileId);
    if (exportThemeFile == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    if (!exportThemeFile.getTheme().getId().equals(theme.getId())) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }
    
    exportThemeController.updateExportThemeFile(exportThemeFile, payload.getPath(), payload.getContent(), loggedUserId);

    return createOk(exportThemeTranslator.translateExportThemeFile(exportThemeFile));
  }
  
  /**
   * Resolves reply user from payload. If user has appropriate permissions user can 
   * be other than logged user, otherwise logged user is returned
   * 
   * @param reply reply
   * @return reply user id
   */
  private UUID getReplyUserId(Reply reply) {
    UUID userId = reply.getUserId();
    if (!isRealmMetaformAdmin() || userId == null) {
      return getLoggerUserId();
    }
    
    return userId;
  }

  private String serializeMetaform(Metaform metaform) {
    ObjectMapper objectMapper = new ObjectMapper();
    
    try {
      return objectMapper.writeValueAsString(metaform);
    } catch (JsonProcessingException e) {
      logger.error("Failed to serialze metaform", e);
    }
    
    return null;
  }

}
