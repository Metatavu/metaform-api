package fi.metatavu.metaform.server.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.metatavu.metaform.api.spec.V1Api;
import fi.metatavu.metaform.api.spec.model.*;
import fi.metatavu.metaform.server.attachments.AttachmentController;
import fi.metatavu.metaform.server.crypto.CryptoController;
import fi.metatavu.metaform.server.drafts.DraftController;
import fi.metatavu.metaform.server.exporttheme.ExportThemeController;
import fi.metatavu.metaform.server.keycloak.AuthorizationScope;
import fi.metatavu.metaform.server.keycloak.KeycloakAdminUtils;
import fi.metatavu.metaform.server.logentry.AuditLogEntryController;
import fi.metatavu.metaform.server.metaforms.FieldController;
import fi.metatavu.metaform.server.metaforms.FieldFilters;
import fi.metatavu.metaform.server.metaforms.MetaformController;
import fi.metatavu.metaform.server.metaforms.ReplyController;
import fi.metatavu.metaform.server.notifications.EmailNotificationController;
import fi.metatavu.metaform.server.pdf.PdfRenderException;
import fi.metatavu.metaform.server.persistence.model.Draft;
import fi.metatavu.metaform.server.rest.translate.*;
import fi.metatavu.metaform.server.script.FormRuntimeContext;
import fi.metatavu.metaform.server.script.ScriptController;
import fi.metatavu.metaform.server.settings.SystemSettingController;
import fi.metatavu.metaform.server.xlsx.XlsxException;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.Configuration;
import org.keycloak.authorization.client.representation.TokenIntrospectionResponse;
import org.keycloak.authorization.client.util.HttpResponseException;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.authorization.AuthorizationRequest;
import org.keycloak.representations.idm.authorization.AuthorizationResponse;
import org.keycloak.representations.idm.authorization.Permission;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequestScoped
@Transactional
public class V1ApiImpl extends AbstractApi implements V1Api {

  private static final String THEME_DOES_NOT_EXIST = "Theme %s does not exist";
  private static final String YOU_ARE_NOT_ALLOWED_TO_UPDATE_THEMES = "You are not allowed to update themes";

  private static final String USER_POLICY_NAME = "user";
  private static final String OWNER_POLICY_NAME = "owner";
  private static final String METAFORM_ADMIN_POLICY_NAME = "metaform-admin";
  private static final String REPLY_PERMISSION_NAME_TEMPLATE = "permission-%s-%s";
  private static final String REPLY_GROUP_NAME_TEMPLATE = "%s:%s:%s";

  private static final String YOU_ARE_NOT_ALLOWED_TO_UPDATE_METAFORMS = "You are not allowed to update Metaforms";
  private static final String ANONYMOUS_USERS_LIST_METAFORMS_MESSAGE = "Anonymous users are not allowed to list Metaforms";
  private static final String ANONYMOUS_USERS_FIND_METAFORM_MESSAGE = "Anonymous users are not allowed to find Metaforms";
  private static final String NOT_ALLOWED_TO_VIEW_REPLY_MESSAGE = "You are not allowed to view this reply";
  private static final String NOT_ALLOWED_TO_UPDATE_REPLY_MESSAGE = "You are not allowed to edit this reply";
  private static final String ANONYMOUS_USERS_MESSAGE = "Anonymous users are not allowed on this Metaform";
  private static final String DRAFTS_NOT_ALLOWED = "Draft are not allowed on this Metaform";
  private static final String YOU_ARE_NOT_ALLOWE_TO_DELETE_LOGS = "You are not allowed to delete logs";

  @Inject
  private Logger logger;

  @Inject
  private ExportThemeController exportThemeController;

  @Inject
  private ExportThemeTranslator exportThemeTranslator;

  @Inject
  private MetaformController metaformController;

  @Inject
  private ReplyController replyController;

  @Inject
  private FieldController fieldController;

  @Inject
  private EmailNotificationController emailNotificationController;

  @Inject
  private AttachmentController attachmentController;

  @Inject
  private AttachmentTranslator attachmentTranslator;

  @Inject
  private MetaformTranslator metaformTranslator;

  @Inject
  private ReplyTranslator replyTranslator;

  @Inject
  private EmailNotificationTranslator emailNotificationTranslator;

  @Inject
  private FormRuntimeContext formRuntimeContext;

  @Inject
  private ScriptController scriptController;

  @Inject
  private DraftTranslator draftTranslator;

  @Inject
  private DraftController draftController;

  @Inject
  private CryptoController cryptoController;

  @Inject
  private AuditLogEntryController auditLogEntryController;

  @Inject
  private AuditLogEntryTranslator auditLogEntryTranslator;

  @Inject
  private SystemSettingController systemSettingController;


