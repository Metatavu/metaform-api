package fi.metatavu.metaform.server.billingReport

/**
 * POJO for Billing Report Data Model
 */
data class BillingReportDataModel (
    val strongAuthenticationCount: Int,
    val strongAuthenticationCost: Int = 25,
    val formsCount: Int,
    val formCost: Int = 50,
    val managersCount: Int,
    val managerCost: Int = 0,
    val adminsCount: Int,
    val adminCost: Int = 0,
    val forms: List<BillingReportMetaform>
)