package fi.metatavu.metaform.server.email.mailgun

import fi.metatavu.metaform.server.email.EmailProvider
import net.sargue.mailgun.Configuration
import net.sargue.mailgun.Mail
import org.apache.commons.lang3.StringUtils
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.slf4j.Logger
import javax.annotation.PostConstruct
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Mailgun email provider implementation
 *
 * @author Heikki Kurhinen
 * @author Antti Leppä
 */
@ApplicationScoped
class MailgunEmailProviderImpl : EmailProvider {
    lateinit var configuration: Configuration

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

    @PostConstruct
    fun init() {
        configuration = Configuration()
                .domain(domain)
                .apiKey(apiKey)
                .from(senderName, senderEmail)
        if (StringUtils.isNotEmpty(apiUrl)) {
            configuration.apiUrl(apiUrl)
        }
    }

    override fun sendMail(toEmail: String?, subject: String?, content: String?, format: MailFormat?) {
        logger.info("Sending email to {}", toEmail)
        /**
        var mailBuilder = Mail.using(configuration)
                .to(toEmail)
                .subject(subject)
        mailBuilder = when (format) {
            MailFormat.HTML -> mailBuilder.html(content)
            MailFormat.PLAIN -> mailBuilder.text(content)
            else -> {
                logger.error("Unknown mail format {}", format)
                return
            }
        }
        val response = mailBuilder.build().send()
        if (response.isOk) {
            logger.info("Send email to {}", toEmail)
        } else {
            logger.info("Sending email to {} failed with message {}", toEmail, response.responseMessage())
        }
        **/
    }

}