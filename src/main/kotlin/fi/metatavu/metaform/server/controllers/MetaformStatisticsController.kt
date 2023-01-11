package fi.metatavu.metaform.server.controllers

import fi.metatavu.metaform.api.spec.model.MetaformStatistics
import fi.metatavu.metaform.server.persistence.dao.AuditLogEntryDAO
import fi.metatavu.metaform.server.persistence.dao.ReplyDAO
import fi.metatavu.metaform.server.persistence.model.Metaform
import java.time.YearMonth
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
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

    /**
     * Gets statistics for given Metaform
     *
     * @param metaform Metaform
     * @returns Metaform Statistics
     */
    fun getMetaformStatistics(metaform: Metaform): MetaformStatistics {
        val lastReplyDate = auditLogEntryDAO.getLastReplyDateByMetaform(metaform)
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