package fi.metatavu.metaform.server.persistence.dao

import fi.metatavu.metaform.server.persistence.model.billing.MonthlyInvoice
import fi.metatavu.metaform.server.persistence.model.billing.MonthlyInvoice_
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.TypedQuery
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*

/**
 * DAO class for MonthlyInvoice
 */
@ApplicationScoped
class MonthlyInvoiceDAO: AbstractDAO<MonthlyInvoice>() {

    /**
     * Lists invoices for dates
     *
     * @param start start date
     * @param end end date
     */
    fun listInvoices(
        start: LocalDate? = null,
        end: LocalDate? = null,
    ): List<MonthlyInvoice> {
        val criteriaBuilder: CriteriaBuilder = entityManager.criteriaBuilder
        val criteria: CriteriaQuery<MonthlyInvoice> = criteriaBuilder.createQuery(
            MonthlyInvoice::class.java
        )
        val root = criteria.from(
            MonthlyInvoice::class.java
        )
        criteria.select(root)

        if (start != null) {
            criteria.where(criteriaBuilder.greaterThanOrEqualTo(root.get(MonthlyInvoice_.startsAt), start))
        }

        if (end != null) {
            criteria.where(criteriaBuilder.lessThanOrEqualTo(root.get(MonthlyInvoice_.startsAt), end))
        }

        val query: TypedQuery<MonthlyInvoice> = entityManager.createQuery(criteria)
        return query.resultList
    }

    /**
     * Creates a new MonthlyInvoice
     *
     * @param id id
     * @param systemAdminsCount system admins count
     * @param startsAt invoice start date
     * @param createdAt created at
     * @param startsAt start date
     */
    fun create(id: UUID, systemAdminsCount: Int, startsAt: LocalDate, createdAt: OffsetDateTime): MonthlyInvoice {
        val metaformInvoice = MonthlyInvoice()
        metaformInvoice.id = id
        metaformInvoice.systemAdminsCount = systemAdminsCount
        metaformInvoice.startsAt = startsAt
        metaformInvoice.createdAt = createdAt
        return persist(metaformInvoice)
    }
}