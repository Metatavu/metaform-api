package fi.metatavu.metaform.server.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import fi.metatavu.metaform.server.exceptions.FailStoreFailedException
import fi.metatavu.metaform.server.files.File
import fi.metatavu.metaform.server.files.FileMeta
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.slf4j.Logger
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * Controller for Files
 */
@ApplicationScoped
class FileController {

    @Inject
    lateinit var logger: Logger

    @Inject
    @ConfigProperty(name = "metaform.uploads.folder")
    lateinit var filesDir: String

    /**
     * Stores file and returns reference id
     *
     * @param inputStream input stream
     * @return reference id
     */
    fun storeFile(
        contentType: String,
        fileName: String,
        inputStream: InputStream
    ): String {
        val fileRef = UUID.randomUUID().toString()
        try {
            persistFile(Path.of(getDataDir().toString(), fileRef), IOUtils.toByteArray(inputStream))
            persistFile(Path.of(getMetaDir().toString(), fileRef), getObjectMapper().writeValueAsString(FileMeta(contentType, fileName)).toByteArray())
            return fileRef
        } catch (e: IOException) {
            throw FailStoreFailedException("Failed to store file", e)
        }
    }

    /**
     * Returns a file data
     *
     * @param fileRef file reference id
     * @return file data or null if not found
     */
    fun getFileData(fileRef: String): File? {
        if (StringUtils.isEmpty(fileRef)) {
            return null
        }
        val dataPath = Path.of(getDataDir().toString(), fileRef)
        val metaPath = Path.of(getMetaDir().toString(), fileRef)
        if (Files.notExists(dataPath) || Files.notExists(metaPath)) {
            return null
        }
        val data = readFileData(dataPath)
        val metaData = String(readFileData(metaPath)!!)
        return createFile(fileRef, data, metaData)
    }

    /**
     * Returns meta data for a file or null if file does not exist
     *
     * @param fileRef file reference id
     * @return meta data
     */
    fun getFileMeta(fileRef: String): FileMeta? {
        val metaData = getRawFileMeta(fileRef)
        if (StringUtils.isBlank(metaData)) {
            return null
        }
        try {
            return getObjectMapper().readValue(metaData, FileMeta::class.java)
        } catch (e: IOException) {
            logger.error("Failed to retrieve file meta", e)
        }
        return null
    }

    /**
     * Returns raw meta data for a file or null if file does not exist
     *
     * @param fileRef file reference id
     * @return meta data as JSON string
     */
    fun getRawFileMeta(fileRef: String): String? {
        try {
            val bytes = readFileData(Path.of(getMetaDir().toString(), fileRef))
            if (bytes != null) {
                return IOUtils.toString(bytes, "UTF8")
            }
        } catch (e: IOException) {
            logger.error(e.message)
        }
        return null
    }

    /**
     * Returns a file data and removes it from the store
     *
     * @param fileRef file reference id
     * @return file data or null if not found
     */
    fun popFileData(fileRef: String): File? {
        if (StringUtils.isEmpty(fileRef)) {
            return null
        }
        val fileData = getFileData(fileRef)
        deleteFile(fileRef)
        return fileData
    }

    /**
     * Deletes file from store
     *
     * @param fileRef file ref
     */
    fun deleteFile(fileRef: String) {
        try {
            Files.delete(Path.of(getDataDir().toString(), fileRef))
            Files.delete(Path.of(getMetaDir().toString(), fileRef))
        } catch (e: IOException) {
            logger.error(e.message)
        }
    }

    /**
     * Creates file from data and metadata
     *
     * @param fileRef file ref
     * @param data byte array data
     * @param metaData metadata
     * @return new file
     */
    private fun createFile(fileRef: String, data: ByteArray?, metaData: String?): File? {
        if (data != null && metaData != null) {
            try {
                val fileMeta = getObjectMapper().readValue(metaData, FileMeta::class.java)
                return File(fileMeta, data)
            } catch (e: IOException) {
                logger.error(String.format("Failed to deserialize file meta for fileRef %s", fileRef), e)
            }
        }
        return null
    }

    private fun getObjectMapper(): ObjectMapper {
        return ObjectMapper()
    }


    /**
     * Persists file
     *
     * @param path path
     * @param data data
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun persistFile(path: Path, data: ByteArray) {
        val dataFile = Files.createFile(path)
        FileOutputStream(dataFile.toFile()).use { outputStream -> outputStream.write(data) }
    }

    /**
     * Reads file into byte array
     *
     * @param path path to file
     * @return file data
     */
    private fun readFileData(path: Path): ByteArray? {
        if (Files.exists(path)) {
            try {
                FileInputStream(path.toFile()).use { fileInputStream -> return IOUtils.toByteArray(fileInputStream) }
            } catch (e: Exception) {
                logger.error(e.message)
            }
        }
        return null
    }

    /**
     * Gets the data dir path
     *
     * @return data dir path
     */
    private fun getDataDir(): Path {
        val data = Path.of(filesDir, "data").toFile()
        if (!data.exists()) {
            data.mkdir()
        }
        return Path.of(data.absolutePath)
    }

    /**
     * Gets meta dir path
     *
     * @return meta dir path
     */
    private fun getMetaDir(): Path {
        val meta = Path.of(filesDir, "meta").toFile()
        if (!meta.exists()) {
            meta.mkdir()
        }
        return Path.of(meta.absolutePath)
    }

}