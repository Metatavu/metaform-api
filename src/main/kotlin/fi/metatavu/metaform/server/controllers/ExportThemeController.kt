package fi.metatavu.metaform.server.controllers

import fi.metatavu.metaform.server.persistence.dao.ExportThemeDAO
import fi.metatavu.metaform.server.persistence.dao.ExportThemeFileDAO
import fi.metatavu.metaform.server.persistence.model.ExportTheme
import fi.metatavu.metaform.server.persistence.model.ExportThemeFile
import org.apache.commons.lang3.ArrayUtils
import org.apache.commons.lang3.StringUtils
import java.util.*
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.io.InputStream

/**
 * Controller for export theme related operations
 *
 * @author Antti Leppä
 */
@ApplicationScoped
class ExportThemeController {
    @Inject
    lateinit var exportThemeDAO: ExportThemeDAO

    @Inject
    lateinit var exportThemeFileDAO: ExportThemeFileDAO

    /**
     * Creates new export theme
     *
     * @param locales locales string
     * @param parent parent theme
     * @param name theme name
     * @param userId user Id
     * @return created export theme
     */
    fun createExportTheme(
        locales: String?,
        parent: ExportTheme?,
        name: String,
        userId: UUID
    ): ExportTheme {
        return exportThemeDAO.create(
            UUID.randomUUID(),
            locales,
            parent,
            name,
            userId,
            userId
        )
    }

    /**
     * Update export theme
     *
     * @param exportTheme export theme
     * @param locales locales string
     * @param parent parent theme
     * @param name theme name
     * @param userId user Id
     * @return updated export theme
     */
    fun updateExportTheme(
            exportTheme: ExportTheme,
            locales: String?,
            parent: ExportTheme?,
            name: String,
            userId: UUID
    ): ExportTheme {
        exportThemeDAO.updateLocales(exportTheme, locales, userId)
        exportThemeDAO.updateName(exportTheme, name, userId)
        exportThemeDAO.updateParent(exportTheme, parent, userId)
        return exportTheme
    }

    /**
     * Finds a export theme by id
     *
     * @param id id
     * @return found export theme or null if not found
     */
    fun findExportTheme(id: UUID): ExportTheme? {
        return exportThemeDAO.findById(id)
    }

    /**
     * Finds a export theme by name
     *
     * @param name name
     * @return found export theme or null if not found
     */
    fun findExportTheme(name: String): ExportTheme? {
        return exportThemeDAO.findByName(name)
    }

    /**
     * List export themes
     *
     * @return export themes
     */
    fun listExportThemes(): List<ExportTheme> {
        return exportThemeDAO.list()
    }

    /**
     * Deletes an export theme
     *
     * @param theme theme to be deleted
     */
    fun deleteTheme(theme: ExportTheme) {
        exportThemeDAO.delete(theme)
    }

    /**
     * Deletes an export theme file
     *
     * @param themeFile file
     */
    fun deleteThemeFile(themeFile: ExportThemeFile) {
        exportThemeFileDAO.delete(themeFile)
    }

    /**
     * Creates new theme file
     *
     * @param theme theme
     * @param path file path
     * @param content file contents
     * @param userId user Id
     * @return created file
     */
    fun createExportThemeFile(theme: ExportTheme, path: String, content: String, userId: UUID): ExportThemeFile {
        return exportThemeFileDAO.create(
                id = UUID.randomUUID(),
                theme = theme,
                path = path,
                content = content,
                creator = userId
        )
    }

    /**
     * Finds theme file by id
     *
     * @param id id
     * @return found file or null if not found
     */
    fun findExportThemeFile(id: UUID): ExportThemeFile? {
        return exportThemeFileDAO.findById(id)
    }

    /**
     * Finds theme file by full path
     *
     * @param path path including theme
     * @return found file or null if not found
     */
    fun findExportThemeFile(path: String?): ExportThemeFile? {
        val parts = StringUtils.split(path, '/')
        if (parts.size < 2) {
            return null
        }
        val exportTheme = findExportTheme(parts[0]) ?: return null
        val filePath = StringUtils.join(ArrayUtils.remove(parts, 0), "/")
        val result = findExportThemeFile(exportTheme, filePath)
        return if (result == null && exportTheme.parent != null) {
            findExportThemeFile(String.format("%s/%s", exportTheme.parent!!.name, filePath))
        } else result
    }

    /**
     * Finds theme file by theme name and path
     *
     * @param themeName theme name
     * @param path path within theme
     * @return found file or null if not found
     */
    fun findExportThemeFile(themeName: String, path: String): ExportThemeFile? {
        val exportTheme = findExportTheme(themeName) ?: return null
        return findExportThemeFile(exportTheme, path)
    }

    /**
     * Finds theme file by theme and path
     *
     * @param theme theme
     * @param path path within theme
     * @return found file or null if not found
     */
    fun findExportThemeFile(theme: ExportTheme, path: String): ExportThemeFile? {
        return exportThemeFileDAO.findByThemeAndPath(theme, path)
    }

    /**
     * Finds theme file from JAR path
     *
     * @param path Path to theme file
     * @return InputStream
     */
    fun findBaseThemeWithinJar(path: String): InputStream? {
        println("Loading resource export-themes/$path")
        val resource = this.javaClass.classLoader.getResourceAsStream("export-themes/$path")
        if (resource != null) {
            println("Loaded resource ${resource.available()}")
        }
        return resource
    }

    /**
     * Lists theme files
     *
     * @param theme thme
     * @return theme files
     */
    fun listExportThemeFiles(theme: ExportTheme): List<ExportThemeFile> {
        return exportThemeFileDAO.listByTheme(theme)
    }

    /**
     * Updates theme file
     *
     * @param exportThemeFile theme file
     * @param path path
     * @param content content
     * @param userId user Id
     * @return updated theme file
     */
    fun updateExportThemeFile(
            exportThemeFile: ExportThemeFile,
            path: String,
            content: String,
            userId: UUID
    ): ExportThemeFile {
        exportThemeFileDAO.updatePath(exportThemeFile, path, userId)
        exportThemeFileDAO.updateContent(exportThemeFile, content, userId)
        return exportThemeFile
    }
}