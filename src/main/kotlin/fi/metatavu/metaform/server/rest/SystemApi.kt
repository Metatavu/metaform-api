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
class SystemApi: fi.metatavu.metaform.api.spec.SystemApi, AbstractApi() {

  @Inject
  lateinit var billingReportController: BillingReportController

  @ConfigProperty(name = "billing.report.key")
  lateinit var apiKey: Optional<String>

  override fun ping(): Response {
    return createOk("pong")
  }

  @Transactional
  override fun sendBillingReport(billingReportRequest: BillingReportRequest?): Response {
    if (apiKey.isEmpty) return createForbidden(UNAUTHORIZED)
    if (apiKey.get() != requestApiKey) {
      return createForbidden(UNAUTHORIZED)
    }

    if (billingReportRequest?.startDate != null && billingReportRequest.startDate.isAfter(billingReportRequest.endDate)) {
      return createBadRequest("Start date cannot be after end date")
    }

    billingReportController.sendBillingReports(billingReportRequest?.startDate, billingReportRequest?.endDate, billingReportRequest?.recipientEmail)
    return createNoContent()
  }
}