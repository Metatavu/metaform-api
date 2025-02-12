package fi.metatavu.metaform.server.metaform.jobs

import fi.metatavu.metaform.server.controllers.*
import fi.metatavu.metaform.server.persistence.dao.MetaformDAO
import fi.metatavu.metaform.server.persistence.model.AuditLogEntry
import fi.metatavu.metaform.server.persistence.model.Metaform
import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.util.*

@Transactional
@ApplicationScoped
class DeletionJobs {
    @Inject
    lateinit var draftController: DraftController

    @Inject
    lateinit var metaformKeycloakController: MetaformKeycloakController

    @Inject
    lateinit var auditLogEntryController: AuditLogEntryController

    @Inject
    lateinit var emailNotificationController: EmailNotificationController

    @Inject
    lateinit var replyController: ReplyController

    @Inject
    lateinit var metaformController: MetaformController

    @Inject
    lateinit var metaformVersionController: MetaformVersionController

    @Inject
    lateinit var metaformDAO: MetaformDAO


    /**
     * Deletes replies from forms that are marked as deleted
     */
    @Scheduled(every="\${metaforms.deletion.interval}", delayed = "\${metaforms.deletion.delay}")
    fun deleteMetaform() {
        val metaform = metaformController.listDeletedMetaforms().firstOrNull() ?: return

        val replies = replyController.listReplies(metaform, includeRevisions = true)
        val drafts = draftController.listByMetaform(metaform)
        val versions = metaformVersionController.listMetaformVersionsByMetaform(metaform)
        val emailNotifications = emailNotificationController.listEmailNotificationByMetaform(metaform)
        val auditLogEntriers = auditLogEntryController.listAuditLogEntries(metaform, null, null, null, null)

        var counter = 0
        counter = deleteMetaformResources(replies, replyController, counter)
        counter = deleteMetaformResources(drafts, draftController, counter)
        counter = deleteMetaformResources(versions, metaformVersionController, counter)

        counter = deleteMetaformMembers(counter, metaform)

        counter = deleteMetaformResources(emailNotifications, emailNotificationController, counter)
        counter = deleteMetaformResources(auditLogEntriers, auditLogEntryController, counter)

        if (counter < 10) {
            metaformDAO.delete(metaform)
            metaformKeycloakController.deleteMetaformManagementGroup(metaform.id!!)
        }
    }

    private fun deleteMetaformMembers(currentCounter: Int, metaform: Metaform): Int {
        var counter = currentCounter
        val metaformMembers = metaformKeycloakController.listMetaformMemberAdmin(metaform.id!!) +
                metaformKeycloakController.listMetaformMemberManager(metaform.id!!)
        for (member in metaformMembers) {
            if (counter < 10) {
                metaformKeycloakController.deleteMetaformMember(UUID.fromString(member.id), metaform.id!!)
            } else {
                break
            }

            counter++
        }

        return counter
    }

    private fun <T> deleteMetaformResources(resources: List<T>, controller: AbstractMetaformResourceController<T>, currentCounter: Int): Int {
        var counter = currentCounter
        for (resource in resources) {
            if (counter < 10) {
                controller.delete(resource)
            } else {
                break
            }

            counter++
        }

        return counter
    }
}