  @Override
  public Response createDraft(UUID metaformId, @Valid fi.metatavu.metaform.api.spec.model.Draft payload) {
    UUID loggedUserId = getLoggerUserId();
    if (loggedUserId == null) {
      return createForbidden(UNAUTHORIZED);
    }

    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    Metaform metaformEntity = metaformTranslator.translateMetaform(metaform);
    if (BooleanUtils.isNotTrue(metaformEntity.getAllowDrafts())) {
      return createForbidden(DRAFTS_NOT_ALLOWED);
    }

    boolean anonymous = !isRealmUser();
    if (!metaform.getAllowAnonymous() && anonymous) {
      return createForbidden(ANONYMOUS_USERS_MESSAGE);
    }

    fi.metatavu.metaform.server.persistence.model.Draft draft = draftController.createDraft(metaform, getLoggerUserId(), payload.getData());
    fi.metatavu.metaform.api.spec.model.Draft draftEntity = draftTranslator.translateDraft(draft);

    return createOk(draftEntity);
  }

  @Override
  public Response createEmailNotification(UUID metaformId, @Valid EmailNotification payload) {
    if (!isRealmMetaformAdmin()) {
      return createForbidden(YOU_ARE_NOT_ALLOWED_TO_UPDATE_METAFORMS);
    }

    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    fi.metatavu.metaform.server.persistence.model.notifications.EmailNotification emailNotification;
    try {
      emailNotification = emailNotificationController.createEmailNotification(
        metaform,
        payload.getSubjectTemplate(),
        payload.getContentTemplate(),
        payload.getEmails(),
        payload.getNotifyIf());
    } catch (JsonProcessingException e) {
      return createBadRequest(e.getMessage());
    }

    return createOk(emailNotificationTranslator.translateEmailNotification(emailNotification));
  }

  @Override
  public Response createExportTheme(@Valid ExportTheme payload) {
    if (!isRealmMetaformSuper()) {
      return createForbidden(YOU_ARE_NOT_ALLOWED_TO_UPDATE_THEMES);
    }

    UUID parentId = payload.getParentId();
    fi.metatavu.metaform.server.persistence.model.ExportTheme parent = null;
    UUID loggedUserId = getLoggerUserId();

    if (parentId != null) {
      parent = exportThemeController.findExportTheme(parentId);
      if (parent == null) {
        return createBadRequest(String.format(THEME_DOES_NOT_EXIST, parentId));
      }
    }

    fi.metatavu.metaform.server.persistence.model.ExportTheme exportTheme = exportThemeController.createExportTheme(payload.getLocales(), parent, payload.getName(), loggedUserId);

    return createOk(exportThemeTranslator.translateExportTheme(exportTheme));
  }

  @Override
  public Response createExportThemeFile(UUID exportThemeId, @Valid ExportThemeFile payload) {
    if (!isRealmMetaformSuper()) {
      return createForbidden(YOU_ARE_NOT_ALLOWED_TO_UPDATE_THEMES);
    }

    UUID themeId = payload.getThemeId();
    UUID loggedUserId = getLoggerUserId();

    fi.metatavu.metaform.server.persistence.model.ExportTheme theme = exportThemeController.findExportTheme(themeId);
    if (theme == null) {
      return createBadRequest(String.format(THEME_DOES_NOT_EXIST, themeId));
    }

    fi.metatavu.metaform.server.persistence.model.ExportThemeFile themeFile = exportThemeController.createExportThemeFile(theme, payload.getPath(), payload.getContent(), loggedUserId);

    return createOk(exportThemeTranslator.translateExportThemeFile(themeFile));
  }

  @Override
  public Response createMetaform(@Valid Metaform payload) {
    if (!isRealmMetaformAdmin()) {
      return createForbidden("You are not allowed to create Metaforms");
    }

    String data = serializeMetaform(payload);
    if (data == null) {
      return createBadRequest("Invalid Metaform JSON");
    }

    Boolean allowAnonymous = payload.getAllowAnonymous();
    if (allowAnonymous == null) {
      allowAnonymous  = false;
    }

    Response validationResponse = validateMetaform(payload);
    if (validationResponse != null) {
      return validationResponse;
    }

    fi.metatavu.metaform.server.persistence.model.ExportTheme exportTheme = null;
    if (payload.getExportThemeId() != null) {
      exportTheme = exportThemeController.findExportTheme(payload.getExportThemeId());
      if (exportTheme == null) {
        return createBadRequest("Invalid exportThemeId");
      }
    }

    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.createMetaform(exportTheme, allowAnonymous, payload.getTitle(), data);
    updateMetaformPermissionGroups(metaform.getSlug(), payload);
    return createOk(metaformTranslator.translateMetaform(metaform));
  }

