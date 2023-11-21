package fi.metatavu.metaform.server.persistence.model

import java.util.*
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Lob
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

/**
 * Class containing shared general properties of entities
 */
@Entity
class Attachment : Metadata() {

  @Id
  var id: UUID? = null

  @Column(nullable = false)
  @NotNull
  lateinit var userId: UUID

  @Column(nullable = false)
  @NotEmpty
  @NotNull
  lateinit var name: String

  @Column(nullable = false)
  @NotEmpty
  @NotNull
  lateinit var contentType: String

  @Column(nullable = false)
  @Lob
  /*@Type(type = "org.hibernate.type.BinaryType")*/
  lateinit var content: ByteArray

}