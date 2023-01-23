package fi.metatavu.metaform.server.controllers

import fi.metatavu.metaform.api.spec.model.MetaformVisibility
import fi.metatavu.metaform.server.billingReport.BillingReportDataModel
import fi.metatavu.metaform.server.billingReport.BillingReportFreemarkerRenderer
import fi.metatavu.metaform.server.billingReport.BillingReportMetaform
import fi.metatavu.metaform.server.email.EmailProvider
import fi.metatavu.metaform.server.email.mailgun.MailFormat
import fi.metatavu.metaform.server.persistence.model.Metaform
import fi.metatavu.metaform.server.rest.translate.MetaformTranslator
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
    lateinit var billingReportFreemarkerRenderer: BillingReportFreemarkerRenderer
    
    @Inject
    lateinit var metaformTranslator: MetaformTranslator

    @Inject
    lateinit var emailProvider: EmailProvider

    fun createBillingReport(period: Int?): String? {
        val metaforms = metaformController.listMetaforms().filter { !it.nonBillable && it.publishedAt != null }
        val privateMetaforms = metaforms.filter { it.visibility == MetaformVisibility.PRIVATE }
        val publicMetaforms = metaforms.filter { it.visibility == MetaformVisibility.PUBLIC }

        val billingReportMetaforms = mutableListOf<BillingReportMetaform>()
        metaforms.forEach {
            billingReportMetaforms.add(createBillingReportMetaform(it))
        }

        val totalManagersCount = billingReportMetaforms
            .map { it.managersCount }
            .fold(0) { sum, element -> sum + element }

        val totalAdminsCount = metaformKeycloakController.getSystemAdministrators()
            .filter { !it.email.contains(DOMAIN_TO_EXCLUDE) }
            .size

        val billingReportDataModel = BillingReportDataModel(
            strongAuthenticationCount = privateMetaforms.size,
            formsCount = privateMetaforms.size + publicMetaforms.size,
            managersCount = totalManagersCount,
            adminsCount = totalAdminsCount,
            forms = billingReportMetaforms
        )

        val dataModelMap = HashMap<String, Any>()
        dataModelMap.put("strongAuthenticationCount", billingReportDataModel.strongAuthenticationCount)
        dataModelMap.put("strongAuthenticationCost", billingReportDataModel.strongAuthenticationCost)
        dataModelMap.put("formsCount", billingReportDataModel.formsCount)
        dataModelMap.put("formCost", billingReportDataModel.formCost)
        dataModelMap.put("managersCount", billingReportDataModel.managersCount)
        dataModelMap.put("managerCost", billingReportDataModel.managerCost)
        dataModelMap.put("adminsCount", billingReportDataModel.adminsCount)
        dataModelMap.put("adminCost", billingReportDataModel.adminCost)
        dataModelMap.put("forms", billingReportDataModel.forms)

        return billingReportFreemarkerRenderer.render("billing-report.ftl", dataModelMap)
    }

    fun sendBillingReport(recipientEmail: String, content: String) {
        emailProvider.sendMail(
            toEmail = recipientEmail,
            subject = BILLING_REPORT_MAIL_SUBJECT,
            content = content,
            format = MailFormat.HTML
        )
    }

    /**
     * Creates Billing Report Metaform
     *
     * @param metaform Metaform
     * @returns Billing Report Metaform
     */
    private fun createBillingReportMetaform(metaform: Metaform): BillingReportMetaform {
        val translatedMetaform = metaformTranslator.translate(metaform)
        return BillingReportMetaform(
            title = translatedMetaform.title!!,
            strongAuthentication = translatedMetaform.visibility == MetaformVisibility.PRIVATE,
            managersCount = metaformKeycloakController.listMetaformMemberManager(metaform.id!!).size,
            groupsCount = metaformKeycloakController.listMetaformMemberGroups(metaform.id!!).size
        )
    }

    companion object {
        const val BILLING_REPORT_MAIL_SUBJECT = "Metaform Billing Report"
        const val DOMAIN_TO_EXCLUDE = "metatavu.fi"
    }
}