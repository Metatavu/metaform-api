package fi.metatavu.metaform.server.controllers

import fi.metatavu.metaform.api.spec.model.MetaformVisibility
import fi.metatavu.metaform.server.billingReport.BillingReportFreemarkerRenderer
import fi.metatavu.metaform.server.billingReport.BillingReportMetaform
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
     * Periodic job that runs in the beginning of the month and creates the invoices for the starting month based on the
     * metaforms and their managers and groups
     */
    @Scheduled(
        every = "2s", // first day of every month at 00 10
        concurrentExecution = Scheduled.ConcurrentExecution.SKIP,
        delay = 10,
        delayUnit = TimeUnit.SECONDS,
    )
    @Transactional
    fun createInvoices() {
        val now = OffsetDateTime.now()  //invoices are created at 01.00
        logger.info("Creating the billing reports for the period of the starting month")

        val monthlyInvoices = monthlyInvoiceDAO.listInvoices(
            start = now,
            end = now.withDayOfMonth(now.month.length(now.toLocalDate().isLeapYear)).withHour(23).withMinute(59),
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
                metaform = metaform,
                metaformTitle = title,
                monthlyInvoice = newMontlyInvoice,
                groupsCount = groupsCount,
                managersCount = managersCount,
                metaformVisibility = metaform.visibility,
                created = now.withHour(1).withMinute(0)
            )
        }
    }

    /**
     * Periodic job that runs in the end of the month and sends the billing invoices to the configured email addresses
     */
    @Scheduled(
        every = "5s", // last day of every month
        delay = 15,
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
        val start = now.withDayOfMonth(1).withHour(0).withMinute(0)
        val end = now.withDayOfMonth(now.month.length(now.toLocalDate().isLeapYear)).withHour(23).withMinute(59)
        createBillingReport(start, end)
    }

    /**
     * Creates the billing report for the given period and sends it to the configured email addresses
     *
     * @param start start date
     * @param end end date
     */
    fun createBillingReport(start: OffsetDateTime?, end: OffsetDateTime?) {
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
        dataModelMap["from"] = formatter.format(start)
        dataModelMap["to"] = formatter.format(end)
        dataModelMap["totalInvoices"] = invoices.size

        val rendered = billingReportFreemarkerRenderer.render("billing-report.ftl", dataModelMap)
        println("Billing report rendered: \n$rendered")
        billingReportRecipientEmails.get().split(",").forEach {
            emailProvider.sendMail(
                toEmail = it,
                subject = BILLING_REPORT_MAIL_SUBJECT,
                content = rendered
            )
        }
    }

    // todo what is to happen to the invoice if metaform is deleted later along the way?
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