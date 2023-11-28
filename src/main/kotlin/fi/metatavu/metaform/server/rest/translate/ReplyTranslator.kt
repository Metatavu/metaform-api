package fi.metatavu.metaform.server.rest.translate

import fi.metatavu.metaform.api.spec.model.Metaform
import fi.metatavu.metaform.api.spec.model.MetaformField
import fi.metatavu.metaform.api.spec.model.Reply
import fi.metatavu.metaform.server.controllers.CryptoController
import fi.metatavu.metaform.server.controllers.FieldController
import fi.metatavu.metaform.server.utils.MetaformUtils
import java.security.PublicKey
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class ReplyTranslator {

    @Inject
    lateinit var cryptoController: CryptoController

    @Inject
    lateinit var fieldController: FieldController

    /**
     * Translates JPA reply object into REST reply object
     *
     * @param metaformEntity Metaform entity
     * @param reply JPA reply object
     * @param ownerKey reply owner public key
     * @return REST reply
     */
    fun translate(metaformEntity: Metaform, reply: fi.metatavu.metaform.server.persistence.model.Reply, ownerKey: PublicKey?): Reply {
        val replyFieldMap = fieldController.getReplyFieldMap(reply)
        val replyData = mutableMapOf<String, Any>()

        MetaformUtils.getMetaformFields(metaformEntity)
                .mapNotNull(MetaformField::name)
                .forEach { fieldName ->
                    val value = fieldController.getFieldValue(metaformEntity, reply, fieldName, replyFieldMap)
                    if (value != null) {
                        replyData[fieldName] = value
                    }
                }

        return Reply(
                id = reply.id,
                data = replyData,
                userId = reply.userId,
                revision = reply.revision,
                createdAt = reply.createdAt,
                modifiedAt = reply.modifiedAt,
                ownerKey = cryptoController.getPublicKeyBase64(ownerKey),
                lastModifierId = reply.lastModifierId
        )
    }
}