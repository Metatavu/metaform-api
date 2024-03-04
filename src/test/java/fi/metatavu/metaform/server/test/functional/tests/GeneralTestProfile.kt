package fi.metatavu.metaform.server.test.functional.tests

import io.quarkus.test.junit.QuarkusTestProfile

/**
 * Test profile settings common values for all tests
 */
class GeneralTestProfile : QuarkusTestProfile {
    override fun getConfigOverrides(): Map<String, String> {
        val properties: MutableMap<String, String> = HashMap()
        properties["runmode"] = "TEST"
        properties["metaform.uploads.folder"] = "/tmp"
        properties["quarkus.liquibase.contexts"] = "test"
        properties["metaforms.keycloak.card.identity.provider"] = "oidc"
        properties["metaforms.features.auditlog"] = "true"
        properties["metaforms.features.cardauth"] = "true"
        properties["billing.report.cron.key"] = "8EDCE3DF-0BC2-48AF-942E-25A9E83FA19D"
        properties["scheduled.invoices.createCron"] = "1 0 0 ? * * *"
        properties["scheduled.invoices.sendCron"] = "1 0 0 ? * * *"
        properties["billing.report.recipient.emails"] = "test@example.com"
        properties["quarkus.scheduler.enabled"] = "true"
        return properties
    }
}