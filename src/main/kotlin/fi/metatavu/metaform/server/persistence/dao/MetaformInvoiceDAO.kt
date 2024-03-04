package fi.metatavu.metaform.server.persistence.dao

import fi.metatavu.metaform.api.spec.model.MetaformVisibility
import fi.metatavu.metaform.server.persistence.model.Metaform
import fi.metatavu.metaform.server.persistence.model.billing.MetaformInvoice
import fi.metatavu.metaform.server.persistence.model.billing.MetaformInvoice_
import fi.metatavu.metaform.server.persistence.model.billing.MonthlyInvoice
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.TypedQuery
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import java.time.OffsetDateTime
import java.util.*

/**
 * DAO class for MetaformInvoice
 */
@ApplicationScoped
class MetaformInvoiceDAO: AbstractDAO<MetaformInvoice>() {

    /**
     * Lists invoices for dates
     *
     * @param monthlyInvoices monthly invoices
     * @param metaform metaform
     * @return list of invoices
     */
    fun listInvoices(
        monthlyInvoices: List<MonthlyInvoice>? = null,
        metaform: Metaform? = null
    ): List<MetaformInvoice> {
        val criteriaBuilder: CriteriaBuilder = entityManager.criteriaBuilder
        val criteria: CriteriaQuery<MetaformInvoice> = criteriaBuilder.createQuery(
            MetaformInvoice::class.java
        )
        val root = criteria.from(
            MetaformInvoice::class.java
        )
        criteria.select(root)

        if (monthlyInvoices != null) {
            criteria.where(root.get(MetaformInvoice_.monthlyInvoice).`in`(monthlyInvoices))
        }

        if (metaform != null) {
            criteria.where(criteriaBuilder.equal(root.get(MetaformInvoice_.metaform), metaform))
        }

        val query: TypedQuery<MetaformInvoice> = entityManager.createQuery(criteria)
        return query.resultList
    }

    /**
     * Creates a new MetaformInvoice
     *
     * @param id id
     * @param metaform metaform
     * @param monthlyInvoice monthly invoice
     * @param metaformVisibility metaform visibility
     * @param groupsCount groups count
     * @param managersCount managers count
     * @param created created
     * @return created metaform invoice
     */
    fun create(
        id: UUID,
        metaform: Metaform,
        monthlyInvoice: MonthlyInvoice,
        metaformVisibility: MetaformVisibility?,
        groupsCount: Int,
        managersCount: Int,
        created: OffsetDateTime,
        metaformTitle: String?
    ): MetaformInvoice {
        val metaformInvoice = MetaformInvoice()
        metaformInvoice.id = id
        metaformInvoice.metaform = metaform
        metaformInvoice.monthlyInvoice = monthlyInvoice
        metaformInvoice.title = metaformTitle
        metaformInvoice.visibility = metaformVisibility
        metaformInvoice.groupsCount = groupsCount
        metaformInvoice.managersCount = managersCount
        return persist(metaformInvoice)
    }
}