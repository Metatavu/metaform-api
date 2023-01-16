package fi.metatavu.metaform.server.controllers

import fi.metatavu.metaform.server.email.EmailProvider
import fi.metatavu.metaform.server.email.mailgun.MailFormat
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for Billing Report
 */
@ApplicationScoped
class BillingReportController {

    @Inject
    lateinit var metaformController: MetaformController

    @Inject
    lateinit var metaformKeycloakController: MetaformKeycloakController

    @Inject
    lateinit var emailProvider: EmailProvider

    fun createBillingReport(period: Int?) {
        val metaforms = metaformController.
    }

    fun sendBillingReport(recipientEmail: String, content: String) {
        emailProvider.sendMail(
            toEmail = recipientEmail,
            subject = BILLING_REPORT_MAIL_SUBJECT,
            content = content,
            format = MailFormat.HTML
        )
    }

    companion object {
        const val BILLING_REPORT_MAIL_SUBJECT = "Metaform Billing Report"
    }
}