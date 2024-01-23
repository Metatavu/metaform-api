package fi.metatavu.metaform.server.persistence.dao

import fi.metatavu.metaform.api.spec.model.TemplateVisibility
import fi.metatavu.metaform.server.persistence.model.Template
import fi.metatavu.metaform.server.persistence.model.Template_
import java.util.ArrayList
import java.util.UUID
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.TypedQuery
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate

/**
 * DAO class for template entity
 *
 * @author Harri Häkkinen
 */
@ApplicationScoped
class TemplateDAO : AbstractDAO<Template>() {
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
     * @param lastModifier lastModifier UUID
     * @return updated template
     */
    fun updateData(
            template: Template,
            data: String,
            lastModifier: UUID
    ): Template {
        template.data = data
        template.lastModifierId = lastModifier
        return persist(template)
    }

    /**
     * Updates template visibility
     *
     * @param template template
     * @param templateVisibility template visibility
     * @param lastModifierId last modifier UUID
     */
    fun updateVisibility(
            template: Template,
            templateVisibility: TemplateVisibility,
            lastModifierId: UUID
    ): Template {
        template.visibility = templateVisibility
        template.lastModifierId = lastModifierId
        return persist(template)
    }

    /**
     * Lists templates
     *
     * @param visibility template visibility
     * @return list of templates
     */
    fun list(visibility: TemplateVisibility?): List<Template> {
        val criteriaBuilder: CriteriaBuilder = entityManager.criteriaBuilder
        val criteria: CriteriaQuery<Template> = criteriaBuilder.createQuery(Template::class.java)
        val root = criteria.from(Template::class.java)
        criteria.select(root)

        val restrictions: MutableList<Predicate> = ArrayList()

        visibility?.let {
            restrictions.add(criteriaBuilder.equal(root.get(Template_.visibility), visibility))
        }

        criteria.where(criteriaBuilder.and(*restrictions.toTypedArray()))

        val query: TypedQuery<Template> = entityManager.createQuery(criteria)
        return query.resultList
    }
}