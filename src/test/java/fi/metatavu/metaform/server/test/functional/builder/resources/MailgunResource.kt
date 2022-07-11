package fi.metatavu.metaform.server.test.functional.builder.resources

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import org.testcontainers.containers.GenericContainer

/**
 * Resource for wiremock container
 */
class MailgunResource : QuarkusTestResourceLifecycleManager {
    override fun start(): Map<String, String> {
        container.start()
        val config: MutableMap<String, String> = HashMap()
        config["wiremock.host"] = container.host
        config["wiremock.port"] = container.getMappedPort(8080).toString()
        config["mailgun.api_url"] = "http://" + container.host + ':' + container.getMappedPort(8080).toString() + "/mgapi"
        config["mailgun.domain"] = "domain.example.com"
        config["mailgun.api_key"] = "fakekey"
        config["mailgun.sender_email"] = "metaform-test@example.com"
        config["mailgun.sender_name"] = "Metaform Test"
        return config
    }

    override fun stop() {
        container.stop()
    }

    companion object {
        var container: GenericContainer<*> = GenericContainer("rodolpheche/wiremock")
                .withExposedPorts(8080)
    }
}