  @Override
  public Response createReply(UUID metaformId, @Valid Reply payload, Boolean updateExisting, String replyModeParam) {
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

    EnumMap<AuthorizationScope, List<String>> permissionGroups = new EnumMap<>(AuthorizationScope.class);
    Arrays.stream(AuthorizationScope.values()).forEach(scope -> permissionGroups.put(scope, new ArrayList<>()));

    PrivateKey privateKey = null;
    PublicKey publicKey = null;

    if (BooleanUtils.isTrue(metaformEntity.getAllowReplyOwnerKeys())) {
      KeyPair keyPair = cryptoController.generateRsaKeyPair();
      privateKey = keyPair.getPrivate();
      publicKey = keyPair.getPublic();
    }

    // TODO: Support multiple

    fi.metatavu.metaform.server.persistence.model.Reply reply = replyController.createReplyResolveReply(replyMode, metaform, anonymous, userId, privateKey);
    Map<String, Object> data = payload.getData();
    if (data == null) {
      logger.warn("Received a reply with null data");
    } else {
      Map<String, MetaformField> fieldMap = fieldController.getFieldMap(metaformEntity);
      for (Map.Entry<String, Object> entry : data.entrySet()) {
        String fieldName = entry.getKey();
        Object fieldValue = entry.getValue();
        MetaformField field = fieldMap.get(fieldName);

        if (field == null) {
          return createBadRequest(String.format("Invalid field %s", fieldName));
        }

        if (fieldValue != null) {
          if (!replyController.isValidFieldValue(field, fieldValue)) {
            return createBadRequest(String.format("Invalid field value for field %s", fieldName));
          }

          replyController.setReplyField(field, reply, fieldName, fieldValue);
          metaformController.addPermissionContextGroups(permissionGroups, metaform.getSlug(), field, fieldValue);
        }
      }
    }

    Reply replyEntity = replyTranslator.translateReply(metaformEntity, reply, publicKey);

    if (metaformEntity.getScripts() != null && metaformEntity.getScripts().getAfterCreateReply() != null) {
      setupFormRuntimeContext(metaform, metaformEntity, replyEntity);
      scriptController.runScripts(metaformEntity.getScripts().getAfterCreateReply());
    }

    metaformController.handleReplyPostPersist(true, metaform, reply, replyEntity, permissionGroups);

    auditLogEntryController.generateAuditLog(metaform, getLoggerUserId(), reply.getId(), null, null, AuditLogEntryType.CREATE_REPLY);
    return createOk(replyEntity);
  }

  @Override
  public Response deleteAuditLogEntry(UUID metaformId, UUID auditLogEntryId) {
    if (!systemSettingController.inTestMode()) {
      return createForbidden(YOU_ARE_NOT_ALLOWE_TO_DELETE_LOGS);
    }

    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    fi.metatavu.metaform.server.persistence.model.AuditLogEntry auditLogEntry = auditLogEntryController.findAuditLogEntryById(auditLogEntryId);
    auditLogEntryController.deleteAuditLogEntry(auditLogEntry);
    return createNoContent();
  }

