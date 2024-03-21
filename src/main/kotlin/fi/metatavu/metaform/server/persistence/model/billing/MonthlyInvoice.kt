package fi.metatavu.metaform.server.persistence.model.billing

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.OffsetDateTime
import java.util.*

/**
 * JPA entity representing single MonthlyInvoice
 */
@Entity
class MonthlyInvoice {

    @Id
    lateinit var id: UUID

    @Column(nullable = false)
    var startsAt: OffsetDateTime? = null

    //some data for reporting
    @Column(nullable = false)
    var systemAdminsCount: Int = 0
}