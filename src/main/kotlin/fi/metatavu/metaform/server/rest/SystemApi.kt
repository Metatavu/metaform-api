package fi.metatavu.metaform.server.rest

import fi.metatavu.metaform.api.spec.model.BillingReportRequest
import fi.metatavu.metaform.server.billingReport.BillingReportController
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.config.ConfigProvider
import java.util.*

/**
 * Implementation for System API
 */
@RequestScoped
@Transactional
class SystemApi: fi.metatavu.metaform.api.spec.SystemApi, AbstractApi() {

  @Inject
  lateinit var billingReportController: BillingReportController

  private val cronKey: UUID?
    get() {
      return UUID.fromString(ConfigProvider.getConfig().getValue("billing.report.cron.key", String::class.java))
    }

  override fun ping(): Response {
    return createOk("pong")
  }

  override fun sendBillingReport(billingReportRequest: BillingReportRequest?): Response {
    if (requestCronKey == null && loggedUserId == null) return createForbidden(UNAUTHORIZED)

    if (cronKey != requestCronKey) {
      return createForbidden(UNAUTHORIZED)
    }

    billingReportController.sendBillingReports(billingReportRequest?.startDate, billingReportRequest?.endDate, billingReportRequest?.recipientEmail)
    return createNoContent()
  }
}