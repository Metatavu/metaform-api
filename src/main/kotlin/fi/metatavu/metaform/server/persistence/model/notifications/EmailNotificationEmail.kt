package fi.metatavu.metaform.server.persistence.model.notifications

import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.util.*
import jakarta.persistence.Cacheable
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

/**
 * JPA entity representing email notification email
 *
 * @author Antti Leppä
 */
@Entity
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
class EmailNotificationEmail {
  @Id
  var id: UUID? = null

  @ManyToOne(optional = false)
  var emailNotification: EmailNotification? = null

  @Column
  @NotNull
  @NotEmpty
  @Email
  lateinit var email: String
}