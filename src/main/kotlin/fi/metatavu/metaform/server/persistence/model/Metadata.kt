package fi.metatavu.metaform.server.persistence.model

import java.time.OffsetDateTime
import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate

/**
 * Class containing shared general properties of entities
 */
@MappedSuperclass
abstract class Metadata {

  @Column(nullable = false)
  open var createdAt: OffsetDateTime? = null

  @Column(nullable = false)
  open var modifiedAt: OffsetDateTime? = null

  /**
   * JPA pre-persist event handler
   */
  @PrePersist
  fun onCreate() {
    val odtNow = OffsetDateTime.now()
    createdAt = odtNow
    modifiedAt = odtNow
  }

  /**
   * JPA pre-update event handler
   */
  @PreUpdate
  fun onUpdate() {
    modifiedAt = OffsetDateTime.now()
  }

}