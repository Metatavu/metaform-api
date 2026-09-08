package fi.metatavu.metaform.server.controllers

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@ApplicationScoped
class ReplyAuthorizationExecutor {

    @Inject
    @ConfigProperty(name = "metaforms.keycloak.authorization.parallelism", defaultValue = "8")
    var parallelism: Int = 8

    private lateinit var executor: ExecutorService

    @PostConstruct
    fun initialize() {
        require(parallelism > 0) {
            "metaforms.keycloak.authorization.parallelism must be greater than zero"
        }
        executor = Executors.newFixedThreadPool(parallelism)
    }

    fun <T> submit(task: () -> T): CompletableFuture<T> {
        return CompletableFuture.supplyAsync(task, executor)
    }

    @PreDestroy
    fun shutdown() {
        if (::executor.isInitialized) {
            executor.shutdownNow()
        }
    }
}
