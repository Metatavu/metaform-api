package fi.metatavu.metaform.server.files;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

/**
 *
 * Servlet that handles file upload requests
 * 
 * @author Antti Leppä
*/
@RequestScoped
@MultipartConfig
@WebServlet (urlPatterns = "/fileUpload")
public class FileUploadServlet extends HttpServlet {
  
  private static final String FILE_REF = "fileRef";

  private static final long serialVersionUID = 4209609403222008762L;
  
  @Inject
  private Logger logger;

  @Inject
  private FileController fileController;
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String fileRef = req.getParameter(FILE_REF);
    if (StringUtils.isBlank(fileRef)) {
      resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    setCorsHeaders(resp);
    boolean meta = "true".equalsIgnoreCase(req.getParameter("meta"));
    if (meta) {
      getFileMeta(resp, fileRef);      
    } else {
      getFileData(resp, fileRef);
    }
  }
  
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    try {
      Part file = req.getPart("file");
      if (file == null) {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }
      
      String contentType = file.getContentType();
      String fileName = file.getSubmittedFileName();
      InputStream inputStream = file.getInputStream();
      
      String fileRef = fileController.storeFile(contentType, fileName, inputStream);
      Map<String, String> result = new HashMap<>();
      result.put(FILE_REF, fileRef);
      result.put("fileName", fileName);
      setCorsHeaders(resp);
      resp.setContentType("application/json");
      ServletOutputStream servletOutputStream = resp.getOutputStream();
      try {
        (new ObjectMapper()).writeValue(servletOutputStream, result);
      } finally {
        servletOutputStream.flush();
      }
      
    } catch (IOException | ServletException e) {
      logger.error("Upload failed on internal server error", e);
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }
  
  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String fileRef = req.getParameter(FILE_REF);
    if (StringUtils.isBlank(fileRef)) {
      resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    
    fileController.deleteFile(fileRef);
    setCorsHeaders(resp);
    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
  }

/*
   * Sets CORS headers for the response
   * 
   * @param resp reponse
*/
  private void setCorsHeaders(HttpServletResponse resp) {
    resp.setHeader("Access-Control-Allow-Origin", "*");
    resp.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization");
    resp.setHeader("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE,OPTIONS");
  }

/*
   * Outputs file metadata as JSON
   * 
   * @param resp response object
   * @param fileRef file ref
*/
  private void getFileMeta(HttpServletResponse resp, String fileRef) {
    String fileMeta = fileController.getRawFileMeta(fileRef);
    if (fileMeta == null) {
      resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    
    resp.setContentType("application/json");
    
    try {
      ServletOutputStream servletOutputStream = resp.getOutputStream();
      try {
        servletOutputStream.write(fileMeta.getBytes("UTF-8"));
      } finally {
        servletOutputStream.flush();
      }
    } catch (IOException e) {
      logger.warn("Failed to send response", e);
    }    
  }

 /*
   * Outputs file data
   * 
   * @param resp response object
   * @param fileRef file ref
*/
  private void getFileData(HttpServletResponse resp, String fileRef) {
    File fileData = fileController.getFileData(fileRef);
    if (fileData == null) {
      resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    
    resp.setContentType(fileData.getMeta().getContentType());
    
    try {
      ServletOutputStream servletOutputStream = resp.getOutputStream();
      try {
        servletOutputStream.write(fileData.getData());
      } finally {
        servletOutputStream.flush();
      }
    } catch (IOException e) {
      logger.warn("Failed to send response", e);
    }
  }
  
}