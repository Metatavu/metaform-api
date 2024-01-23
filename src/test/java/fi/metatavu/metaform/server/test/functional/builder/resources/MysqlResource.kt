package fi.metatavu.metaform.server.test.functional.builder.resources

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.MySQLContainerProvider

/**
 * Starts test container for mysql
 */
class MysqlResource : QuarkusTestResourceLifecycleManager {
    override fun start(): Map<String, String> {
        db.withCommand(
                "--character-set-server=utf8mb4",
                "--collation-server=utf8mb4_unicode_ci",
                "--lower_case_table_names=1"
        )
        db.start()
        val config: MutableMap<String, String> = HashMap()
        config["quarkus.datasource.username"] = USERNAME
        config["quarkus.datasource.password"] = PASSWORD
        config["quarkus.datasource.jdbc.url"] = db.jdbcUrl
        config["jdbc.driver"] = db.driverClassName
        return config
    }

    override fun stop() {
        db.stop()
    }

    companion object {
        private const val DATABASE = "db"
        private const val USERNAME = "user"
        private const val PASSWORD = "pass"
        val db: JdbcDatabaseContainer<*> = MySQLContainerProvider()
                .newInstance("5.6")
                .withDatabaseName(DATABASE)
                .withUsername(USERNAME)
                .withPassword(PASSWORD)
    }
}