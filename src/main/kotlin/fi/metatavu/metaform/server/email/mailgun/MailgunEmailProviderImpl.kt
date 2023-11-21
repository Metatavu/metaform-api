package fi.metatavu.metaform.server.email.mailgun

import fi.metatavu.metaform.server.email.EmailProvider
import io.vertx.core.MultiMap
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.client.WebClient
/*import net.sargue.mailgun.Configuration
import net.sargue.mailgun.Mail*/
import org.apache.commons.lang3.StringUtils
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.slf4j.Logger
import jakarta.annotation.PostConstruct
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

/**
 * Mailgun email provider implementation
 *
 * @author Heikki Kurhinen
 * @author Antti Leppä
 */
@ApplicationScoped
class MailgunEmailProviderImpl : EmailProvider {
    /*lateinit var configuration: Configuration*/

    @Inject
    lateinit var logger: Logger

    @Inject
    @ConfigProperty(name = "mailgun.domain")
    lateinit var domain: String

    @Inject
    @ConfigProperty(name = "mailgun.api_key")
    lateinit var apiKey: String

    @Inject
    @ConfigProperty(name = "mailgun.sender_name")
    lateinit var senderName: String

    @Inject
    @ConfigProperty(name = "mailgun.sender_email")
    lateinit var senderEmail: String

    @Inject
    @ConfigProperty(name = "mailgun.api_url")
    lateinit var apiUrl: String

    @Inject
    lateinit var vertx: Vertx

    @PostConstruct
    fun init() {

    }

    /**
     * Sends an email
     *
     * @param to email which will receive this email
     * @param subject email subject
     * @param content email content
     */
    override fun sendMail(toEmail: String?, subject: String?, content: String?, format: MailFormat?) {
        val client: WebClient = WebClient.create(vertx)
        client.requestAbs(
                HttpMethod.POST,
                "$apiUrl/$domain/messages",
        ).basicAuthentication("api", apiKey)
                .sendForm(
                        MultiMap.caseInsensitiveMultiMap()
                                .add("to", toEmail)
                                .add("subject", subject)
                                .add("html", content)
                                .add("from", "$senderName <$senderEmail>")
                )
    }
}