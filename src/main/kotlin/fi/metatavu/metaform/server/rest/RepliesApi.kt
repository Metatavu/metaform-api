package fi.metatavu.metaform.server.rest

import fi.metatavu.metaform.api.spec.model.*
import fi.metatavu.metaform.server.controllers.*
import fi.metatavu.metaform.server.keycloak.AuthorizationScope
import fi.metatavu.metaform.server.controllers.MetaformController
import fi.metatavu.metaform.server.exceptions.AuthzException
import fi.metatavu.metaform.server.exceptions.MalformedMetaformJsonException
import fi.metatavu.metaform.server.exceptions.PdfRenderException
import fi.metatavu.metaform.server.exceptions.XlsxException
import fi.metatavu.metaform.server.permissions.GroupMemberPermission
import fi.metatavu.metaform.server.permissions.PermissionController
import fi.metatavu.metaform.server.persistence.model.Metaform
import fi.metatavu.metaform.server.rest.translate.AttachmentTranslator
import fi.metatavu.metaform.server.rest.translate.MetaformTranslator
import fi.metatavu.metaform.server.rest.translate.ReplyTranslator
import fi.metatavu.metaform.server.script.FormRuntimeContext
import org.apache.commons.lang3.BooleanUtils
import org.slf4j.Logger
import java.security.PrivateKey
import java.security.PublicKey
import java.util.*
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.core.Response

@RequestScoped
@Transactional
@Suppress ("UNUSED")
class RepliesApi: fi.metatavu.metaform.api.spec.RepliesApi, AbstractApi() {

  @Inject
  lateinit var logger: Logger

  @Inject
  lateinit var fieldController: FieldController

  @Inject
  lateinit var metaformController: MetaformController

  @Inject
  lateinit var cryptoController: CryptoController

  @Inject
  lateinit var permissionController: PermissionController

  @Inject
  lateinit var metaformTranslator: MetaformTranslator

  @Inject
  lateinit var replyTranslator: ReplyTranslator

  @Inject
  lateinit var auditLogEntryController: AuditLogEntryController

  @Inject
  lateinit var scriptsController: ScriptsController

  @Inject
  lateinit var formRuntimeContext: FormRuntimeContext

  @Inject
  lateinit var attachmentController: AttachmentController

  @Inject
  lateinit var attachmentTranslator: AttachmentTranslator

  /**
   * Create reply
   *
   * @param metaformId Metaform id
   * @param reply Reply to create
   * @param updateExisting If true, existing reply will be updated
   * @param replyModeParam Reply mode
   * @return Created reply
   */
  override fun createReply(
    metaformId: UUID,
    reply: Reply,
    updateExisting: Boolean?,
    replyModeParam: String?
  ): Response {
    val userId = loggedUserId ?: return createForbidden(UNAUTHORIZED)

    val metaform: Metaform = metaformController.findMetaformById(metaformId)
            ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    if ((metaform.visibility != MetaformVisibility.PUBLIC || metaform.allowAnonymous != true) && isAnonymous) {
      return createForbidden(ANONYMOUS_USERS_METAFORM_MESSAGE)
    }

    val replyUserId = if (!isMetatavuAdmin || !isRealmSystemAdmin || reply.userId == null) loggedUserId!! else reply.userId
    var replyMode = try {
      replyModeParam?.let { ReplyMode.valueOf(it) }
    } catch (ex: IllegalArgumentException) {
      return createBadRequest(createInvalidMessage(REPLY_MODE))
    }

    if (updateExisting != null) {
      replyMode = if (updateExisting) ReplyMode.UPDATE else ReplyMode.REVISION
    }

    if (replyMode == null) {
      replyMode = ReplyMode.UPDATE
    }

    val metaformEntity = try {
      metaformTranslator.translate(metaform)
    } catch (e: MalformedMetaformJsonException) {
      return createInternalServerError(e.message)
    }

    var privateKey: PrivateKey? = null
    var publicKey: PublicKey? = null

    if (BooleanUtils.isTrue(metaformEntity.allowReplyOwnerKeys)) {
      val keyPair = cryptoController.generateRsaKeyPair()
      privateKey = keyPair?.private
      publicKey = keyPair?.public
    }

    // TODO: Support multiple

    val createdReply: fi.metatavu.metaform.server.persistence.model.Reply = replyController.createReplyResolveReply(
      replyMode,
      metaform,
      isAnonymous,
      replyUserId,
      privateKey,
      userId
    )

    val replyData = reply.data?.filter { (fieldName, fieldValue) ->
      @Suppress("SENSELESS_COMPARISON")
      fieldName != null && fieldValue != null
    } ?: return createBadRequest("Received a reply with null data")

    val fieldMap: Map<String, MetaformField> = fieldController.getFieldMap(metaformEntity)

    replyData.forEach { (fieldName, fieldValue) ->
      val field = fieldMap[fieldName] ?: return createBadRequest(String.format("Invalid field %s", fieldName))
      if (!replyController.isValidFieldValue(field, fieldValue)) {
        return createBadRequest(String.format("Invalid field value for field %s", fieldName))
      }

      replyController.setReplyField(field, createdReply, fieldName, fieldValue)
    }

    val replyEntity = replyTranslator.translate(metaformEntity, createdReply, publicKey)

    try {
      val groupMemberPermissions = getGroupPermissions(
        metaformEntity = metaformEntity,
        replyData = replyData,
        fieldMap = fieldMap
      )

      metaformController.handleReplyPostPersist(
        replyCreated = true,
        metaform = metaform,
        reply = createdReply,
        replyEntity = replyEntity,
        loggedUserId = userId,
        groupMemberPermissions = groupMemberPermissions
      )

    } catch (e: AuthzException) {
      return createInternalServerError(e.message!!)
    }

    auditLogEntryController.generateAuditLog(
      metaform = metaform,
      userId = replyUserId,
      replyId = createdReply.id!!,
      attachmentId = null,
      action = null,
      type = AuditLogEntryType.CREATE_REPLY
    )

    replyController.triggerReplyCreatedEvent(reply = createdReply)

    return createOk(replyEntity)
  }

