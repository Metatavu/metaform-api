package fi.metatavu.metaform.server.persistence.model

import fi.metatavu.metaform.api.spec.model.ScriptType
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

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
    @NotEmpty
    @Enumerated(EnumType.STRING)
    lateinit var type: ScriptType

    @Lob
    @Column(nullable = false)
    @NotNull
    @NotEmpty
    lateinit var content: String
}