package fi.metatavu.metaform.server.controllers

import org.slf4j.Logger
import java.util.*
import javax.annotation.PostConstruct
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.event.Observes
import javax.enterprise.event.TransactionPhase
import javax.inject.Inject
import javax.transaction.Transactional

@ApplicationScoped
@Suppress ("unused")
class MetaformStatisticsObserver {

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var metaformStatisticsController: MetaformStatisticsController

    @Inject
    lateinit var metaformController: MetaformController

    /**
     * Initializes observer. Method assures eager initialization of observer
     */
    @PostConstruct
    fun init() {
        // this method is left empty on purpose
    }

    /**
     * Event handler for reply created event after successful transaction
     *
     * @param event event
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    fun onReplyCreated(@Observes(during = TransactionPhase.AFTER_SUCCESS) event: ReplyCreatedEvent) {
        onReplyEvent(event.metaformId)
    }

    /**
     * Event handler for reply updated event after successful transaction
     *
     * @param event event
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    fun onReplyUpdated(@Observes(during = TransactionPhase.AFTER_SUCCESS) event: ReplyUpdatedEvent) {
        onReplyEvent(event.metaformId)
    }

    /**
     * Event handler for reply deleted event after successful transaction
     *
     * @param event event
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    fun onReplyDeleted(@Observes(during = TransactionPhase.AFTER_SUCCESS) event: ReplyDeletedEvent) {
        onReplyEvent(event.metaformId)
    }

    /**
     * Finds Metaform by given UUID and triggers statistics recalculation for given Metaform after successful
     * create/delete/update event
     *
     * @param metaformId
     */
    private fun onReplyEvent(metaformId: UUID) {
        val metaform = metaformController.findMetaformById(metaformId)
        if (metaform != null) {
            metaformStatisticsController.recalculateMetaformStatistics(metaform = metaform)
        } else {
            logger.error("Could not recalculate Metaform statistics")
        }
    }
}