  /**
   * Sets up runtime context for running scripts
   *
   * @param metaform Metaform JPA entity
   * @param metaformEntity Metaform REST entity
   * @param replyEntity Reply REST entity
   */
  private fun setupFormRuntimeContext(userId: UUID, metaform: Metaform, metaformEntity: fi.metatavu.metaform.api.spec.model.Metaform, replyEntity: Reply) {
    formRuntimeContext.loggedUserId = userId
    formRuntimeContext.metaform = metaformEntity
    formRuntimeContext.reply = replyEntity
    formRuntimeContext.attachmentMap = getAttachmentMap(metaformEntity, replyEntity)
    formRuntimeContext.locale = locale
    if (metaform.exportTheme != null) {
      formRuntimeContext.exportThemeName = metaform.exportTheme!!.name
    }
  }

  /**
   * Returns a map of reply attachments where map key is the attachment id and value the rest representation of the attachment
   *
   * @param metaformEntity Metaform REST model
   * @param replyEntity Reply REST model
   * @return reply map
   */
  private fun getAttachmentMap(metaformEntity: fi.metatavu.metaform.api.spec.model.Metaform, replyEntity: Reply): Map<String, Attachment> {
    return fieldController.getFieldNamesByType(metaformEntity, MetaformFieldType.FILES)
            .map { fieldName ->
              val attachmentIds = replyEntity.data?.get(fieldName) as List<UUID>?
                attachmentIds
                  ?.mapNotNull { attachmentId -> attachmentController.findAttachmentById(attachmentId) }
                  ?.map{ attachment -> attachmentTranslator.translate(attachment) }
            }
            .flatMap { it?.toList() ?: emptyList() }
            .associateBy { attachment -> attachment.id.toString() }
  }

  override fun deleteReply(metaformId: UUID, replyId: UUID, ownerKey: String?): Response {
    val userId = loggedUserId ?: return createForbidden(UNAUTHORIZED)

    val reply = replyController.findReplyById(replyId)
            ?: return createNotFound(createNotFoundMessage(REPLY, metaformId))

    if (!isPermittedReply(reply, ownerKey, AuthorizationScope.REPLY_EDIT) && !isMetaformAdmin(metaformId)) {
      return createForbidden(createNotAllowedMessage(DELETE, REPLY))
    }

    val metaform = metaformController.findMetaformById(metaformId)
      ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    if (reply.metaform.id != metaform.id) {
      return createNotFound(createNotBelongMessage(REPLY))
    }

    auditLogEntryController.generateAuditLog(metaform, userId, reply.id!!, null, null, AuditLogEntryType.DELETE_REPLY)
    replyController.deleteReply(reply = reply)

    replyController.triggerReplyDeletedEvent(reply = reply)
    return createNoContent()
  }

