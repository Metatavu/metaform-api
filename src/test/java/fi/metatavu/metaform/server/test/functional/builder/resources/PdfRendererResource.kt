package fi.metatavu.metaform.server.test.functional.builder.resources

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testcontainers.containers.GenericContainer

/**
 * Starts test container for PDF renderer
 */
class PdfRendererResource : QuarkusTestResourceLifecycleManager {

    override fun start(): Map<String, String> {
        container.start()

        return hashMapOf(
            "metaform.pdf-renderer.url" to "http://${container.host}:${container.getMappedPort(3000)}/dev/print"
        )
    }

    override fun stop() {
        container.stop()
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(PdfRendererResource::class.java)

        private val container: GenericContainer<*> = GenericContainer("ghcr.io/metatavu/aws-lambda-pdf-generator:master")
            .withLogConsumer {
                logger.info(it.utf8String)
            }
            .withExposedPorts(3000)
    }

}