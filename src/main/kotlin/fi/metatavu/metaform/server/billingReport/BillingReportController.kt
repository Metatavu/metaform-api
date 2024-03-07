package fi.metatavu.metaform.server.billingReport

import fi.metatavu.metaform.api.spec.model.MetaformVisibility
import fi.metatavu.metaform.server.controllers.MetaformController
import fi.metatavu.metaform.server.controllers.MetaformKeycloakController
import fi.metatavu.metaform.server.email.EmailProvider
import fi.metatavu.metaform.server.persistence.dao.MetaformInvoiceDAO
import fi.metatavu.metaform.server.persistence.dao.MonthlyInvoiceDAO
import fi.metatavu.metaform.server.persistence.model.billing.MetaformInvoice
import fi.metatavu.metaform.server.rest.translate.MetaformTranslator
import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.slf4j.Logger
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Controller for Billing Report
 */
@ApplicationScoped
class BillingReportController {

    @ConfigProperty(name = "billing.report.recipient.emails")
    lateinit var billingReportRecipientEmails: Optional<String>

    @ConfigProperty(name = "billing.report.form.cost", defaultValue = "50")
    var formCost: Int? = null

    @ConfigProperty(name = "billing.report.manager.cost", defaultValue = "50")
    var managerCost: Int? = null

    @ConfigProperty(name = "billing.report.admin.cost", defaultValue = "0")
    var adminCost: Int? = null

    @ConfigProperty(name = "billing.report.strongAuthentication.cost", defaultValue = "25")
    var authCost: Int? = null

    @Inject
    lateinit var metaformController: MetaformController

    @Inject
    lateinit var metaformKeycloakController: MetaformKeycloakController

    @Inject
    lateinit var billingReportFreemarkerRenderer: BillingReportFreemarkerRenderer
    
    @Inject
    lateinit var metaformTranslator: MetaformTranslator

    @Inject
    lateinit var emailProvider: EmailProvider

    @Inject
    lateinit var monthlyInvoiceDAO: MonthlyInvoiceDAO

    @Inject
    lateinit var metaformInvoiceDAO: MetaformInvoiceDAO

    @Inject
    lateinit var logger: Logger

    /**
     * Periodic job that creates the invoices for the starting month based on the
     * metaforms and their managers and groups.
     * Does not matter when it runs, as it will only create the invoice once per month
     */
    @Scheduled(
        cron = "\${createInvoices.cron.expr}",
        concurrentExecution = Scheduled.ConcurrentExecution.SKIP,
        delay = 10,
        delayUnit = TimeUnit.SECONDS,
    )
    @Transactional
    fun createInvoices() {
        val now = OffsetDateTime.now()
        logger.info("Creating the billing reports for the period of the starting month")

        val currentMonthStart = getCurrentMonthStart(now)
        val currentMonthEnd = getCurrentMonthEnd(now)

        val monthlyInvoices = monthlyInvoiceDAO.listInvoices(
            start = currentMonthStart,
            end = currentMonthEnd
        )

        if (monthlyInvoices.isNotEmpty()) {
            logger.info("Monthly invoice already exists for the current month, ${monthlyInvoices[0].startsAt}")
            return
        }

        val newMontlyInvoice = monthlyInvoiceDAO.create(
            id = UUID.randomUUID(),
            systemAdminsCount = metaformKeycloakController.getSystemAdministrators().size,
            startsAt = now,
        )

        metaformController.listMetaforms(
            active = true
        ).forEach { metaform ->
            val managersCount = metaformKeycloakController.listMetaformMemberManager(metaform.id!!).size
            val groupsCount = metaformKeycloakController.listMetaformMemberGroups(metaform.id!!).size
            val title = metaformTranslator.translate(metaform).title

            metaformInvoiceDAO.create(
                id = UUID.randomUUID(),
                metaformId = metaform.id!!,
                metaformTitle = title,
                monthlyInvoice = newMontlyInvoice,
                groupsCount = groupsCount,
                managersCount = managersCount,
                metaformVisibility = metaform.visibility,
                created = now
            )
        }
    }

