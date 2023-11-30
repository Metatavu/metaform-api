package fi.metatavu.metaform.server.rest

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.reflect.Type
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.MultivaluedMap
import jakarta.ws.rs.ext.MessageBodyReader
import jakarta.ws.rs.ext.Provider

@Provider
@Consumes("*/*")
class WildcardRequestBodyReader : MessageBodyReader<String?> {

    override fun isReadable(
        type: Class<*>?,
        genericType: Type?,
        annotations: Array<Annotation>?,
        mediaType: MediaType?
    ): Boolean {
        return true
    }

    @Throws(IOException::class, WebApplicationException::class)
    override fun readFrom(
        type: Class<String?>,
        genericType: Type?,
        annotations: Array<Annotation>?,
        mediaType: MediaType?,
        httpHeaders: MultivaluedMap<String, String>,
        entityStream: InputStream?
    ): String? {
        entityStream?.let { InputStreamReader(it) }?.let {
            BufferedReader(it).use { br ->
                return br.readLine()
            }
        }
        return null
    }
}