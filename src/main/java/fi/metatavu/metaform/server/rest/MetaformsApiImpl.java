package fi.metatavu.metaform.server.rest;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import fi.metatavu.metaform.client.model.AuditLogEntryType;
import fi.metatavu.metaform.server.logentry.AuditLogEntryController;
import fi.metatavu.metaform.server.persistence.model.AuditLogEntry;
import fi.metatavu.metaform.server.rest.translate.*;
import fi.metatavu.metaform.server.settings.SystemSettingController;
import org.apache.commons.lang3.BooleanUtils;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.authorization.client.Configuration;
import org.keycloak.authorization.client.representation.TokenIntrospectionResponse;
import org.keycloak.authorization.client.util.HttpResponseException;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.authorization.AuthorizationRequest;
import org.keycloak.representations.idm.authorization.AuthorizationResponse;
import org.keycloak.representations.idm.authorization.DecisionStrategy;
import org.keycloak.representations.idm.authorization.Permission;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import _fi.metatavu.metaform.server.rest.api.MetaformsApi;
import fi.metatavu.metaform.server.attachments.AttachmentController;
import fi.metatavu.metaform.server.crypto.CryptoController;
import fi.metatavu.metaform.server.drafts.DraftController;
import fi.metatavu.metaform.server.exporttheme.ExportThemeController;
import fi.metatavu.metaform.server.keycloak.AuthorizationScope;
import fi.metatavu.metaform.server.keycloak.KeycloakAdminUtils;
import fi.metatavu.metaform.server.keycloak.KeycloakConfigProvider;
import fi.metatavu.metaform.server.keycloak.ResourceType;
import fi.metatavu.metaform.server.metaforms.FieldController;
import fi.metatavu.metaform.server.metaforms.FieldFilters;
import fi.metatavu.metaform.server.metaforms.MetaformController;
import fi.metatavu.metaform.server.metaforms.ReplyController;
import fi.metatavu.metaform.server.notifications.EmailNotificationController;
import fi.metatavu.metaform.server.pdf.PdfRenderException;
import fi.metatavu.metaform.server.rest.model.EmailNotification;
import fi.metatavu.metaform.server.rest.model.Metaform;
import fi.metatavu.metaform.server.rest.model.MetaformField;
import fi.metatavu.metaform.server.rest.model.MetaformFieldPermissionContexts;
import fi.metatavu.metaform.server.rest.model.MetaformFieldType;
import fi.metatavu.metaform.server.rest.model.MetaformSection;
import fi.metatavu.metaform.server.rest.model.Reply;
import fi.metatavu.metaform.server.rest.model.Draft;
import fi.metatavu.metaform.server.rest.translate.AttachmentTranslator;
import fi.metatavu.metaform.server.rest.translate.EmailNotificationTranslator;
import fi.metatavu.metaform.server.rest.translate.MetaformTranslator;
import fi.metatavu.metaform.server.rest.translate.ReplyTranslator;
import fi.metatavu.metaform.server.script.FormRuntimeContext;
import fi.metatavu.metaform.server.script.ScriptController;
import fi.metatavu.metaform.server.xlsx.XlsxException;
import fi.metatavu.metaform.server.rest.translate.DraftTranslator;

/**
 * Realms REST Service implementation
 * 
 * @author Antti Leppä
 */
@RequestScoped
@Stateful
public class MetaformsApiImpl extends AbstractApi implements MetaformsApi {
  
  private static final List<AuthorizationScope> REPLY_SCOPES = Arrays.asList(AuthorizationScope.REPLY_VIEW, AuthorizationScope.REPLY_EDIT, AuthorizationScope.REPLY_NOTIFY);
  private static final String USER_POLICY_NAME = "user";
  private static final String OWNER_POLICY_NAME = "owner";
  private static final String METAFORM_ADMIN_POLICY_NAME = "metaform-admin";
  private static final String REPLY_RESOURCE_URI_TEMPLATE = "/v1/metaforms/%s/replies/%s";
  private static final String REPLY_RESOURCE_NAME_TEMPLATE = "reply-%s";
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
  private ExportThemeController exportThemeController;

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
  @SuppressWarnings ("squid:S3776")
  public Response createReply(UUID metaformId, Reply payload, Boolean updateExisting, String replyModeParam) {
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

    fi.metatavu.metaform.server.persistence.model.Reply reply = createReplyResolveReply(replyMode, metaform, anonymous, userId, privateKey);
    Map<String, Object> data = payload.getData();
    if (data == null) {
      logger.warn("Received a reply with null data");
    } else {
      Map<String, MetaformField> fieldMap = fieldController.getFieldMap(metaformEntity);
      for (Entry<String, Object> entry : data.entrySet()) {
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
          addPermissionContextGroups(permissionGroups, metaform.getSlug(), field, fieldValue);
        }
      }
    } 

