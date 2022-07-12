package fi.metatavu.metaform.server.persistence.model

import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Lob
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

/**
 * Class representing an admin theme
 */
@Entity
class AdminTheme : Metadata() {
    @Id
    var id: UUID? = null

    @Column(nullable = false)
    @NotEmpty
    @NotNull
    @Lob
    lateinit var data: String

    @Column(nullable = false)
    @NotEmpty
    @NotNull
    lateinit var name: String

    @Column(nullable = false)
    @NotEmpty
    @NotNull
    lateinit var slug: String

    @Column(nullable = false)
    @NotNull
    lateinit var creatorId: UUID

    @Column(nullable = false)
    @NotNull
    lateinit var lastModifierId: UUID
}