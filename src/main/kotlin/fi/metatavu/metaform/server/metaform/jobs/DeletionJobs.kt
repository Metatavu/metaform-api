package fi.metatavu.metaform.server.metaform.jobs

import fi.metatavu.metaform.server.controllers.MetaformController
import fi.metatavu.metaform.server.controllers.ReplyController
import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional

@Transactional
@ApplicationScoped
class DeletionJobs {
    @Inject
    lateinit var replyController: ReplyController

    @Inject
    lateinit var metaformController: MetaformController

    /**
     * Deletes replies from forms that are marked as deleted
     */
    @Scheduled(every="60s")
    fun deleteReplies() {
        println("Running job")
        var counter = 0
        metaformController.listDeletedMetaforms().forEach { form ->
            val replies = replyController.listReplies(metaform = form, includeRevisions = true)
            for (reply in replies) {
                if (counter < 10) {
                    replyController.deleteReply(reply)
                } else {
                    break;
                }
                counter++
            }
        }
    }
}