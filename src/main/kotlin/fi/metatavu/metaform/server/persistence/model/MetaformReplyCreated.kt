package fi.metatavu.metaform.server.persistence.model

import java.time.OffsetDateTime
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.PrePersist

@Entity
class MetaformReplyCreated {

    @Id
    var id: UUID? = null

    @ManyToOne(optional = false)
    lateinit var metaform: Metaform

    @Column(nullable = false)
    lateinit var replyCreated: OffsetDateTime

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