package fi.metatavu.metaform.server.rest

import com.fasterxml.jackson.databind.ObjectMapper
import fi.metatavu.metaform.server.controllers.FileController
import fi.metatavu.metaform.server.exceptions.FailStoreFailedException
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import java.io.IOException
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.servlet.ServletException
import jakarta.servlet.annotation.MultipartConfig
import jakarta.servlet.annotation.WebServlet
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

/**
 * Servlet that handles file upload requests
 *
 * @author Antti Leppä
 */
@RequestScoped
@MultipartConfig
@WebServlet(name = "fileupload", urlPatterns = ["/fileUpload"])
class FileUploadServlet : HttpServlet() {
    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var fileController: FileController

    @Throws(ServletException::class, IOException::class)
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        val fileRef = req.getParameter(FILE_REF)
        if (StringUtils.isBlank(fileRef)) {
            resp.status = HttpServletResponse.SC_NOT_FOUND
            return
        }
        setCorsHeaders(resp)
        val meta = "true".equals(req.getParameter("meta"), ignoreCase = true)
        if (meta) {
            getFileMeta(resp, fileRef)
        } else {
            getFileData(resp, fileRef)
        }
    }

    @Throws(ServletException::class, IOException::class)
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        try {
            val file = req.getPart("file")
            if (file == null) {
                resp.status = HttpServletResponse.SC_BAD_REQUEST
                return
            }
            val contentType = file.contentType
            val fileName = file.submittedFileName
            val inputStream = file.inputStream
            val fileRef = try {
                fileController.storeFile(contentType, fileName, inputStream)
            } catch (e: FailStoreFailedException) {
                logger.error("Upload failed on internal server error", e)
                resp.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
                return
            }
            val result: MutableMap<String, String> = HashMap()
            result[FILE_REF] = fileRef
            result["fileName"] = fileName
            setCorsHeaders(resp)
            resp.contentType = "application/json"
            val servletOutputStream = resp.outputStream
            try {
                ObjectMapper().writeValue(servletOutputStream, result)
            } finally {
                servletOutputStream.flush()
            }
        } catch (e: IOException) {
            logger.error("Upload failed on internal server error", e)
            resp.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
        } catch (e: ServletException) {
            logger.error("Upload failed on internal server error", e)
            resp.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
        }
    }

    @Throws(ServletException::class, IOException::class)
    override fun doDelete(req: HttpServletRequest, resp: HttpServletResponse) {
        val fileRef = req.getParameter(FILE_REF)
        if (StringUtils.isBlank(fileRef)) {
            resp.status = HttpServletResponse.SC_NOT_FOUND
            return
        }
        fileController.deleteFile(fileRef)
        setCorsHeaders(resp)
        resp.status = HttpServletResponse.SC_NO_CONTENT
    }

    /*
   * Sets CORS headers for the response
   * 
   * @param resp response
*/
    private fun setCorsHeaders(resp: HttpServletResponse) {
        resp.setHeader("Access-Control-Allow-Origin", "*")
        resp.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization")
        resp.setHeader("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE,OPTIONS")
    }

    /*
   * Outputs file metadata as JSON
   * 
   * @param resp response object
   * @param fileRef file ref
*/
    private fun getFileMeta(resp: HttpServletResponse, fileRef: String) {
        val fileMeta= fileController.getRawFileMeta(fileRef)
        if (fileMeta == null) {
            resp.status = HttpServletResponse.SC_NOT_FOUND
            return
        }
        resp.contentType = "application/json"
        try {
            val servletOutputStream = resp.outputStream
            try {
                servletOutputStream.write(fileMeta.toByteArray(charset("UTF-8")))
            } finally {
                servletOutputStream.flush()
            }
        } catch (e: IOException) {
            logger.warn("Failed to send response", e)
        }
    }

    /*
   * Outputs file data
   * 
   * @param resp response object
   * @param fileRef file ref
*/
    private fun getFileData(resp: HttpServletResponse, fileRef: String) {
        val fileData = fileController.getFileData(fileRef)
        if (fileData == null) {
            resp.status = HttpServletResponse.SC_NOT_FOUND
            return
        }
        resp.contentType = fileData.meta.contentType
        try {
            val servletOutputStream = resp.outputStream
            try {
                servletOutputStream.write(fileData.data)
            } finally {
                servletOutputStream.flush()
            }
        } catch (e: IOException) {
            logger.warn("Failed to send response", e)
        }
    }

    companion object {
        private const val FILE_REF = "fileRef"
        private const val serialVersionUID = 4209609403222008762L
    }
}