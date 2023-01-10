package fi.metatavu.metaform.server.persistence.model

import java.time.OffsetDateTime
import java.util.*
import javax.persistence.*

@Entity
class MetaformReplyViewed {

    @Id
    var id: UUID? = null

    @ManyToOne(optional = false)
    lateinit var metaform: Metaform

    @Column(nullable = false)
    lateinit var replyViewed: OffsetDateTime

    @ManyToOne(optional = false)
    lateinit var reply: Reply

    /**
     * JPA Pre-Persist handler
     */
    @PrePersist
    fun onCreate() {
        id = UUID.randomUUID()
    }
}