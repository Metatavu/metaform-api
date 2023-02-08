package fi.metatavu.metaform.server.persistence.model

import java.time.OffsetDateTime
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.ManyToOne

/**
 * JPA entity represeting MetaformReplyCreated View
 */
@Entity
class MetaformReplyCreated {

    @ManyToOne(optional = false)
    lateinit var metaform: Metaform

    @Column(nullable = false)
    lateinit var replyCreated: OffsetDateTime

    @Id
    @Column(nullable = false)
    lateinit var replyId: UUID
}