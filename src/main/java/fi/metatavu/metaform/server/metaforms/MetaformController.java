package fi.metatavu.metaform.server.metaforms;

import java.util.*;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.metaform.api.spec.model.*;
import fi.metatavu.metaform.server.attachments.AttachmentController;
import fi.metatavu.metaform.server.keycloak.AuthorizationScope;
import fi.metatavu.metaform.server.keycloak.KeycloakAdminUtils;
import fi.metatavu.metaform.server.keycloak.KeycloakConfigProvider;
import fi.metatavu.metaform.server.keycloak.ResourceType;
import fi.metatavu.metaform.server.logentry.AuditLogEntryController;
import fi.metatavu.metaform.server.notifications.EmailNotificationController;
import fi.metatavu.metaform.server.persistence.dao.AuditLogEntryDAO;
import fi.metatavu.metaform.server.persistence.model.AuditLogEntry;
import fi.metatavu.metaform.server.script.FormRuntimeContext;
import org.apache.commons.lang3.StringUtils;

import com.github.slugify.Slugify;

import fi.metatavu.metaform.server.persistence.dao.MetaformDAO;
import fi.metatavu.metaform.server.persistence.dao.ReplyDAO;
import fi.metatavu.metaform.server.persistence.model.ExportTheme;
import fi.metatavu.metaform.server.persistence.model.Metaform;
import fi.metatavu.metaform.server.persistence.model.Reply;
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

/**
 * Metaform controller
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class MetaformController {

  private static final String USER_POLICY_NAME = "user";
  private static final String OWNER_POLICY_NAME = "owner";
  private static final String METAFORM_ADMIN_POLICY_NAME = "metaform-admin";
  private static final String REPLY_RESOURCE_URI_TEMPLATE = "/v1/metaforms/%s/replies/%s";
  private static final String REPLY_RESOURCE_NAME_TEMPLATE = "reply-%s";
  private static final String REPLY_PERMISSION_NAME_TEMPLATE = "permission-%s-%s";
  private static final String REPLY_GROUP_NAME_TEMPLATE = "%s:%s:%s";
  private static final List<AuthorizationScope> REPLY_SCOPES = Arrays.asList(AuthorizationScope.REPLY_VIEW, AuthorizationScope.REPLY_EDIT, AuthorizationScope.REPLY_NOTIFY);


  @Inject
  private AttachmentController attachmentController;

  @Inject
  private FieldController fieldController;

  @Inject
  private FormRuntimeContext formRuntimeContext;

  @Inject
  private EmailNotificationController emailNotificationController;

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
    auditLogEntryDAO.listByMetaform(metaform).stream()
            .forEach(auditLogEntryController::deleteAuditLogEntry);
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

  /**
   * Adds field permission context groups into appropriate lists
   *
   * @param formSlug form slug
   * @param permissionGroups target map
   * @param field field
   * @param fieldValue field value
   */
  public void addPermissionContextGroups(EnumMap<AuthorizationScope, List<String>> permissionGroups, String formSlug, MetaformField field, Object fieldValue) {
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
   * Creates reply security context group name
   *
   * @param formSlug form slug
   * @param fieldName field name
   * @param fieldValue field value
   * @return reply security context group name
   */
  public String getReplySecurityContextGroup(String formSlug, String fieldName, String fieldValue) {
    return String.format(REPLY_GROUP_NAME_TEMPLATE, formSlug, fieldName, fieldValue);
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
  public void handleReplyPostPersist(boolean replyCreated, fi.metatavu.metaform.server.persistence.model.Metaform metaform, fi.metatavu.metaform.server.persistence.model.Reply reply, fi.metatavu.metaform.api.spec.model.Reply replyEntity, EnumMap<AuthorizationScope, List<String>> newPermissionGroups) {
    Configuration keycloakConfiguration = KeycloakAdminUtils.getKeycloakConfiguration();
    Keycloak adminClient = KeycloakAdminUtils.getAdminClient(keycloakConfiguration);
    ClientRepresentation keycloakClient = KeycloakAdminUtils.getKeycloakClient(adminClient);

    UUID resourceId = reply.getResourceId();
    String resourceName = replyController.getReplyResourceName(reply);

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
  private void sendReplyEmailNotification(Keycloak keycloak, boolean replyCreated, fi.metatavu.metaform.server.persistence.model.notifications.EmailNotification emailNotification, fi.metatavu.metaform.api.spec.model.Reply replyEntity, Set<UUID> notifyUserIds) {
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
   * Returns permission context fields from Metaform REST entity
   *
   * @param metaformEntity metaform REST entity
   * @return permission context fields
   */
  public List<MetaformField> getPermissionContextFields(fi.metatavu.metaform.api.spec.model.Metaform metaformEntity) {
    return metaformEntity.getSections().stream()
      .map(MetaformSection::getFields)
      .flatMap(List::stream)
      .filter(field -> field.getPermissionContexts() != null)
      .filter(field -> field.getPermissionContexts().getEditGroup() || field.getPermissionContexts().getViewGroup() || field.getPermissionContexts().getNotifyGroup())
      .collect(Collectors.toList());
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
        replyController.getReplyResourceName(reply),
        replyController.getReplyResourceUri(reply),
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
}
