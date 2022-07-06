package fi.metatavu.metaform.server.persistence.model

import org.hibernate.annotations.Type
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Lob
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

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
  @Type(type = "org.hibernate.type.BinaryType")
  lateinit var content: ByteArray

}