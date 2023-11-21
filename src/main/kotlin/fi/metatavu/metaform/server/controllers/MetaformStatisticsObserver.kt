package fi.metatavu.metaform.server.controllers

import org.slf4j.Logger
import java.util.*
import jakarta.annotation.PostConstruct
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.enterprise.event.TransactionPhase
import jakarta.inject.Inject
import jakarta.transaction.Transactional


/**
 * Observer for reply statistics recalculating triggered by event
 *
 * @author Harri Häkkinen
 */
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
        recalculateMetaformStatistics(event.metaformId)
    }

    /**
     * Event handler for reply updated event after successful transaction
     *
     * @param event event
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    fun onReplyUpdated(@Observes(during = TransactionPhase.AFTER_SUCCESS) event: ReplyUpdatedEvent) {
        recalculateMetaformStatistics(event.metaformId)
    }

    /**
     * Event handler for reply deleted event after successful transaction
     *
     * @param event event
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    fun onReplyDeleted(@Observes(during = TransactionPhase.AFTER_SUCCESS) event: ReplyDeletedEvent) {
        recalculateMetaformStatistics(event.metaformId)
    }

    /**
     * Event handler for reply found event after successful transaction
     *
     * @param event event
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    fun onReplyFound(@Observes(during = TransactionPhase.AFTER_SUCCESS) event: ReplyFoundEvent) {
        recalculateMetaformStatistics(event.metaformId)
    }

    /**
     * Finds Metaform by given UUID and triggers statistics recalculation for given Metaform after successful
     * create/delete/update event
     *
     * @param metaformId
     */
    private fun recalculateMetaformStatistics(metaformId: UUID) {
        val metaform = metaformController.findMetaformById(metaformId)
        if (metaform != null) {
            metaformStatisticsController.recalculateMetaformStatistics(metaform = metaform)
        } else {
            logger.error("Could not recalculate Metaform statistics")
        }
    }
}