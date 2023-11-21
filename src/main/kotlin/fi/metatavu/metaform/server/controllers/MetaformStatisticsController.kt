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
import kotlin.math.roundToInt

/**
 * Controller for Metaform Statistics
 */
@ApplicationScoped
class MetaformStatisticsController {

    @Inject
    lateinit var auditLogEntryDAO: AuditLogEntryDAO

    @Inject
    lateinit var replyDAO: ReplyDAO

    @Inject
    @CacheName("metaform-statistics")
    lateinit var statisticsCache: Cache

    /**
     * Recalculates statistics for given Metaform
     * @param metaform Metaform
     * @return MetaformStatistics for provided metaform
     */
    fun recalculateMetaformStatistics(metaform: Metaform): MetaformStatistics {
        statisticsCache.invalidate(metaform.id.toString()).await().indefinitely()
        return getMetaformStatistics(metaform)
    }

    /**
     * Calculates statistics for given Metaform
     * @param metaform Metaform
     * @return MetaformStatistics object for provided metaform
     */
    fun getMetaformStatistics(metaform: Metaform): MetaformStatistics {
        return statisticsCache.get(metaform.id.toString()) {
            calculateMetaformStatistics(metaform)
        }.await().indefinitely()
    }

    /**
     * Gets statistics for given Metaform
     *
     * @param metaform Metaform
     * @returns Metaform Statistics
     */
    private fun calculateMetaformStatistics(metaform: Metaform): MetaformStatistics {
        val lastReplyDate = replyDAO.getLastReplyDateByMetaform(metaform)
        val amountOfUnprocessedReplies = replyDAO.countUnprocessedReplies(metaform)
        val averageProcessDelay = auditLogEntryDAO.getAverageProcessDelayByMetaform(metaform)
        val averageMonthlyReplies = getAverageMonthlyReplies(metaform)

        return MetaformStatistics(
                metaformId = metaform.id,
                lastReplyDate = lastReplyDate,
                averageMonthlyReplies = averageMonthlyReplies,
                unprocessedReplies = amountOfUnprocessedReplies?.toInt(),
                averageReplyProcessDelay = averageProcessDelay?.roundToInt()
        )
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
            repliesCount += repliesGroupedByYearMonth[yearMonth]!!.size
        }

        return if (repliesGroupedByYearMonth.keys.isEmpty()) {
            0
        } else {
            repliesCount / repliesGroupedByYearMonth.keys.size
        }
    }
}