package fi.metatavu.metaform.server.persistence.model

import fi.metatavu.metaform.api.spec.model.TemplateVisibility
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

/**
 * JPA entity representing Metaform Template
 *
 * @author Harri Häkkinen
 */
@Entity
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
class Template : Metadata() {
    @Id
    var id: UUID? = null

    @Lob
    @Column(nullable = false)
    @NotNull
    @NotEmpty
    var data: String? = null

    @NotNull
    @Enumerated(EnumType.STRING)
    var visibility: TemplateVisibility? = null

    @Column(nullable = false)
    lateinit var creatorId: UUID

    @Column(nullable = false)
    lateinit var lastModifierId: UUID
}