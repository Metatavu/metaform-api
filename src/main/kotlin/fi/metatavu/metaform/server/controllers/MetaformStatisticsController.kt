package fi.metatavu.metaform.server.controllers

import fi.metatavu.metaform.api.spec.model.MetaformStatistics
import fi.metatavu.metaform.server.persistence.dao.AuditLogEntryDAO
import fi.metatavu.metaform.server.persistence.dao.ReplyDAO
import fi.metatavu.metaform.server.persistence.model.Metaform
import io.quarkus.cache.Cache
import io.quarkus.cache.CacheName
import java.time.YearMonth
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.slf4j.Logger
import java.util.UUID
import kotlin.math.roundToInt

/**
 * Controller for Metaform Statistics
 */
@ApplicationScoped
class MetaformStatisticsController {

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var replyDAO: ReplyDAO

    @Inject
    @CacheName("metaform-statistics-last-reply-date")
    lateinit var statisticsLastReplyDateCache: Cache

    @Inject
    @CacheName("metaform-statistics-average-monthly-replies")
    lateinit var statisticsAverageMonthlyRepliesCache: Cache

    @Inject
    @CacheName("metaform-statistics-unprocessed-replies")
    lateinit var statisticsUnprocessedRepliesCache: Cache

    @Inject
    @CacheName("metaform-statistics-average-reply-process-delay")
    lateinit var statisticsAverageReplyProcessDelayCache: Cache

    /**
     * Invalidated last reply date cache.
     *
     * This method should be called when new replies are added or when reply is deleted
     *
     * @param metaformId metaform id
     */
    fun invalidateLastReplyDateCache(metaformId: UUID) {
        statisticsLastReplyDateCache.invalidate(metaformId).await().indefinitely()
    }

    /**
     * Invalidates average monthly replies cache.
     *
     * This method should be called when new replies are added or when replies are updated or deleted
     *
     * @param metaformId metaform id
     */
    fun invalidateAverageMonthlyRepliesCache(metaformId: UUID) {
        statisticsAverageMonthlyRepliesCache.invalidate(metaformId).await().indefinitely()
    }

    /**
     * Invalidates unprocessed replies cache.
     *
     * This method should be called when new replies are added or when replies are updated or deleted
     *
     * @param metaformId metaform id
     */
    fun invalidateUnprocessedRepliesCache(metaformId: UUID) {
        statisticsUnprocessedRepliesCache.invalidate(metaformId).await().indefinitely()
    }

    /**
     * Invalidates average reply process delay cache.
     *
     * This method should be called when new replies are added or deleted
     *
     * @param metaformId metaform id
     */
    fun invalidateAverageReplyProcessDelayCache(metaformId: UUID) {
        statisticsAverageReplyProcessDelayCache.invalidate(metaformId).await().indefinitely()
    }

    /**
     * Calculates statistics for given Metaform
     * @param metaform Metaform
     * @return MetaformStatistics object for provided metaform
     */
    fun getMetaformStatistics(metaform: Metaform): MetaformStatistics {
        val lastReplyDate = statisticsLastReplyDateCache.get(metaform.id) {
            recalculateStatistic(statistic = "last reply date", metaform = metaform) {
                replyDAO.getLastReplyDateByMetaform(metaform)
            }
        }.await().indefinitely()

        val averageMonthlyReplies = statisticsAverageMonthlyRepliesCache.get(metaform.id) {
            recalculateStatistic(statistic = "average monthly replies", metaform = metaform) {
                getAverageMonthlyReplies(metaform)
            }
        }.await().indefinitely()

        val amountOfUnprocessedReplies = statisticsUnprocessedRepliesCache.get(metaform.id) {
            recalculateStatistic(statistic = "count unprocessed replies", metaform = metaform) {
                replyDAO.countUnprocessedReplies(metaform)
            }
        }.await().indefinitely()

        val averageProcessDelay = statisticsAverageReplyProcessDelayCache.get(metaform.id) {
            recalculateStatistic(statistic = "average process delay", metaform = metaform) {
                replyDAO.getAverageProcessDelayByMetaform(metaform)
            }
        }.await().indefinitely()

        return MetaformStatistics(
                metaformId = metaform.id,
                lastReplyDate = lastReplyDate,
                averageMonthlyReplies = averageMonthlyReplies,
                unprocessedReplies = amountOfUnprocessedReplies?.toInt(),
                averageReplyProcessDelay = averageProcessDelay?.roundToInt()
        )
    }

    /**
     * Recalculates statistic and logs recalculation time
     *
     * @param statistic statistic name
     * @param metaform metaform
     * @param method method to recalculate statistic
     * @return recalculated statistic
     */
    private fun <T> recalculateStatistic (statistic: String, metaform: Metaform, method: () -> T): T {
        val recalculationStart = System.currentTimeMillis()
        val result = method()
        val recalculationTime = System.currentTimeMillis() - recalculationStart
        logger.info("Recalculated $statistic of ${metaform.id} in $recalculationTime ms")
        return result
    }

    /**
     * Gets average replies for Metaform per month
     *
     * @param metaform metaform
     * @returns Average replies per month
     */
    private fun getAverageMonthlyReplies(metaform: Metaform): Int {
        val replies = replyDAO.listByMetaform(metaform)
        val repliesGroupedByYearMonth = replies.groupBy { YearMonth.from(it.createdAt) }
        var repliesCount = 0

        repliesGroupedByYearMonth.keys.forEach { yearMonth ->
            repliesCount += repliesGroupedByYearMonth[yearMonth]?.size ?: 0
        }

        return if (repliesGroupedByYearMonth.keys.isEmpty()) {
            0
        } else {
            repliesCount / repliesGroupedByYearMonth.keys.size
        }
    }
}