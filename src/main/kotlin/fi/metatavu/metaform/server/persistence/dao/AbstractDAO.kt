package fi.metatavu.metaform.server.persistence.dao

import org.slf4j.Logger
import java.lang.reflect.ParameterizedType
import java.util.*
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.persistence.Query
import jakarta.persistence.TypedQuery

/**
 * Abstract base class for all DAO classes
 *
 * @author Jari Nykänen
 *
 * @param <T> entity type
 */
abstract class AbstractDAO<T> {

    @Inject
    open lateinit var logger: Logger

    @PersistenceContext
    lateinit var entityManager: EntityManager


    /**
     * Returns entity by id
     *
     * @param id entity id
     * @return entity or null if non found
     */
    open fun findById(id: UUID): T? {
        return entityManager.find(genericTypeClass, id)
    }

    /**
     * Returns entity by id
     *
     * @param id entity id
     * @return entity or null if non found
     */
    open fun findById(id: Long): T? {
        return entityManager.find(genericTypeClass, id)
    }

    /**
     * Returns entity by id
     *
     * @param id entity id
     * @return entity or null if non found
     */
    open fun findById(id: String): T? {
        return entityManager.find(genericTypeClass, id)
    }

    /**
     * Lists all entities from database
     *
     * @return all entities from database
     */
    @Suppress("UNCHECKED_CAST")
    open fun listAll(): List<T> {
        val genericTypeClass: Class<*>? = genericTypeClass
        val query: Query = entityManager.createQuery("select o from " + genericTypeClass!!.name + " o")
        return query.resultList as List<T>
    }

    /**
     * Lists all entities from database limited by firstResult and maxResults parameters
     *
     * @param firstResult first result
     * @param maxResults max results
     * @return all entities from database limited by firstResult and maxResults parameters
     */
    @Suppress("UNCHECKED_CAST")
    open fun listAll(firstResult: Int, maxResults: Int): List<T> {
        val genericTypeClass: Class<*>? = genericTypeClass
        val query: Query = entityManager.createQuery("select o from " + genericTypeClass!!.name + " o")
        query.firstResult = firstResult
        query.maxResults = maxResults
        return query.resultList as List<T>
    }

    /**
     * Deletes entity
     *
     * @param e entity
     */
    open fun delete(e: T) {
        entityManager.remove(e)
        flush()
    }

    /**
     * Flushes persistence context state
     */
    open fun flush() {
        entityManager.flush()
    }

    /**
     * Persists an entity
     *
     * @param object entity to be persisted
     * @return persisted entity
     */
    protected open fun persist(`object`: T): T {
        entityManager.persist(`object`)
        return `object`
    }

    /**
     * Returns single result entity or null if result is empty
     *
     * @param query query
     * @return entity or null if result is empty
     */
    protected open fun <X> getSingleResult(query: TypedQuery<X>): X? {
        val list: List<X> = query.resultList
        if (list.isEmpty()) return null
        if (list.size > 1) {
            logger.error(String.format("SingleResult query returned %d elements from %s", list.size, genericTypeClass!!.name))
        }
        return list[list.size - 1]
    }

    /**
     * Returns DAO class
     *
     * @returns DAO class
     */
    private val genericTypeClass: Class<T>?
        get() {
            val genericSuperclass = javaClass.genericSuperclass
            if (genericSuperclass is ParameterizedType) {
                return getFirstTypeArgument(genericSuperclass)
            } else {
                if (genericSuperclass is Class<*> && AbstractDAO::class.java.isAssignableFrom(genericSuperclass)) {
                    return getFirstTypeArgument(genericSuperclass.genericSuperclass as ParameterizedType)
                }
            }
            return null
        }

    /**
     * Returns first type argument for parameterized type
     *
     * @return first type argument for parameterized type
     */
    @Suppress("UNCHECKED_CAST")
    private fun getFirstTypeArgument(parameterizedType: ParameterizedType): Class<T> {
        return parameterizedType.actualTypeArguments[0] as Class<T>
    }

}