package fi.metatavu.metaform.server.controllers

import org.eclipse.microprofile.config.ConfigProvider
import javax.enterprise.context.ApplicationScoped

/**
 * Controller for System settings
 */
@ApplicationScoped
class SystemSettingController {

    /**
     * Returns whether the system is running in test mode or not
     *
     * @return whether the system is running in test mode or not
     */
    fun inTestMode(): Boolean {
        return "TEST" == ConfigProvider.getConfig().getValue("runmode", String::class.java)
    }
}