package fi.metatavu.metaform.server.persistence.dao

import fi.metatavu.metaform.server.persistence.model.AdminTheme
import fi.metatavu.metaform.server.persistence.model.AdminTheme_
import java.util.UUID
import javax.enterprise.context.ApplicationScoped
import javax.persistence.EntityManager
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery

@ApplicationScoped
class AdminThemeDAO : AbstractDAO<AdminTheme>() {
    /**
     * Creates a new admin theme
     *
     * @param id the id of the theme
     * @param data the data in the theme
     * @param name the name of the theme
     * @param slug the slug of the theme
     * @param creatorId who created this theme
     * @param lastModifierId who last modified this theme
     *
     * @return new [AdminTheme] object
     */
    fun create(
        id: UUID,
        data: String,
        name: String,
        slug: String,
        creatorId: UUID,
        lastModifierId: UUID
    ): AdminTheme {
        val adminTheme = AdminTheme()
        adminTheme.id = id
        adminTheme.data = data
        adminTheme.name = name
        adminTheme.slug = slug
        adminTheme.creatorId = creatorId
        adminTheme.lastModifierId = lastModifierId

        return persist(adminTheme)
    }

    /**
     * Updates an admin theme
     * 
     * @param adminTheme to update
     * @param data to update with
     * @param name to update to
     * @param slug to update to
     * 
     * @return updated admin theme
     */
    fun update(
        adminTheme: AdminTheme,
        data: String,
        name: String,
        slug: String
        ): AdminTheme {
        adminTheme.data = data
        adminTheme.name = name
        adminTheme.slug = slug
        return persist(adminTheme)
    }

    /**
     * Finds admin theme by slug
     * 
     * @param slug to find
     * 
     * @return admin theme or null if not found
     */
    fun findBySlug(slug: String): AdminTheme? {
        val criteriaBuilder: CriteriaBuilder = entityManager.criteriaBuilder
        val criteria: CriteriaQuery<AdminTheme> = criteriaBuilder.createQuery<AdminTheme>(
            AdminTheme::class.java
        )
        val root = criteria.from(
            AdminTheme::class.java
        )
        criteria.select(root)
        criteria.where(criteriaBuilder.equal(root.get(AdminTheme_.slug), slug))
        return getSingleResult<AdminTheme>(entityManager.createQuery(criteria))
    }
}