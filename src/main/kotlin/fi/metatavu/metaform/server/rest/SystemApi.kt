package fi.metatavu.metaform.server.rest

import fi.metatavu.metaform.api.spec.model.BillingReportRequest
import fi.metatavu.metaform.server.billingReport.BillingReportController
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.util.*

/**
 * Implementation for System API
 */
@RequestScoped
@Transactional
class SystemApi: fi.metatavu.metaform.api.spec.SystemApi, AbstractApi() {

  @Inject
  lateinit var billingReportController: BillingReportController

  @ConfigProperty(name = "billing.report.cron.key")
  lateinit var requestCronKeyString: Optional<String>

  private val cronKey: UUID?
    get() {
      if (requestCronKeyString.isEmpty) return null
      return UUID.fromString(requestCronKeyString.get())
    }

  override fun ping(): Response {
    return createOk("pong")
  }

  override fun sendBillingReport(billingReportRequest: BillingReportRequest?): Response {
    if (requestCronKey == null && loggedUserId == null) return createForbidden(UNAUTHORIZED)

    if (cronKey != requestCronKey) {
      return createForbidden(UNAUTHORIZED)
    }

    if (billingReportRequest?.startDate != null && billingReportRequest.startDate.isAfter(billingReportRequest.endDate)) {
      return createBadRequest("Start date cannot be after end date")
    }

    billingReportController.sendBillingReports(billingReportRequest?.startDate, billingReportRequest?.endDate, billingReportRequest?.recipientEmail)
    return createNoContent()
  }
}