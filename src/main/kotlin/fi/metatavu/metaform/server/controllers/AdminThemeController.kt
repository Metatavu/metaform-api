package fi.metatavu.metaform.server.controllers

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.slugify.Slugify
import fi.metatavu.metaform.server.exceptions.MalformedDraftDataException
import fi.metatavu.metaform.server.persistence.dao.AdminThemeDAO
import fi.metatavu.metaform.server.persistence.model.AdminTheme
import java.util.UUID
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import org.apache.commons.lang3.StringUtils

/**
 * Controller for admin themes
 */
@ApplicationScoped
class AdminThemeController {
    @Inject
    lateinit var adminThemeDAO: AdminThemeDAO

    @Inject
    lateinit var objectMapper: ObjectMapper

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
        slug: String? = null,
        userId: UUID
    ): AdminTheme {
        return adminThemeDAO.create(
            id, 
            data, 
            name, 
            slug = slug ?: createSlug(name),
            userId,
            userId
        )
    }

    /**
     * Finds an admin theme by id
     *
     * @param id the id of the admin theme
     * 
     * @return the located admin theme or null
     */
    fun findById(id: UUID): AdminTheme? {
        return adminThemeDAO.findById(id)
    }

    /**
     * Generates unique slug within a realm for an Admin theme
     *
     * @param name name
     * @return unique slug
     */
    private fun createSlug(name: String?): String {
        val slugify = Slugify()
        val prefix = if (StringUtils.isNotBlank(name)) slugify.slugify(name) else "theme"
        var count = 0
        do {
            val slug = if (count > 0) String.format("%s-%d", prefix, count) else prefix

            adminThemeDAO.findBySlug(slug) ?: return slug
            count++
        } while (true)
    }

    /**
     * Finds an admin theme by slug
     * 
     * @param slug the slug of the admin theme
     * 
     * @return the located admin theme or null
     */
    fun findBySlug(slug: String): AdminTheme? {
        return adminThemeDAO.findById(slug)
    }
    
    /**
     * Unique check for metaform slug
     *
     * @param slug slug
     * @return boolean result for unique check
     */
    fun isSlugUnique(themeId: UUID?, slug: String): Boolean {
        val foundAdminTheme = adminThemeDAO.findBySlug(slug)
        return foundAdminTheme == null || foundAdminTheme.id === themeId
    }

    /**
     * Validate a slug for Metaform
     *
     * @param slug slug
     * @return boolean result for validation
     */
    fun validateSlug(slug: String): Boolean {
        return slug.matches(Regex("^[a-z\\d]+(?:[-, _][a-z\\d]+)*$"))
    }

    /**
     * Update an admin theme
     * 
     * @param theme the theme to update
     * @param data the data to update
     * @param name the name to update
     * @param slug the slug to update
     * 
     * @return updated admin theme
     */
    fun updateAdminTheme(
        adminTheme: AdminTheme,
        data: String,
        name: String,
        slug: String?,
    ): AdminTheme { 
        return adminThemeDAO.update(
            adminTheme,
            data,
            name,
            slug ?: adminTheme.slug
        )
    }

    /**
     * Serializes data as string
     *
     * @param data data
     * @return data as string
     */
    @Throws(MalformedDraftDataException::class)
    fun serializeData(data: Map<String, Any>): String {
        try {
            val objectMapper = ObjectMapper()
            return objectMapper.writeValueAsString(data)
        } catch (e: JsonProcessingException) {
            throw MalformedDraftDataException("Failed to serialize draft data", e)
        }
    }
}