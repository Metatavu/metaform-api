package fi.metatavu.metaform.server.script

import java.util.*
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class EncodingServices {
    /**
     * Base64 encodes data
     *
     * @param data data to encode
     */
    fun base64Encode(data: ByteArray): String {
        return Base64.getEncoder().encodeToString(data)
    }
}