    /**
     * Periodic job that sends the billing invoices to the configured email addresses. Can be run anytime after the createInvoices
     */
    @Scheduled(
        cron = "\${sendInvoices.cron.expr}",
        delay = 20,
        delayUnit = TimeUnit.SECONDS,
        concurrentExecution = Scheduled.ConcurrentExecution.SKIP
    )
    @Transactional
    fun sendInvoices() {
        logger.info("Sending the billing reports for the period of the current month")
        if (!billingReportRecipientEmails.isPresent) {
            logger.warn("No billing report recipient emails configured, cannot send invoices")
            return
        }
        val now = OffsetDateTime.now()
        val start = getCurrentMonthStart(now)
        val end = getCurrentMonthEnd(now)
        sendBillingReports(start, end, null)
    }

    /**
     * Creates the billing report for the given period and sends it to the configured email addresses
     *
     * @param start start date
     * @param end end date
     * @param specialReceiverEmails recipient email (if not used the default system recipient emails are used)
     */
    fun sendBillingReports(start: OffsetDateTime?, end: OffsetDateTime?, specialReceiverEmails: String?) {
        val invoices = monthlyInvoiceDAO.listInvoices(
            start = start,
            end = end,
        )
        val allMetaformInvoices = metaformInvoiceDAO.listInvoices(monthlyInvoices = invoices)

        val privateMetaformInvoices = allMetaformInvoices.filter { it.visibility == MetaformVisibility.PRIVATE }
        val publicMetaformInvoices = allMetaformInvoices.filter { it.visibility == MetaformVisibility.PUBLIC }

        val billingReportMetaforms = allMetaformInvoices.map {
            createBillingReportMetaform(it)
        }

        val totalManagersCount = billingReportMetaforms
            .map { it.managersCount }
            .fold(0) { sum, element -> sum + element }

        val totalAdminsCount = metaformKeycloakController.getSystemAdministrators()
            .filter { !it.email.contains(DOMAIN_TO_EXCLUDE) }
            .size

        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        val dataModelMap = HashMap<String, Any>()
        dataModelMap["strongAuthenticationCount"] = privateMetaformInvoices.size
        dataModelMap["strongAuthenticationCost"] = authCost!!
        dataModelMap["formsCount"] = privateMetaformInvoices.size + publicMetaformInvoices.size
        dataModelMap["formCost"] = formCost!!
        dataModelMap["managersCount"] = totalManagersCount
        dataModelMap["managerCost"] = managerCost!!
        dataModelMap["adminsCount"] = totalAdminsCount
        dataModelMap["adminCost"] = adminCost!!
        dataModelMap["forms"] = billingReportMetaforms
        dataModelMap["from"] = if (start == null)  "-" else formatter.format(start)
        dataModelMap["to"] = if (end == null)  "-" else formatter.format(end)
        dataModelMap["totalInvoices"] = invoices.size

        val rendered = billingReportFreemarkerRenderer.render("billing-report.ftl", dataModelMap)

        val recipientEmailLong = specialReceiverEmails ?: billingReportRecipientEmails.get()
        recipientEmailLong.replace(",", " ").split(" ").forEach {
            emailProvider.sendMail(
                toEmail = it.trim(),
                subject = BILLING_REPORT_MAIL_SUBJECT,
                content = rendered
            )
        }
    }

    /**
     * Gets the start of the current month
     *
     * @param time time
     * @return start of the current month
     */
    private fun getCurrentMonthStart(time: OffsetDateTime): OffsetDateTime {
        return time.withDayOfMonth(1).withHour(0).withMinute(0)
    }

    /**
     * Gets the end of the current month
     *
     * @param time time
     * @return end of the current month
     */
    private fun getCurrentMonthEnd(time: OffsetDateTime): OffsetDateTime {
        return time.withDayOfMonth(time.month.length(time.toLocalDate().isLeapYear)).withHour(23).withMinute(59)
    }

    /**
     * Creates Billing Report Metaform
     *
     * @param metaformInvoice Metaform
     * @returns Billing Report Metaform
     */
    private fun createBillingReportMetaform(metaformInvoice: MetaformInvoice): BillingReportMetaform {
        return BillingReportMetaform(
            title = metaformInvoice.title!!,
            strongAuthentication = metaformInvoice.visibility == MetaformVisibility.PRIVATE,
            managersCount = metaformInvoice.managersCount,
            groupsCount = metaformInvoice.groupsCount
        )
    }

    companion object {
        const val BILLING_REPORT_MAIL_SUBJECT = "Metaform Billing Report"
        const val DOMAIN_TO_EXCLUDE = "metatavu.fi"
    }
}