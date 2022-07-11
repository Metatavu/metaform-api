package fi.metatavu.metaform.server.persistence.model

import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull

/**
 * JPA entity representing reply draft
 *
 * @author Antti Leppä
 */
@Entity
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
class Draft : Metadata() {

  @Id
  var id: UUID? = null

  @Column(nullable = false)
  @NotNull
  var userId: UUID? = null

  @ManyToOne(optional = false)
  var metaform: Metaform? = null

  @Lob
  @Column(nullable = false)
  @NotNull
  var data: String? = null
}