    Reply replyEntity = replyTranslator.translateReply(metaformEntity, reply, publicKey);

    if (metaformEntity.getScripts() != null && metaformEntity.getScripts().getAfterCreateReply() != null) {
      setupFormRuntimeContext(metaform, metaformEntity, replyEntity);
      scriptController.runScripts(metaformEntity.getScripts().getAfterCreateReply());
    }

    handleReplyPostPersist(true, metaform, reply, replyEntity, permissionGroups);

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

    AuditLogEntry auditLogEntry = auditLogEntryController.findAuditLogEntryById(auditLogEntryId);
    auditLogEntryController.deleteAuditLogEntry(auditLogEntry);
    return createNoContent();
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
      throw new NotFoundException(NOT_FOUND_MESSAGE);
    }

    List<AuditLogEntry> auditLogEntries = auditLogEntryController.listAuditLogEntries(metaform, replyId, userId, createdBeforeTime, createdAfterTime);

    List<fi.metatavu.metaform.server.rest.model.AuditLogEntry> result = auditLogEntries.stream()
            .map(entity -> auditLogEntryTranslator.translateAuditLogEntry(entity))
            .collect(Collectors.toList());

    return createOk(result);
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

  public Response updateReply(UUID metaformId, UUID replyId, Reply payload, String ownerKey) {
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

    for (Entry<String, Object> entry : data.entrySet()) {
      String fieldName = entry.getKey();
      MetaformField field = fieldMap.get(fieldName);
      Object fieldValue = entry.getValue();
      
      if (!replyController.isValidFieldValue(field, fieldValue)) {
        return createBadRequest(String.format("Invalid field value for field %s", fieldName));
      }
      
      replyController.setReplyField(fieldMap.get(fieldName), reply, fieldName, fieldValue);
      addPermissionContextGroups(newPermissionGroups, metaform.getSlug(), field, fieldValue);
      fieldNames.remove(fieldName);
    }

    replyController.deleteReplyFields(reply, fieldNames);

    Reply replyEntity = replyTranslator.translateReply(metaformEntity, reply, null);

    if (metaformEntity.getScripts() != null && metaformEntity.getScripts().getAfterUpdateReply() != null) {
      setupFormRuntimeContext(metaform, metaformEntity, replyEntity);
      scriptController.runScripts(metaformEntity.getScripts().getAfterUpdateReply());
    }

    auditLogEntryController.generateAuditLog(metaform, getLoggerUserId(), reply.getId(), null, null, AuditLogEntryType.MODIFY_REPLY);
    handleReplyPostPersist(false, metaform, reply, replyEntity, newPermissionGroups);
    
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
  public Response replyExport(UUID metaformId, UUID replyId, String format) {
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
    Map<String, fi.metatavu.metaform.server.rest.model.Attachment> attachmentMap = getAttachmentMap(metaformEntity, replyEntity);
    
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
  @SuppressWarnings ("squid:S3776")
  public Response createDraft(UUID metaformId, Draft payload) {
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
    Draft draftEntity = draftTranslator.translateDraft(draft);

    return createOk(draftEntity);
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
  @SuppressWarnings ("squid:S3776")
  public Response updateDraft(UUID metaformId, UUID draftId, Draft payload) {
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
  public Response createMetaform(Metaform payload) {
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
  public Response listMetaforms() {
    // TODO: Permission check
    if (!isRealmUser()) {
      return createForbidden(ANONYMOUS_USERS_LIST_METAFORMS_MESSAGE);
    }

    return createOk(metaformController.listMetaforms().stream().map(entity -> metaformTranslator.translateMetaform(entity)).collect(Collectors.toList()));
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
  public Response updateMetaform(UUID metaformId, Metaform payload) {
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
    
    // TODO: Permission check
    
    return createOk(metaformTranslator.translateMetaform(metaformController.updateMetaform(metaform, exportTheme, data, allowAnonymous)));
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

    // TODO: Permission check
    
    metaformController.deleteMetaform(metaform);
    
    return createNoContent();
  }

  public Response export(UUID metaformId, String format) {
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
  
  /**
   * Sets up runtime context for running scripts
   * 
   * @param metaform Metaform JPA entity
   * @param metaformEntity Metaform REST entity
   * @param replyEntity Reply REST entity
   */
  private void setupFormRuntimeContext(fi.metatavu.metaform.server.persistence.model.Metaform metaform, Metaform metaformEntity, Reply replyEntity) {
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
   * Handles reply post persist tasks. Tasks include adding to user groups permissions and notifying users about the reply
   * 
   * @param replyCreated whether the reply was just created
   * @param metaform metaform
   * @param reply reply
   * @param replyEntity reply entity
   * @param newPermissionGroups added permission groups
   */
  private void handleReplyPostPersist(boolean replyCreated, fi.metatavu.metaform.server.persistence.model.Metaform metaform, fi.metatavu.metaform.server.persistence.model.Reply reply, Reply replyEntity, EnumMap<AuthorizationScope, List<String>> newPermissionGroups) {
    Configuration keycloakConfiguration = KeycloakAdminUtils.getKeycloakConfiguration();
    Keycloak adminClient = KeycloakAdminUtils.getAdminClient(keycloakConfiguration);
    ClientRepresentation keycloakClient = KeycloakAdminUtils.getKeycloakClient(adminClient);
    
    UUID resourceId = reply.getResourceId();
    String resourceName = getReplyResourceName(reply);
    
    Set<UUID> notifiedUserIds = replyCreated ? Collections.emptySet() : KeycloakAdminUtils.getResourcePermittedUsers(adminClient, keycloakClient, resourceId, resourceName, Arrays.asList(AuthorizationScope.REPLY_NOTIFY));
    
    resourceId = updateReplyPermissions(adminClient, keycloakClient, reply, newPermissionGroups);
    
    Set<UUID> notifyUserIds = KeycloakAdminUtils.getResourcePermittedUsers(adminClient, keycloakClient, resourceId, resourceName, Arrays.asList(AuthorizationScope.REPLY_NOTIFY)).stream()
      .filter(notifyUserId -> !notifiedUserIds.contains(notifyUserId))
      .collect(Collectors.toSet());

    emailNotificationController.listEmailNotificationByMetaform(metaform).forEach(emailNotification -> sendReplyEmailNotification(adminClient, replyCreated, emailNotification, replyEntity, notifyUserIds));
  }
  
  /**
   * Sends reply email notifications
   * 
   * @param keycloak Keycloak admin client
   * @param replyCreated whether the reply was just created
   * @param emailNotification email notification
   * @param replyEntity reply REST entity
   * @param notifyUserIds notify user ids
   */
  private void sendReplyEmailNotification(Keycloak keycloak, boolean replyCreated, fi.metatavu.metaform.server.persistence.model.notifications.EmailNotification emailNotification, Reply replyEntity, Set<UUID> notifyUserIds) {
    if (!emailNotificationController.evaluateEmailNotificationNotifyIf(emailNotification, replyEntity)) {
      return;
    }
    
    List<String> directEmails = replyCreated ? emailNotificationController.getEmailNotificationEmails(emailNotification) : Collections.emptyList();
    String realmName = KeycloakConfigProvider.getConfig().getRealm();
    UsersResource usersResource = keycloak.realm(realmName).users();
    
    List<String> groupEmails = notifyUserIds.stream()
      .map(UUID::toString)
      .map(usersResource::get)
      .map(UserResource::toRepresentation)
      .filter(Objects::nonNull)
      .map(UserRepresentation::getEmail)
      .collect(Collectors.toList());
    
    Set<String> emails = new HashSet<>(directEmails);
    emails.addAll(groupEmails);
    
    emailNotificationController.sendEmailNotification(emailNotification, replyEntity, emails.stream().filter(StringUtils::isNotEmpty).collect(Collectors.toSet()));
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
  private fi.metatavu.metaform.server.persistence.model.Reply createReplyResolveReply(ReplyMode replyMode, fi.metatavu.metaform.server.persistence.model.Metaform metaform, boolean anonymous, UUID userId, PrivateKey privateKey) {
    fi.metatavu.metaform.server.persistence.model.Reply reply;
    
    if (anonymous || replyMode == ReplyMode.CUMULATIVE) {
      reply = replyController.createReply(userId, metaform, privateKey);
    } else {
      reply = replyController.findActiveReplyByMetaformAndUserId(metaform, userId);
      if (reply == null) {
        reply = replyController.createReply(userId, metaform, privateKey);
      } else {
        if (replyMode == ReplyMode.REVISION) {
          // If there is already an existing reply but we are not updating it
          // We need to change the existing reply into a revision and create new reply
          replyController.convertToRevision(reply);
          reply = replyController.createReply(userId, metaform, privateKey);
        }
      }
    }
    
    return reply;
  }

  @Override
  public Response createEmailNotification(UUID metaformId, EmailNotification payload) {
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
  public Response updateEmailNotification(UUID metaformId, UUID emailNotificationId, EmailNotification payload) {
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

  /**
   * Returns permission context fields from Metaform REST entity
   * 
   * @param metaformEntity metaform REST entity
   * @return permission context fields
   */
  private List<MetaformField> getPermissionContextFields(Metaform metaformEntity) {
    return metaformEntity.getSections().stream()
      .map(MetaformSection::getFields)
      .flatMap(List::stream)
      .filter(field -> field.getPermissionContexts() != null)
      .filter(field -> field.getPermissionContexts().getEditGroup() || field.getPermissionContexts().getViewGroup() || field.getPermissionContexts().getNotifyGroup())
      .collect(Collectors.toList());
  }

  /**
   * Adds field permission context groups into appropriate lists
   * 
   * @param formSlug form slug
   * @param permissionGroups target map
   * @param field field
   * @param fieldValue field value
   */
  private void addPermissionContextGroups(EnumMap<AuthorizationScope, List<String>> permissionGroups, String formSlug, MetaformField field, Object fieldValue) {
    MetaformFieldPermissionContexts permissionContexts = field.getPermissionContexts();
    
    if (permissionContexts != null && fieldValue instanceof String) {
      String fieldName = field.getName();
      String permissionGroupName = getReplySecurityContextGroup(formSlug, fieldName, (String) fieldValue);
      
      if (permissionContexts.getEditGroup()) {
        permissionGroups.get(AuthorizationScope.REPLY_EDIT).add(permissionGroupName); 
      }
      
      if (permissionContexts.getViewGroup()) {
        permissionGroups.get(AuthorizationScope.REPLY_VIEW).add(permissionGroupName); 
      }
      
      if (permissionContexts.getNotifyGroup()) {
        permissionGroups.get(AuthorizationScope.REPLY_NOTIFY).add(permissionGroupName); 
      }
    }
  }
  
  /**
   * Returns groups permission name for a reply
   * 
   * @param reply reply
   * @param name permission name
   * @return resource name
   */
  private String getReplyPermissionName(fi.metatavu.metaform.server.persistence.model.Reply reply, String name) {
    if (reply == null) {
      return null;
    }
    
    return String.format(REPLY_PERMISSION_NAME_TEMPLATE, reply.getId(), name);
  }

  /**
   * Creates reply security context group name
   * 
   * @param formSlug form slug
   * @param fieldName field name
   * @param fieldValue field value
   * @return reply security context group name
   */
  private String getReplySecurityContextGroup(String formSlug, String fieldName, String fieldValue) {
    return String.format(REPLY_GROUP_NAME_TEMPLATE, formSlug, fieldName, fieldValue);
  }
  
  /**
   * Updates reply permissions
   * 
   * @param keycloak keycloak
   * @param keycloakClient keycloakClient 
   * @param permissionGroups permissionGroups
   * @return resource id
   */
  private UUID updateReplyPermissions(Keycloak keycloak, ClientRepresentation keycloakClient, fi.metatavu.metaform.server.persistence.model.Reply reply, EnumMap<AuthorizationScope, List<String>> permissionGroups) {
    fi.metatavu.metaform.server.persistence.model.Metaform metaform = reply.getMetaform();
    
    UUID resourceId = reply.getResourceId();
    
    if (resourceId == null) {
      resourceId = KeycloakAdminUtils.createProtectedResource(keycloak,
        keycloakClient,
        reply.getUserId(), 
        getReplyResourceName(reply), 
        getReplyResourceUri(reply), 
        ResourceType.REPLY.getUrn(), 
        REPLY_SCOPES);
      
      replyController.updateResourceId(reply, resourceId);
    }
    
    Set<UUID> commonPolicyIds = KeycloakAdminUtils.getPolicyIdsByNames(keycloak, keycloakClient, Arrays.asList(METAFORM_ADMIN_POLICY_NAME, OWNER_POLICY_NAME));

    for (AuthorizationScope scope : AuthorizationScope.values()) {
      List<String> groupNames = permissionGroups.get(scope);
      Set<UUID> groupPolicyIds = KeycloakAdminUtils.getPolicyIdsByNames(keycloak, keycloakClient, groupNames);
      
      HashSet<UUID> policyIds = new HashSet<>(groupPolicyIds);
      policyIds.addAll(commonPolicyIds);

      assert resourceId != null;
      KeycloakAdminUtils.upsertScopePermission(keycloak, keycloakClient, resourceId, Collections.singleton(scope), getReplyPermissionName(reply, scope.getName().toLowerCase()), DecisionStrategy.AFFIRMATIVE, policyIds);
    }
    
    Set<UUID> userPolicyIds = KeycloakAdminUtils.getPolicyIdsByNames(keycloak, keycloakClient, Arrays.asList(USER_POLICY_NAME));
    
    if (metaform.getAllowAnonymous()) {
      assert resourceId != null;
      KeycloakAdminUtils.upsertScopePermission(keycloak, keycloakClient, resourceId, Collections.singleton(AuthorizationScope.REPLY_VIEW), "require-user", DecisionStrategy.AFFIRMATIVE, userPolicyIds);
    }
    
    return resourceId;
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
    
    List<String> groupNames = getPermissionContextFields(metaformEntity).stream()
      .map(field -> field.getOptions().stream().map(option -> getReplySecurityContextGroup(formSlug, field.getName(), option.getName())).collect(Collectors.toList()))
      .flatMap(List::stream)
      .collect(Collectors.toList());
    
    KeycloakAdminUtils.updatePermissionGroups(adminClient, keycloakConfiguration.getRealm(), keycloakClient, groupNames);
  }
  
  /**
   * Returns resource name for a reply
   * 
   * @param reply reply
   * @return resource name
   */
  private String getReplyResourceName(fi.metatavu.metaform.server.persistence.model.Reply reply) {
    if (reply == null) {
      return null;
    }
    
    return String.format(REPLY_RESOURCE_NAME_TEMPLATE, reply.getId());
  }
  
  /**
   * Returns resource URI for reply
   * 
   * @param reply reply
   * @return resource URI
   */
  private String getReplyResourceUri(fi.metatavu.metaform.server.persistence.model.Reply reply) {
    if (reply == null) {
      return null;
    }
    
    fi.metatavu.metaform.server.persistence.model.Metaform metaform = reply.getMetaform();
    return getReplyResourceUri(metaform.getId(), reply.getId());
  }
  
  /**
   * Returns resource URI for reply
   * 
   * @param metaformId Metaform id
   * @param replyId reply id
   * @return resource URI
   */
  private String getReplyResourceUri(UUID metaformId, UUID replyId) {
    return String.format(REPLY_RESOURCE_URI_TEMPLATE, metaformId, replyId);
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

  /**
   * Returns a map of reply attachments where map key is the attachment id and value the rest representation of the attachment
   * 
   * @param metaformEntity Metaform REST model
   * @param replyEntity Reply REST model
   * @return reply map
   */
  private Map<String, fi.metatavu.metaform.server.rest.model.Attachment> getAttachmentMap(Metaform metaformEntity, Reply replyEntity) {
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
   * Serializes Metaform into JSON
   * 
   * @param metaform Metaform
   * @return serialized Metaform
   */
  private String serializeMetaform(Metaform metaform) {
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

}
