package fi.metatavu.metaform.persistence.model

import java.time.OffsetDateTime
import java.util.*
import javax.persistence.Column
import javax.persistence.MappedSuperclass
import javax.persistence.PrePersist
import javax.persistence.PreUpdate

/**
 * Class containing shared general properties of entities
 */
@MappedSuperclass
abstract class Metadata {

  @Column(nullable = false)
  open var createdAt: OffsetDateTime? = null

  @Column(nullable = false)
  open var modifiedAt: OffsetDateTime? = null

  abstract var creatorId: UUID?

  abstract var lastModifierId: UUID?

  /**
   * JPA pre-persist event handler
   */
  @PrePersist
  fun onCreate() {
    createdAt = OffsetDateTime.now()
    modifiedAt = OffsetDateTime.now()
  }

  /**
   * JPA pre-update event handler
   */
  @PreUpdate
  fun onUpdate() {
    modifiedAt = OffsetDateTime.now()
  }

}