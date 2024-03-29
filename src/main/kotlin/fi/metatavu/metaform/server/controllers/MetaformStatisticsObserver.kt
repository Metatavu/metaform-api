package fi.metatavu.metaform.server.controllers

import org.slf4j.Logger
import jakarta.annotation.PostConstruct
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.enterprise.event.TransactionPhase
import jakarta.inject.Inject


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
    fun onReplyCreated(@Observes(during = TransactionPhase.AFTER_SUCCESS) event: ReplyCreatedEvent) {
        logger.info("Reply created, clearing related caches...")

        metaformStatisticsController.invalidateLastReplyDateCache(metaformId = event.metaformId)
        metaformStatisticsController.invalidateAverageMonthlyRepliesCache(metaformId = event.metaformId)
        metaformStatisticsController.invalidateUnprocessedRepliesCache(metaformId = event.metaformId)
        metaformStatisticsController.invalidateAverageReplyProcessDelayCache(metaformId = event.metaformId)
    }

    /**
     * Event handler for reply updated event after successful transaction
     *
     * @param event event
     */
    fun onReplyUpdated(@Observes(during = TransactionPhase.AFTER_SUCCESS) event: ReplyUpdatedEvent) {
        logger.info("Reply updated, clearing related caches...")

        metaformStatisticsController.invalidateUnprocessedRepliesCache(metaformId = event.metaformId)
    }

    /**
     * Event handler for reply deleted event after successful transaction
     *
     * @param event event
     */
    fun onReplyDeleted(@Observes(during = TransactionPhase.AFTER_SUCCESS) event: ReplyDeletedEvent) {
        logger.info("Reply deleted, clearing related caches...")

        metaformStatisticsController.invalidateLastReplyDateCache(metaformId = event.metaformId)
        metaformStatisticsController.invalidateAverageMonthlyRepliesCache(metaformId = event.metaformId)
        metaformStatisticsController.invalidateUnprocessedRepliesCache(metaformId = event.metaformId)
        metaformStatisticsController.invalidateAverageReplyProcessDelayCache(metaformId = event.metaformId)
    }

    /**
     * Event handler for reply found event after successful transaction
     *
     * @param event event
     */
    fun onReplyFound(@Observes(during = TransactionPhase.AFTER_SUCCESS) event: ReplyFoundEvent) {
        logger.info("Reply found, clearing related caches...")

        metaformStatisticsController.invalidateAverageReplyProcessDelayCache(metaformId = event.metaformId)
    }
}