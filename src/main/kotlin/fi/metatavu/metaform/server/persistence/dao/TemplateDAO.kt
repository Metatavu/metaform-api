package fi.metatavu.metaform.server.persistence.dao

import fi.metatavu.metaform.api.spec.model.TemplateVisibility
import fi.metatavu.metaform.server.persistence.model.Template
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
      visibility: TemplateVisibility
    ): Template {
        val template = Template()
        template.id = id
        template.data = data
        template.visibility = visibility
        return persist(template)
    }

    /**
     * Updates data
     *
     * @param template template
     * @param data data
     * @return updated template
     */
    fun updateData(template: Template, data: String?): Template {
        template.data = data
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
}