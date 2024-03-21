package fi.metatavu.metaform.server.billingReport

/**
 * POJO for Billing Report Metaform
 */
data class BillingReportMetaform (
    val title: String,
    val strongAuthentication: Boolean,
    val managersCount: Int,
    val groupsCount: Int
)