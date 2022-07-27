package fi.metatavu.metaform.server.rest

import org.slf4j.Logger
import java.io.IOException
import java.io.InputStream
import javax.ws.rs.Consumes
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.ext.MessageBodyReader
import javax.ws.rs.ext.Provider
import java.lang.reflect.Type
import javax.inject.Inject

@Provider
@Consumes("*/*")
class MyRequestBodyReader : MessageBodyReader<String> {

    @Inject
    lateinit var logger: Logger

    override fun isReadable(
        type: Class<*>,
        genericType: Type,
        annotations: Array<Annotation>,
        mediaType: MediaType
    ): Boolean {
        logger.info("is wildcard content type readable")
        logger.info(String.format("type: %s", type.toString()))
        logger.info(String.format("genericType: %s", genericType.typeName))
        logger.info(String.format("annotations: %s", annotations.toString()))
        logger.info(String.format("mediaType.type: %s", mediaType.type))
        logger.info(String.format("mediaType.isWildcardType: %b", mediaType.isWildcardType))
        return mediaType.isWildcardType
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