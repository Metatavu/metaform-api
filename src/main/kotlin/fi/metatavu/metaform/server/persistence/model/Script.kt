package fi.metatavu.metaform.server.persistence.model

import fi.metatavu.metaform.api.spec.model.ScriptType
import java.util.*
import jakarta.persistence.*
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

/**
 * JPA entity representing a script
 */
@Entity
@Cacheable(true)
class Script: Metadata() {
    @Id
    var id: UUID? = null

    @Column(nullable = false)
    @NotNull
    @NotEmpty
    lateinit var name: String

    @Column(nullable = false)
    @NotNull
    @NotEmpty
    lateinit var language: String

    @Column(nullable = false)
    @NotNull
    @Enumerated(EnumType.STRING)
    lateinit var scriptType: ScriptType

    @Lob
    @Column(nullable = false)
    @NotNull
    @NotEmpty
    lateinit var content: String

    @Column(nullable = false)
    lateinit var creatorId: UUID

    @Column(nullable = false)
    lateinit var lastModifierId: UUID
}