  @Override
  public Response deleteDraft(UUID metaformId, UUID draftId) {
    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    fi.metatavu.metaform.server.persistence.model.Draft draft = draftController.findDraftById(draftId);
    if (draft == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    if (!isRealmMetaformAdmin()) {
      return createForbidden("You are not allowed to delete drafts");
    }

    if (!draft.getMetaform().getId().equals(metaform.getId())) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    draftController.deleteDraft(draft);

    return null;
  }

  @Override
  public Response deleteEmailNotification(UUID metaformId, UUID emailNotificationId) {
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
  public Response deleteExportTheme(UUID exportThemeId) {
    if (!isRealmMetaformSuper()) {
      return createForbidden(YOU_ARE_NOT_ALLOWED_TO_UPDATE_THEMES);
    }

    fi.metatavu.metaform.server.persistence.model.ExportTheme theme = exportThemeController.findExportTheme(exportThemeId);
    if (theme == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    exportThemeController.deleteTheme(theme);

    return createNoContent();
  }

  @Override
  public Response deleteExportThemeFile(UUID exportThemeId, UUID exportThemeFileId) {
    if (!isRealmMetaformSuper()) {
      return createForbidden(YOU_ARE_NOT_ALLOWED_TO_UPDATE_THEMES);
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

    exportThemeController.deleteThemeFile(exportThemeFile);

    return createNoContent();
  }

  @Override
  public Response deleteMetaform(UUID metaformId) {
    if (!isRealmMetaformAdmin()) {
      return createForbidden("You are not allowed to delete Metaforms");
    }

    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    metaformController.deleteMetaform(metaform);

    return createNoContent();
  }

  @Override
  public Response deleteReply(UUID metaformId, UUID replyId, String ownerKey) {
    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    fi.metatavu.metaform.server.persistence.model.Reply reply = replyController.findReplyById(replyId);
    if (reply == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    if (!isPermittedReply(reply, ownerKey, AuthorizationScope.REPLY_EDIT)) {
      return createForbidden(NOT_ALLOWED_TO_UPDATE_REPLY_MESSAGE);
    }

    if (!reply.getMetaform().getId().equals(metaform.getId())) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    auditLogEntryController.generateAuditLog(metaform, getLoggerUserId(), reply.getId(), null, null, AuditLogEntryType.DELETE_REPLY);
    replyController.deleteReply(reply);

    return createNoContent();
  }

  @Override
  public Response export(UUID metaformId, @NotNull String format) {
    if (!isRealmMetaformAdmin()) {
      return createForbidden("You are not allowed to export Metaforms");
    }

    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    if ("XLSX".equals(format)) {
      Metaform metaformEntity = metaformTranslator.translateMetaform(metaform);
      if (metaformEntity == null) {
        return createInternalServerError("Failed to translate metaform");
      }

      List<fi.metatavu.metaform.server.persistence.model.Reply> replies = replyController.listReplies(metaform, null, null, null, null, null, false, null);
      List<Reply> replyEntities = replies.stream().map(reply -> replyTranslator.translateReply(metaformEntity, reply, null)).collect(Collectors.toList());

      UUID loggedUserId = getLoggerUserId();
      replies.forEach(reply->auditLogEntryController.generateAuditLog(metaform, loggedUserId, reply.getId(), null, null, AuditLogEntryType.EXPORT_REPLY_XLSX));

      try {
        return streamResponse(replyController.getRepliesAsXlsx(metaform, metaformEntity, replyEntities), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
      } catch (XlsxException e) {
        logger.error("Failed to export replies as XLSX", e);
        return createInternalServerError(e.getMessage());
      }
    } else {
      return createBadRequest(String.format("Unknown format %s", format));
    }
  }

  @Override
  public Response findAttachment(UUID attachmentId, String ownerKey) {
    fi.metatavu.metaform.server.persistence.model.Attachment attachment = attachmentController.findAttachmentById(attachmentId);
    if (attachment == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    if (!isPermittedAttachment(attachment, ownerKey)) {
      return createForbidden(ANONYMOUS_USERS_MESSAGE);
    }

    attachmentController.logAttachmentAccess(attachment, null, AuditLogEntryType.DOWNLOAD_REPLY_ATTACHMENT, getLoggerUserId());

    return createOk(attachmentTranslator.translateAttachment(attachment));
  }

  @Override
  public Response findAttachmentData(UUID attachmentId, String ownerKey) {
    fi.metatavu.metaform.server.persistence.model.Attachment attachment = attachmentController.findAttachmentById(attachmentId);
    if (attachment == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    if (!isPermittedAttachment(attachment, ownerKey)) {
      return createForbidden(ANONYMOUS_USERS_MESSAGE);
    }

    attachmentController.logAttachmentAccess(attachment, null, AuditLogEntryType.VIEW_REPLY_ATTACHMENT, getLoggerUserId());

    return streamResponse(attachment.getContent(), attachment.getContentType());

  }

  @Override
  public Response findDraft(UUID metaformId, UUID draftId) {
    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    fi.metatavu.metaform.server.persistence.model.Draft draft = draftController.findDraftById(draftId);
    if (draft == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    if (!draft.getMetaform().getId().equals(metaform.getId())) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    return createOk(draftTranslator.translateDraft(draft));
  }

  @Override
  public Response findEmailNotification(UUID metaformId, UUID emailNotificationId) {
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
  public Response findExportTheme(UUID exportThemeId) {
    if (!isRealmMetaformSuper()) {
      return createForbidden(YOU_ARE_NOT_ALLOWED_TO_UPDATE_THEMES);
    }

    fi.metatavu.metaform.server.persistence.model.ExportTheme exportTheme = exportThemeController.findExportTheme(exportThemeId);
    if (exportTheme == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    return createOk(exportThemeTranslator.translateExportTheme(exportTheme));
  }

  @Override
  public Response findExportThemeFile(UUID exportThemeId, UUID exportThemeFileId) {
    if (!isRealmMetaformSuper()) {
      return createForbidden(YOU_ARE_NOT_ALLOWED_TO_UPDATE_THEMES);
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
  public Response findMetaform(UUID metaformId, UUID replyId, String ownerKey) {
    UUID loggedUserId = getLoggerUserId();
    if (loggedUserId == null) {
      return createForbidden(UNAUTHORIZED);
    }

    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    if (!metaform.getAllowAnonymous() && !isRealmUser()) {
      fi.metatavu.metaform.server.persistence.model.Reply reply = replyId != null ? replyController.findReplyById(replyId) : null;
      if (reply == null || !metaform.getId().equals(reply.getMetaform().getId()) || ownerKey == null) {
        return createForbidden(ANONYMOUS_USERS_FIND_METAFORM_MESSAGE);
      }

      if (!isPermittedReply(reply, ownerKey, AuthorizationScope.REPLY_VIEW)) {
        return createForbidden(ANONYMOUS_USERS_FIND_METAFORM_MESSAGE);
      }
    }

    return createOk(metaformTranslator.translateMetaform(metaform));
  }

  @Override
  public Response findReply(UUID metaformId, UUID replyId, String ownerKey) {
    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    fi.metatavu.metaform.server.persistence.model.Reply reply = replyController.findReplyById(replyId);
    if (reply == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    if (!isPermittedReply(reply, ownerKey, AuthorizationScope.REPLY_VIEW)) {
      return createForbidden(NOT_ALLOWED_TO_VIEW_REPLY_MESSAGE);
    }

    if (!reply.getMetaform().getId().equals(metaform.getId())) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    Metaform metaformEntity = metaformTranslator.translateMetaform(metaform);

    auditLogEntryController.generateAuditLog(metaform, getLoggerUserId(), reply.getId(), null, null, AuditLogEntryType.VIEW_REPLY);
    return createOk(replyTranslator.translateReply(metaformEntity, reply, null));
  }

  @Override
  public Response listAuditLogEntries(UUID metaformId, UUID userId, UUID replyId, String createdBefore, String createdAfter) {
    if (!hasRealmRole(VIEW_AUDIT_LOGS_ROLE))
      return createForbidden(String.format("Only users with %s can access this view", VIEW_AUDIT_LOGS_ROLE));

    OffsetDateTime createdBeforeTime = parseTime(createdBefore);
    OffsetDateTime createdAfterTime = parseTime(createdAfter);

    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    List<fi.metatavu.metaform.server.persistence.model.AuditLogEntry> auditLogEntries = auditLogEntryController.listAuditLogEntries(metaform, replyId, userId, createdBeforeTime, createdAfterTime);

    List<fi.metatavu.metaform.api.spec.model.AuditLogEntry> result = auditLogEntries.stream()
      .map(entity -> auditLogEntryTranslator.translateAuditLogEntry(entity))
      .collect(Collectors.toList());

    return createOk(result);
  }

  @Override
  public Response listEmailNotifications(UUID metaformId) {
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
  public Response listExportThemeFiles(UUID exportThemeId) {
    if (!isRealmMetaformSuper()) {
      return createForbidden(YOU_ARE_NOT_ALLOWED_TO_UPDATE_THEMES);
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
  public Response listExportThemes() {
    if (!isRealmMetaformSuper()) {
      return createForbidden(YOU_ARE_NOT_ALLOWED_TO_UPDATE_THEMES);
    }

    return createOk(exportThemeController.listExportThemes().stream()
      .map(exportThemeTranslator::translateExportTheme)
      .collect(Collectors.toList()));
  }

  @Override
  public Response listMetaforms() {
    if (!isRealmUser()) {
      return createForbidden(ANONYMOUS_USERS_LIST_METAFORMS_MESSAGE);
    }

    return createOk(metaformController.listMetaforms().stream().map(entity -> metaformTranslator.translateMetaform(entity)).collect(Collectors.toList()));
  }

  @Override
  public Response listReplies(UUID metaformId, UUID userId, String createdBeforeParam, String createdAfterParam, String modifiedBeforeParam, String modifiedAfterParam, Boolean includeRevisions, List<String> fields, Integer firstResult, Integer maxResults) {
    if (firstResult != null) {
      return createNotImplemented("firstResult is not supported yet");
    }

    if (maxResults != null) {
      return createNotImplemented("maxResults is not supported yet");
    }

    OffsetDateTime createdBefore = parseTime(createdBeforeParam);
    OffsetDateTime createdAfter = parseTime(createdAfterParam);
    OffsetDateTime modifiedBefore = parseTime(modifiedBeforeParam);
    OffsetDateTime modifiedAfter = parseTime(modifiedAfterParam);

    if (!isRealmUser()) {
      return createForbidden(ANONYMOUS_USERS_MESSAGE);
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
      includeRevisions != null && includeRevisions,
      fieldFilters);

    UUID loggedUser = getLoggerUserId();
    replies.forEach(reply -> auditLogEntryController.generateAuditLog(metaform, loggedUser, reply.getId(), null, null, AuditLogEntryType.LIST_REPLY));

    List<Reply> result = getPermittedReplies(replies, AuthorizationScope.REPLY_VIEW).stream()
      .map(entity -> replyTranslator.translateReply(metaformEntity, entity, null))
      .collect(Collectors.toList());

    return createOk(result);
  }

  @Override
  public Response replyExport(UUID metaformId, UUID replyId, @NotNull String format) {
    // TODO: Permission check

    Locale locale = getLocale();
    if (!isRealmUser()) {
      return createForbidden(ANONYMOUS_USERS_MESSAGE);
    }

    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    fi.metatavu.metaform.server.persistence.model.Reply reply = replyController.findReplyById(replyId);
    if (reply == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    if (!isPermittedReply(reply, null, AuthorizationScope.REPLY_VIEW)) {
      return createForbidden(NOT_ALLOWED_TO_VIEW_REPLY_MESSAGE);
    }

    if (!reply.getMetaform().getId().equals(metaform.getId())) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    Metaform metaformEntity = metaformTranslator.translateMetaform(metaform);
    if (metaform.getExportTheme() == null) {
      return createBadRequest("Metaform does not have an export theme");
    }

    Reply replyEntity = replyTranslator.translateReply(metaformEntity, reply, null);
    Map<String, Attachment> attachmentMap = getAttachmentMap(metaformEntity, replyEntity);

    try {
      byte[] pdfData = replyController.getReplyPdf(metaform.getExportTheme().getName(), metaformEntity, replyEntity, attachmentMap, locale);
      auditLogEntryController.generateAuditLog(metaform, getLoggerUserId(), replyId, null, null, AuditLogEntryType.EXPORT_REPLY_PDF);

      return streamResponse(pdfData, "application/pdf");
    } catch (PdfRenderException e) {
      logger.error("Failed to generate PDF", e);
      return createInternalServerError(e.getMessage());
    }
  }

  @Override
  public Response updateDraft(UUID metaformId, UUID draftId, @Valid fi.metatavu.metaform.api.spec.model.Draft payload) {
    UUID loggedUserId = getLoggerUserId();
    if (loggedUserId == null) {
      return createForbidden(UNAUTHORIZED);
    }

    fi.metatavu.metaform.server.persistence.model.Metaform metaform = metaformController.findMetaformById(metaformId);
    if (metaform == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    Metaform metaformEntity = metaformTranslator.translateMetaform(metaform);
    if (BooleanUtils.isNotTrue(metaformEntity.getAllowDrafts())) {
      return createForbidden(DRAFTS_NOT_ALLOWED);
    }

    boolean anonymous = !isRealmUser();
    if (!metaform.getAllowAnonymous() && anonymous) {
      return createForbidden(ANONYMOUS_USERS_MESSAGE);
    }

    fi.metatavu.metaform.server.persistence.model.Draft draft = draftController.findDraftById(draftId);
    if (draft == null) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    if (!draft.getMetaform().getId().equals(metaform.getId())) {
      return createNotFound(NOT_FOUND_MESSAGE);
    }

    fi.metatavu.metaform.server.persistence.model.Draft updatedDraft = draftController.updateDraft(draft, payload.getData());

    return createOk(draftTranslator.translateDraft(updatedDraft));
  }

  @Override
  public Response updateEmailNotification(UUID metaformId, UUID emailNotificationId, @Valid EmailNotification payload) {
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

    try {
      emailNotificationController.updateEmailNotification(emailNotification, payload.getSubjectTemplate(), payload.getContentTemplate(), payload.getEmails(), payload.getNotifyIf());
    } catch (JsonProcessingException e) {
      return createBadRequest(e.getMessage());
    }

    return createOk(emailNotificationTranslator.translateEmailNotification(emailNotification));
  }

  @Override
  public Response updateExportTheme(UUID exportThemeId, @Valid ExportTheme payload) {
    if (!isRealmMetaformSuper()) {
      return createForbidden(YOU_ARE_NOT_ALLOWED_TO_UPDATE_THEMES);
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
        return createBadRequest(String.format(THEME_DOES_NOT_EXIST, parentId));
      }
    }

    return createOk(exportThemeTranslator.translateExportTheme(exportThemeController.updateExportTheme(theme, payload.getLocales(), parent, payload.getName(), loggedUserId)));
  }

  @Override
  public Response updateExportThemeFile(UUID exportThemeId, UUID exportThemeFileId, @Valid ExportThemeFile payload) {
    if (!isRealmMetaformSuper()) {
      return createForbidden(YOU_ARE_NOT_ALLOWED_TO_UPDATE_THEMES);
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

  @Override
  public Response updateMetaform(UUID metaformId, @Valid Metaform payload) {
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

    Boolean allowAnonymous = payload.getAllowAnonymous();
    if (allowAnonymous == null) {
      allowAnonymous = metaform.getAllowAnonymous();
    }

    Response validationResponse = validateMetaform(payload);
    if (validationResponse != null) {
      return validationResponse;
    }

    fi.metatavu.metaform.server.persistence.model.ExportTheme exportTheme = null;
    if (payload.getExportThemeId() != null) {
      exportTheme = exportThemeController.findExportTheme(payload.getExportThemeId());
      if (exportTheme == null) {
        return createBadRequest("Invalid exportThemeId");
      }
    }

    updateMetaformPermissionGroups(metaform.getSlug(), payload);

    return createOk(metaformTranslator.translateMetaform(metaformController.updateMetaform(metaform, exportTheme, data, allowAnonymous)));
  }

  @Override
  public Response updateReply(UUID metaformId, UUID replyId, @Valid Reply payload, String ownerKey) {
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

    if (!isPermittedReply(reply, ownerKey, AuthorizationScope.REPLY_EDIT)) {
      return createForbidden(NOT_ALLOWED_TO_UPDATE_REPLY_MESSAGE);
    }

    Metaform metaformEntity = metaformTranslator.translateMetaform(metaform);

    EnumMap<AuthorizationScope, List<String>> newPermissionGroups = new EnumMap<>(AuthorizationScope.class);
    Arrays.stream(AuthorizationScope.values()).forEach(scope -> newPermissionGroups.put(scope, new ArrayList<>()));

    List<String> fieldNames = new ArrayList<>(replyController.listFieldNames(reply));
    Map<String, Object> data = payload.getData();
    Map<String, MetaformField> fieldMap = fieldController.getFieldMap(metaformEntity);

    for (Map.Entry<String, Object> entry : data.entrySet()) {
      String fieldName = entry.getKey();
      MetaformField field = fieldMap.get(fieldName);
      Object fieldValue = entry.getValue();

      if (!replyController.isValidFieldValue(field, fieldValue)) {
        return createBadRequest(String.format("Invalid field value for field %s", fieldName));
      }

      replyController.setReplyField(fieldMap.get(fieldName), reply, fieldName, fieldValue);
      metaformController.addPermissionContextGroups(newPermissionGroups, metaform.getSlug(), field, fieldValue);
      fieldNames.remove(fieldName);
    }

    replyController.deleteReplyFields(reply, fieldNames);

    Reply replyEntity = replyTranslator.translateReply(metaformEntity, reply, null);

    if (metaformEntity.getScripts() != null && metaformEntity.getScripts().getAfterUpdateReply() != null) {
      setupFormRuntimeContext(metaform, metaformEntity, replyEntity);
      scriptController.runScripts(metaformEntity.getScripts().getAfterUpdateReply());
    }

    auditLogEntryController.generateAuditLog(metaform, getLoggerUserId(), reply.getId(), null, null, AuditLogEntryType.MODIFY_REPLY);
    metaformController.handleReplyPostPersist(false, metaform, reply, replyEntity, newPermissionGroups);

    return createNoContent();
  }

  @Override
  public Response ping() {
    return createOk("pong");
  }

  /**
   * Serializes Metaform into JSON
   *
   * @param metaform Metaform
   * @return serialized Metaform
   */
  protected String serializeMetaform(Metaform metaform) {
    ObjectMapper objectMapper = new ObjectMapper();

    try {
      return objectMapper.writeValueAsString(metaform);
    } catch (JsonProcessingException e) {
      logger.error("Failed to serialze metaform", e);
    }

    return null;
  }

  /**
   * Validates incoming Metaform
   *
   * @param payload metaform data
   * @return validation error or null if metaform is valid
   */
  private Response validateMetaform(Metaform payload) {
    List<String> keys = payload.getSections().stream()
      .map(MetaformSection::getFields)
      .flatMap(List::stream)
      .map(MetaformField::getName)
      .collect(Collectors.toList());

    List<String> duplicates = keys.stream()
      .filter(key -> Collections.frequency(keys, key) > 1)
      .distinct()
      .collect(Collectors.toList());

    if (!duplicates.isEmpty()) {
      return Response.status(400).entity(String.format("Duplicate field names: %s", StringUtils.join(duplicates, ','))).build();
    }

    return null;
  }

  /**
   * Updates permission groups to match metaform
   *
   * @param formSlug form slug
   * @param metaformEntity Metaform REST entity
   */
  private void updateMetaformPermissionGroups(String formSlug, Metaform metaformEntity) {
    Configuration keycloakConfiguration = KeycloakAdminUtils.getKeycloakConfiguration();
    Keycloak adminClient = KeycloakAdminUtils.getAdminClient(keycloakConfiguration);
    ClientRepresentation keycloakClient = KeycloakAdminUtils.getKeycloakClient(adminClient);

    List<String> groupNames = metaformController.getPermissionContextFields(metaformEntity).stream()
      .map(field -> field.getOptions().stream().map(option -> metaformController.getReplySecurityContextGroup(formSlug, field.getName(), option.getName())).collect(Collectors.toList()))
      .flatMap(List::stream)
      .collect(Collectors.toList());

    KeycloakAdminUtils.updatePermissionGroups(adminClient, keycloakConfiguration.getRealm(), keycloakClient, groupNames);
  }

  /**
   * Resolves reply user from payload. If user has appropriate permissions user can
   * be other than logged user, otherwise logged user is returned
   *
   * @param reply reply
   * @return reply user id
   */
  private UUID getReplyUserId(fi.metatavu.metaform.api.spec.model.Reply reply) {
    UUID userId = reply.getUserId();
    if (!isRealmMetaformAdmin() || userId == null) {
      return getLoggerUserId();
    }

    return userId;
  }

  /**
   * Returns whether given reply is permitted within given scope
   *
   * @param reply reply
   * @param ownerKey reply owner key
   * @param authorizationScope scope
   * @return whether given reply is permitted within given scope
   */
  private boolean isPermittedReply(fi.metatavu.metaform.server.persistence.model.Reply reply, String ownerKey, AuthorizationScope authorizationScope) {
    if (isRealmMetaformAdmin() || isRealmMetaformSuper()) {
      return true;
    }

    if (reply == null || reply.getResourceId() == null) {
      return false;
    }

    if (replyController.isValidOwnerKey(reply, ownerKey)) {
      return true;
    }

    if (!isRealmUser()) {
      return false;
    }

    return isPermittedResourceId(reply.getResourceId(), authorizationScope);
  }

  /**
   * Filters out replies without permission
   *
   * @param replies replies
   * @param authorizationScope scope
   * @return filtered list
   */
  private List<fi.metatavu.metaform.server.persistence.model.Reply> getPermittedReplies(List<fi.metatavu.metaform.server.persistence.model.Reply> replies, AuthorizationScope authorizationScope) {
    if (isRealmMetaformAdmin() || isRealmMetaformSuper() || hasRealmRole(VIEW_ALL_REPLIES_ROLE)) {
      return replies;
    }

    Set<UUID> resourceIds = replies.stream()
      .map(fi.metatavu.metaform.server.persistence.model.Reply::getResourceId)
      .filter(Objects::nonNull)
      .collect(Collectors.toSet());

    Set<UUID> permittedResourceIds = getPermittedResourceIds(resourceIds, authorizationScope);

    return replies.stream()
      .filter(reply -> permittedResourceIds.contains(reply.getResourceId()))
      .collect(Collectors.toList());
  }

  /**
   * Returns whether given resource id is permitted within given scope
   *
   * @param resourceId resource id
   * @param authorizationScope scope
   * @return whether given resource id is permitted within given scope
   */
  private boolean isPermittedResourceId(UUID resourceId, AuthorizationScope authorizationScope) {
    Set<UUID> permittedResourceIds = getPermittedResourceIds(Collections.singleton(resourceId), authorizationScope);
    return permittedResourceIds.size() == 1 && resourceId.equals(permittedResourceIds.iterator().next());
  }

  /**
   * Filters out resource ids without permission
   *
   * @param resourceIds resource ids
   * @param authorizationScope scope
   * @return filtered list
   */
  private Set<UUID> getPermittedResourceIds(Set<UUID> resourceIds, AuthorizationScope authorizationScope) {
    try {
      AuthorizationRequest request = new AuthorizationRequest();

      resourceIds.forEach(resourceId -> request.addPermission(resourceId.toString(), authorizationScope.getName()));

      AuthorizationResponse response = getAuthzClient().authorization(getTokenString()).authorize(request);
      TokenIntrospectionResponse irt = getAuthzClient().protection().introspectRequestingPartyToken(response.getToken());
      List<Permission> permissions = irt.getPermissions();

      return permissions.stream()
        .map(Permission::getResourceId)
        .map(UUID::fromString)
        .collect(Collectors.toSet());
    } catch (HttpResponseException e) {
      // User does not have permissions to any of the resources
    } catch (Exception e) {
      logger.error("Failed to get permissing from Keycloak", e);
    }

    return Collections.emptySet();
  }

  /**
   * Sets up runtime context for running scripts
   *
   * @param metaform Metaform JPA entity
   * @param metaformEntity Metaform REST entity
   * @param replyEntity Reply REST entity
   */
  private void setupFormRuntimeContext(fi.metatavu.metaform.server.persistence.model.Metaform metaform, fi.metatavu.metaform.api.spec.model.Metaform metaformEntity, fi.metatavu.metaform.api.spec.model.Reply replyEntity) {
    formRuntimeContext.setLoggedUserId(getLoggerUserId());
    formRuntimeContext.setMetaform(metaformEntity);
    formRuntimeContext.setReply(replyEntity);
    formRuntimeContext.setAttachmentMap(getAttachmentMap(metaformEntity, replyEntity));
    formRuntimeContext.setLocale(getLocale());

    if (metaform.getExportTheme() != null) {
      formRuntimeContext.setExportThemeName(metaform.getExportTheme().getName());
    }
  }

  /**
   * Returns a map of reply attachments where map key is the attachment id and value the rest representation of the attachment
   *
   * @param metaformEntity Metaform REST model
   * @param replyEntity Reply REST model
   * @return reply map
   */
  private Map<String, Attachment> getAttachmentMap(fi.metatavu.metaform.api.spec.model.Metaform metaformEntity, fi.metatavu.metaform.api.spec.model.Reply replyEntity) {
    return fieldController.getFieldNamesByType(metaformEntity, MetaformFieldType.FILES).stream()
      .map(fieldName -> {
        @SuppressWarnings("unchecked")
        List<UUID> attachmentIds = (List<UUID>) replyEntity.getData().get(fieldName);
        if (attachmentIds != null) {
          return attachmentIds.stream()
            .map(attachmentController::findAttachmentById)
            .map(attachmentTranslator::translateAttachment)
            .collect(Collectors.toList());
        }

        return null;
      })
      .filter(Objects::nonNull)
      .flatMap(List::stream)
      .filter(Objects::nonNull)
      .collect(Collectors.toMap(attachment -> attachment.getId().toString(), attachment -> attachment));
  }

  /**
   * Returns whether given attachment is permitted
   *
   * @param attachment attachment
   * @param ownerKey reply owner key
   * @return whether given attachment is permitted
   */
  private boolean isPermittedAttachment(fi.metatavu.metaform.server.persistence.model.Attachment attachment, String ownerKey) {
    if (isRealmMetaformAdmin() || isRealmMetaformSuper() || isRealmUser()) {
      return true;
    }

    fi.metatavu.metaform.server.persistence.model.Reply reply = attachmentController.findReplyByAttachment(attachment);
    if (reply == null || reply.getResourceId() == null) {
      return false;
    }

    return replyController.isValidOwnerKey(reply, ownerKey);
  }
}
