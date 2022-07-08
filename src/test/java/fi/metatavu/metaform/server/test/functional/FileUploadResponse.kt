package fi.metatavu.metaform.server.test.functional

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

/**
 * Model for file upload response
 *
 * @author Antti Leppä
 */
data class FileUploadResponse (
    @JsonProperty("fileRef")
    val fileRef: UUID,
    @JsonProperty("fileName")
    val fileName: String
)