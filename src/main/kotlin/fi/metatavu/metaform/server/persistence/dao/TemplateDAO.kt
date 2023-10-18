package fi.metatavu.metaform.server.persistence.dao

import fi.metatavu.metaform.api.spec.model.TemplateVisibility
import fi.metatavu.metaform.server.persistence.model.Template
import fi.metatavu.metaform.server.persistence.model.Template_
import java.util.UUID
import javax.enterprise.context.ApplicationScoped
import javax.persistence.TypedQuery
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery

/**
 * DAO class for template entity
 *
 * @author Harri Häkkinen
 */
@ApplicationScoped
class TemplateDAO  : AbstractDAO<Template>() {
    /**
    * Creates new template
    *
    * @param id id
    * @param data data
    * @return created Template
    */
    fun create(
      id: UUID,
      data: String,
      visibility: TemplateVisibility,
      creatorId: UUID,
      lastModifierId: UUID
    ): Template {
        val template = Template()
        template.id = id
        template.data = data
        template.visibility = visibility
        template.creatorId = creatorId
        template.lastModifierId = lastModifierId
        return persist(template)
    }

    /**
     * Updates data
     *
     * @param template template
     * @param data data
     * @param templateVisibility templateVisibility
     * @param lastModifier lastModifier UUID
     * @return updated template
     */
    fun updateData(
            template: Template,
            data: String?,
            templateVisibility: TemplateVisibility,
            lastModifier: UUID
    ): Template {
        template.data = data
        template.visibility = templateVisibility
        template.lastModifierId = lastModifier
        return persist(template)
    }

    /**
     * Lists templates
     *
     * @return list of templates
     */
    fun listTemplates(): List<Template> {
        val criteriaBuilder: CriteriaBuilder = entityManager.criteriaBuilder
        val criteria: CriteriaQuery<Template>? = criteriaBuilder.createQuery(
          Template::class.java
        )

        /*
        val root = criteria.from(Template::class.java)
        criteria.select(root)
        criteria.where(criteriaBuilder.equal(root.get()))
        */

        val query: TypedQuery<Template> = entityManager.createQuery(criteria)
        return query.resultList
    }

    /**
     * Lists metaform by visibility
     *
     * @param visibility visibility
     * @return list of Metaforms
     */
    fun listByVisibility(visibility: TemplateVisibility): List<Template> {
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteria = criteriaBuilder.createQuery(
                Template::class.java
        )
        val root = criteria.from(
                Template::class.java
        )
        criteria.select(root)
        criteria.where(
                criteriaBuilder.equal(root.get(Template_.visibility), visibility)
        )
        return entityManager.createQuery(criteria).resultList
    }
}