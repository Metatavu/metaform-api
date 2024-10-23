package fi.metatavu.metaform.server.billingReport

import fi.metatavu.metaform.api.spec.model.MetaformVisibility
import fi.metatavu.metaform.server.controllers.MetaformController
import fi.metatavu.metaform.server.controllers.MetaformKeycloakController
import fi.metatavu.metaform.server.email.EmailProvider
import fi.metatavu.metaform.server.persistence.dao.MetaformInvoiceDAO
import fi.metatavu.metaform.server.persistence.dao.MonthlyInvoiceDAO
import fi.metatavu.metaform.server.persistence.model.billing.MetaformInvoice
import fi.metatavu.metaform.server.persistence.model.billing.MonthlyInvoice
import fi.metatavu.metaform.server.rest.translate.MetaformTranslator
import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.slf4j.Logger
import java.time.LocalDate
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
     * Periodic job that creates the invoices for the current month based on the
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
        logger.info("Creating the billing reports for the current month")
        val now = OffsetDateTime.now()
        val currentMonthStart = getMonthStart(now)
        val currentMonthEnd = getMonthEnd(now)

        val monthlyInvoices = monthlyInvoiceDAO.listInvoices(
            start = currentMonthStart,
            end = currentMonthEnd
        )

        if (monthlyInvoices.isNotEmpty()) {
            logger.info("Monthly invoice already exists for the current month, ${monthlyInvoices[0].startsAt}")
            return
        }

        buildInvoice()
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
        val start = getMonthStart(now)
        val end = getMonthEnd(now)
        sendBillingReports(start, end, null)
    }

    /**
     * Creates the billing report for the given period and sends it to the configured email addresses
     *
     * @param start start date
     * @param end end date
     * @param specialReceiverEmails recipient email (if not used the default system recipient emails are used)
     */
    fun sendBillingReports(start: LocalDate?, end: LocalDate?, specialReceiverEmails: String?) {
        logger.info("Sending the billing reports for the period of the given dates")
        val invoices = monthlyInvoiceDAO.listInvoices(
            start = start,
            end = end,
        ).toMutableList()

        val now = OffsetDateTime.now()
        val currentMonthStart = getMonthStart(now)

        // If the report is requested for no month or the current month build it immediately (if missing)
        if ((start == null && end == null) || (end != null && end >= currentMonthStart)) {
            if (invoices.isEmpty()) invoices.add(buildInvoice())
        }

        val allMetaformInvoices = metaformInvoiceDAO.listInvoices(monthlyInvoices = invoices)
        val privateMetaformInvoices = allMetaformInvoices.filter { it.visibility == MetaformVisibility.PRIVATE }
        val publicMetaformInvoices = allMetaformInvoices.filter { it.visibility == MetaformVisibility.PUBLIC }

        val billingReportMetaforms = allMetaformInvoices.map {
            createBillingReportMetaform(it)
        }
        val totalManagersCount = billingReportMetaforms
            .map { it.managersCount }
            .fold(0) { sum, element -> sum + element }

        val totalAdminsCount = invoices
            .map { it.systemAdminsCount }
            .fold(0) { sum, element -> sum + element }

        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        val dataModelMap = HashMap<String, Any>()
        dataModelMap["strongAuthenticationCount"] = privateMetaformInvoices.size
        dataModelMap["formsCount"] = privateMetaformInvoices.size + publicMetaformInvoices.size
        dataModelMap["managersCount"] = totalManagersCount
        dataModelMap["adminsCount"] = totalAdminsCount
        dataModelMap["forms"] = billingReportMetaforms
        dataModelMap["from"] = if (start == null) "-" else formatter.format(start)
        dataModelMap["to"] = if (end == null) "-" else formatter.format(end)
        dataModelMap["totalInvoices"] = invoices.size

        val rendered = billingReportFreemarkerRenderer.render(
            configuration = billingReportFreemarkerRenderer.configuration,
            templateName = "billing-report.ftl",
            dataModel = dataModelMap,
            locale = null
        )

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
     * Builds the invoice for the current month
     *
     * @return MonthlyInvoice
     */
    private fun buildInvoice(): MonthlyInvoice {
        val now = OffsetDateTime.now()
        val newMonthlyInvoice = monthlyInvoiceDAO.create(
            id = UUID.randomUUID(),
            systemAdminsCount = metaformKeycloakController.getSystemAdministrators().size,
            startsAt = getMonthStart(now),
            createdAt = now
        )

        val metaformInvoices = metaformController.listMetaforms(
            active = true
        ).map { metaform ->
            val managersCount = metaformKeycloakController.listMetaformMemberManager(metaform.id!!).size
            val groupsCount = metaformKeycloakController.listMetaformMemberGroups(metaform.id!!).size
            val title = metaformTranslator.translate(metaform).title

            metaformInvoiceDAO.create(
                id = UUID.randomUUID(),
                metaformId = metaform.id!!,
                metaformTitle = title,
                monthlyInvoice = newMonthlyInvoice,
                groupsCount = groupsCount,
                managersCount = managersCount,
                metaformVisibility = metaform.visibility
            )
        }
        logger.info("Created new monthly invoice for ${getMonthStart(now)} with ${metaformInvoices.size} metaform invoices")
        return newMonthlyInvoice
    }

    /**
     * Gets the start of the month
     *
     * @param time time
     * @return start of the month
     */
    private fun getMonthStart(time: OffsetDateTime): LocalDate {
        return time.withDayOfMonth(1).toLocalDate()
    }

    /**
     * Gets the end of the month
     *
     * @param time time
     * @return end of the month
     */
    private fun getMonthEnd(time: OffsetDateTime): LocalDate {
        return time.withDayOfMonth(time.month.length(time.toLocalDate().isLeapYear)).toLocalDate()
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