  /**
   * Export reply
   *
   * @param metaformId metaform id
   * @param format format
   */
  override fun export(metaformId: UUID, format: String): Response {
    val userId = loggedUserId ?: return createForbidden(UNAUTHORIZED)

    if (!isMetaformAdmin(metaformId)) {
      return createForbidden(createNotAllowedMessage(EXPORT, REPLY))
    }

    val metaform = metaformController.findMetaformById(metaformId)
            ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    return if ("XLSX" == format) {
      val metaformEntity = try {
        metaformTranslator.translate(metaform)
      } catch (e: MalformedMetaformJsonException) {
        logger.error("Failed to read Metaform", e)
        return createInternalServerError(e.message)
      }

      val replies = replyController.listReplies(metaform = metaform, includeRevisions = false)
      val replyEntities = replies.map { reply -> replyTranslator.translate(metaformEntity, reply, null) }

      replies.forEach{ reply ->
        auditLogEntryController.generateAuditLog(
          metaform = metaform,
          userId = userId,
          replyId = reply.id!!,
          attachmentId = null,
          action = null,
          type = AuditLogEntryType.EXPORT_REPLY_XLSX
        )}

      try {
        streamResponse(replyController.getRepliesAsXlsx(metaform, metaformEntity, replyEntities), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
      } catch (e: XlsxException) {
        logger.error("Failed to rwite XLSX", e)
        createInternalServerError(e.message!!)
      }
    } else {
      createBadRequest(String.format("Unknown format %s", format))
    }
  }

  /**
   * Find a reply by id
   *
   * @param metaformId metaform id
   * @param replyId reply id
   * @param ownerKey owner key
   */
  override fun findReply(metaformId: UUID, replyId: UUID, ownerKey: String?): Response {
    val userId = loggedUserId ?: return createForbidden(UNAUTHORIZED)

    val metaform = metaformController.findMetaformById(metaformId)
            ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    val reply = replyController.findReplyById(replyId)
            ?: return createNotFound(createNotFoundMessage(REPLY, replyId))

    if (!isPermittedReply(reply, ownerKey, AuthorizationScope.REPLY_VIEW) && !isMetaformAdmin(metaformId)) {
      return createForbidden(createNotAllowedMessage(FIND, REPLY))
    }

    if (reply.metaform.id != metaform.id) {
      return createNotFound(createNotBelongMessage(REPLY))
    }

    val metaformEntity = try {
      metaformTranslator.translate(metaform)
    } catch (e: MalformedMetaformJsonException) {
      return createInternalServerError(e.message)
    }

    auditLogEntryController.generateAuditLog(
          metaform = metaform,
          userId = userId,
          replyId = reply.id!!,
          attachmentId = null,
          action = null,
          type = AuditLogEntryType.VIEW_REPLY
    )

    replyController.triggerReplyFoundEvent(reply = reply)

    return createOk(replyTranslator.translate(metaformEntity, reply, null))
  }

  /**
   * List replies
   *
   * @param metaformId metaform id
   * @param userId user id
   * @param createdBeforeParam created before
   * @param createdAfterParam created after
   * @param modifiedBeforeParam modified before
   * @param modifiedAfterParam modified after
   * @param includeRevisions include revisions
   * @param fields fields
   * @param firstResult first result
   * @param maxResults max results
   * @param orderBy criteria to order by
   * @param latestFirst return the latest result first according to the criteria in orderBy
   * @return list of replies
   */
  override fun listReplies(
    metaformId: UUID,
    userId: UUID?,
    createdBeforeParam: String?,
    createdAfterParam: String?,
    modifiedBeforeParam: String?,
    modifiedAfterParam: String?,
    includeRevisions: Boolean?,
    fields: List<String>?,
    firstResult: Int?,
    maxResults: Int?,
    orderBy: ReplyOrderCriteria?,
    latestFirst: Boolean?
  ): Response {
    val auditLogUser = loggedUserId ?: return createForbidden(UNAUTHORIZED)

    val createdBefore = parseTime(createdBeforeParam)
    val createdAfter = parseTime(createdAfterParam)
    val modifiedBefore = parseTime(modifiedBeforeParam)
    val modifiedAfter = parseTime(modifiedAfterParam)

    val metaform = metaformController.findMetaformById(metaformId) ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))
    val metaformEntity = try {
      metaformTranslator.translate(metaform)
    } catch (e: MalformedMetaformJsonException) {
      return createInternalServerError(e.message)
    }

