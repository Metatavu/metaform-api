package fi.metatavu.metaform.server.persistence.model

import java.time.OffsetDateTime
import java.util.*
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne

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