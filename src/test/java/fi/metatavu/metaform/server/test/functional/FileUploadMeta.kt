package fi.metatavu.metaform.server.test.functional

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Model for uploaded file meta
 *
 * @author Antti Leppä
 */
class FileUploadMeta (
    @JsonProperty("contentType")
    val contentType: String,
    @JsonProperty("fileName")
    val fileName: String
)