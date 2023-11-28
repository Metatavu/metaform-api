package fi.metatavu.metaform.server.rest

import fi.metatavu.metaform.server.controllers.MetaformController
import fi.metatavu.metaform.server.controllers.MetaformStatisticsController
import java.util.*
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.core.Response

/**
 * Implementation for Metaform Statistics API
 */
@RequestScoped
@Transactional
class MetaformStatisticsApi: fi.metatavu.metaform.api.spec.MetaformStatisticsApi, AbstractApi() {

    @Inject
    lateinit var metaformController: MetaformController

    @Inject
    lateinit var metaformStatisticsController: MetaformStatisticsController

    override fun getStatistics(metaformId: UUID): Response {
        loggedUserId ?: return createForbidden(UNAUTHORIZED)

        if (!isMetaformManager(metaformId)) {
            return createForbidden(createNotAllowedMessage(FIND, METAFORM))
        }

        val metaform = metaformController.findMetaformById(metaformId)
            ?: return createBadRequest("Invalid request")

        return createOk(metaformStatisticsController.getMetaformStatistics(metaform))
    }
}