    val fieldFilters = fieldController.parseFilters(metaformEntity, fields)

    val replies = replyController.listReplies(
      metaform = metaform,
      userId = userId,
      createdBefore = createdBefore,
      createdAfter = createdAfter,
      modifiedBefore = modifiedBefore,
      modifiedAfter = modifiedAfter,
      includeRevisions = includeRevisions != null && includeRevisions,
      fieldFilters = fieldFilters,
      firstResult = firstResult,
      maxResults = maxResults,
      orderBy = orderBy,
      latestFirst = latestFirst
    )

    replies.forEach { reply -> auditLogEntryController.generateAuditLog(
        metaform = metaform,
        userId = auditLogUser,
        replyId = reply.id!!,
        attachmentId = null,
        action = null,
        type = AuditLogEntryType.LIST_REPLY
      )
    }

    if (isMetaformAdmin(metaformId)) {
      return createOk(replies.map { replyTranslator.translate(metaformEntity, it, null) })
    }

    val result: List<Reply> = getPermittedReplies(metaformId, replies, AuthorizationScope.REPLY_VIEW)
            .map { entity -> replyTranslator.translate(metaformEntity, entity, null) }

    return createOk(result).also { it.headers.add("X-Total-Count", result.size) }
  }

  /**
   * Filters out replies without permission
   *
   * @param metaformId metaform id
   * @param replies replies
   * @param authorizationScope scope
   * @return filtered list
   */
  private fun getPermittedReplies(
    metaformId: UUID,
    replies: List<fi.metatavu.metaform.server.persistence.model.Reply>,
    authorizationScope: AuthorizationScope
  ): List<fi.metatavu.metaform.server.persistence.model.Reply> {
    if (isMetaformAdmin(metaformId)) {
      return replies
    }
    val resourceIds = replies
            .mapNotNull(fi.metatavu.metaform.server.persistence.model.Reply::resourceId).toSet()

    val permittedResourceIds = metaformKeycloakController.getPermittedResourceIds(tokenString, resourceIds, authorizationScope)
    return replies
            .filter { reply -> permittedResourceIds.contains(reply.resourceId) }
  }

  override fun replyExport(metaformId: UUID, replyId: UUID, format: String): Response {
    val userId = loggedUserId ?: return createForbidden(UNAUTHORIZED)

    val reply = replyController.findReplyById(replyId)
            ?: return createNotFound(createNotFoundMessage(REPLY, replyId))

    if (!isPermittedReply(reply, null, AuthorizationScope.REPLY_VIEW) && !isMetaformAdmin(metaformId)) {
      return createForbidden(createNotAllowedMessage(FIND, REPLY))
    }

    val metaform = metaformController.findMetaformById(metaformId)
      ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    if (reply.metaform.id != metaform.id) {
      return createNotFound(createNotBelongMessage(REPLY))
    }

    val metaformEntity = try {
      metaformTranslator.translate(metaform)
    } catch (e: MalformedMetaformJsonException) {
      return createInternalServerError(e.message)
    }

    /*if (metaform.exportTheme == null) {
      return createBadRequest("Metaform does not have an export theme")
    }*/

    val replyEntity: Reply = replyTranslator.translate(metaformEntity, reply, null)
    val attachmentMap = getAttachmentMap(metaformEntity, replyEntity)

    return try {
      val pdfData = replyController.getReplyPdf(metaform.exportTheme?.name ?: "base", metaformEntity, replyEntity, attachmentMap, locale)
      auditLogEntryController.generateAuditLog(metaform, userId, replyId, null, null, AuditLogEntryType.EXPORT_REPLY_PDF)
      streamResponse(pdfData, "application/pdf")
    } catch (e: PdfRenderException) {
      logger.error("Failed to generate PDF", e)
      createInternalServerError(e.message!!)
    }
  }

  /**
   * Update a reply by id
   *
   * @param metaformId metaform id
   * @param replyId reply id
   * @param reply reply data
   * @param ownerKey owner key
   * @return updated reply
   */
  override fun updateReply(
    metaformId: UUID,
    replyId: UUID,
    reply: Reply,
    ownerKey: String?
  ): Response {
    val userId = loggedUserId ?: return createForbidden(UNAUTHORIZED)

    val foundReply = replyController.findReplyById(replyId)
            ?: return createNotFound(createNotFoundMessage(REPLY, replyId))

    val metaform = metaformController.findMetaformById(metaformId)
      ?: return createNotFound(createNotFoundMessage(METAFORM, metaformId))

    if (foundReply.metaform.id != metaform.id) {
      return createNotFound(createNotBelongMessage(REPLY))
    }

    if (!isPermittedReply(foundReply, ownerKey, AuthorizationScope.REPLY_EDIT) && !isMetaformAdmin(metaformId)) {
      return createForbidden(createNotAllowedMessage(UPDATE, REPLY))
    }

    val metaformEntity = try {
      metaformTranslator.translate(metaform)
    } catch (e: MalformedMetaformJsonException) {
      return createInternalServerError(e.message)
    }

    val newPermissionGroups = EnumMap<AuthorizationScope, MutableList<String>>(AuthorizationScope::class.java)
    AuthorizationScope.values().forEach { scope -> newPermissionGroups[scope] = mutableListOf() }

    val fieldNames = replyController.listFieldNames(foundReply).toMutableList()
    val fieldMap = fieldController.getFieldMap(metaformEntity)

    val replyData = reply.data?.filter { (fieldName, fieldValue) ->
      @Suppress("SENSELESS_COMPARISON")
      fieldName != null && fieldValue != null
    } ?: return createBadRequest("Received a reply with null data")

    replyData.forEach { (fieldName, fieldValue) ->
      val field = fieldMap[fieldName]
      if (!replyController.isValidFieldValue(field!!, fieldValue)) {
        return createBadRequest(String.format("Invalid field value for field %s", fieldName))
      }

      replyController.setReplyField(fieldMap[fieldName]!!, foundReply, fieldName, fieldValue)
      fieldNames.remove(fieldName)
    }

    replyController.deleteReplyFields(foundReply, fieldNames)

    val replyEntity: Reply = replyTranslator.translate(metaformEntity, foundReply, null)

    auditLogEntryController.generateAuditLog(metaform, userId, foundReply.id!!, null, null, AuditLogEntryType.MODIFY_REPLY)

    try {
      val groupMemberPermissions = getGroupPermissions(
        metaformEntity = metaformEntity,
        replyData = replyData,
        fieldMap = fieldMap
      )

      metaformController.handleReplyPostPersist(
        replyCreated = false,
        metaform = metaform,
        reply = foundReply,
        replyEntity = replyEntity,
        loggedUserId = userId,
        groupMemberPermissions = groupMemberPermissions
      )

      replyController.updateReplyLastModifierId(foundReply, userId)

    } catch (e: AuthzException) {
      return createInternalServerError(e.message!!)
    }

    replyController.triggerReplyUpdatedEvent(reply = foundReply)
    return createNoContent()
  }

  /**
   * Returns group permissions based on metaform and reply data
   *
   * @param metaformEntity metaform
   * @param fieldMap field map
   * @param replyData reply data
   * @return group permissions based on metaform and reply data
   */
  private fun getGroupPermissions(
    metaformEntity: fi.metatavu.metaform.api.spec.model.Metaform,
    fieldMap: Map<String, MetaformField>,
    replyData: Map<String, Any>
  ): Set<GroupMemberPermission> {
    val result = mutableSetOf<GroupMemberPermission>()

    replyData.forEach { (fieldName, fieldValue) ->
      val field = fieldMap[fieldName] ?: return@forEach

      result.addAll(permissionController.getFieldGroupMemberPermissions(
        field = field,
        fieldValue = fieldValue
      ))
    }

    if (result.isEmpty()) {
      result.addAll(
        permissionController.getDefaultGroupMemberPermissions(
          metaform = metaformEntity
        )
      )
    }

    return result
  }

}