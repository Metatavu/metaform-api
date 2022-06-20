package fi.metatavu.metaform.persistence.model.notifications

import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.util.*
import javax.persistence.Cacheable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

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
  var email: String? = null
}