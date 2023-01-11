package fi.metatavu.metaform.server.persistence.model

import java.time.OffsetDateTime
import java.util.*
import javax.persistence.*

/**
 * JPA entity represeting MetaformReplyViewed View
 */
@Entity
class MetaformReplyViewed {

    @ManyToOne(optional = false)
    lateinit var metaform: Metaform

    @Column(nullable = false)
    lateinit var replyViewed: OffsetDateTime

    @Id
    @Column(nullable = false)
    lateinit var replyId: UUID

}