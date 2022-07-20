package fi.metatavu.metaform.server.persistence.model

import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy

/**
 * Class representing an admin theme
 */
@Entity
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Table
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