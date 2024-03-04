package fi.metatavu.metaform.server.persistence.model.billing

import fi.metatavu.metaform.api.spec.model.MetaformVisibility
import fi.metatavu.metaform.server.persistence.model.Metaform
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime
import java.util.*

/**
 * JPA entity representing single MonthlyInvoice
 * It is created when metaform is published
 */
@Entity
class MetaformInvoice {

    @Id
    lateinit var id: UUID

    @ManyToOne
    lateinit var metaform: Metaform

    @ManyToOne
    lateinit var monthlyInvoice: MonthlyInvoice

    //some data for reporting
    @NotNull
    @Enumerated(EnumType.STRING)
    var visibility: MetaformVisibility? = null

    @Column(nullable = false)
    var groupsCount: Int = 0

    @Column(nullable = false)
    var managersCount: Int = 0

    @Column(nullable = false)
    var title: String? = null
}