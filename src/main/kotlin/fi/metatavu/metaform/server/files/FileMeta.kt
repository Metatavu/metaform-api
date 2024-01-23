package fi.metatavu.metaform.server.files

import com.fasterxml.jackson.annotation.JsonProperty
import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
data class FileMeta (
        @JsonProperty("contentType")
        val contentType: String,
        @JsonProperty("fileName")
        val fileName: String
)