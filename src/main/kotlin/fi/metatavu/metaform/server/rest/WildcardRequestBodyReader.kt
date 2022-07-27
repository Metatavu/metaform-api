package fi.metatavu.metaform.server.rest

import java.io.IOException
import java.io.InputStream
import javax.ws.rs.Consumes
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.ext.MessageBodyReader
import javax.ws.rs.ext.Provider
import java.lang.reflect.Type

@Provider
@Consumes("*/*")
class MyRequestBodyReader : MessageBodyReader<String> {
    override fun isReadable(
        type: Class<*>,
        genericType: Type?,
        annotations: Array<Annotation?>?,
        mediaType: MediaType?
    ): Boolean {
        return type == String::class.java
    }

    @Throws(IOException::class, WebApplicationException::class)
    override fun readFrom(
        type: Class<String>,
        genericType: Type,
        annotations: Array<Annotation>,
        mediaType: MediaType?,
        httpHeaders: MultivaluedMap<String, String>,
        entityStream: InputStream?
    ): String {
        println("unmarshalling")
        return entityStream.toString()
    }
}