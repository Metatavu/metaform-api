package fi.metatavu.metaform.server.metaforms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.metaform.api.spec.model.*;
import fi.metatavu.metaform.server.rest.ReplyMode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.util.CellReference;
import org.slf4j.Logger;

import fi.metatavu.metaform.server.attachments.AttachmentController;
import fi.metatavu.metaform.server.crypto.CryptoController;
import fi.metatavu.metaform.server.exporttheme.ExportThemeFreemarkerRenderer;
import fi.metatavu.metaform.server.exporttheme.ReplyExportDataModel;
import fi.metatavu.metaform.server.files.File;
import fi.metatavu.metaform.server.files.FileController;
import fi.metatavu.metaform.server.pdf.PdfPrinter;
import fi.metatavu.metaform.server.pdf.PdfRenderException;
import fi.metatavu.metaform.server.persistence.dao.AnyReplyFieldDAO;
import fi.metatavu.metaform.server.persistence.dao.AnyTableReplyFieldRowCellDAO;
import fi.metatavu.metaform.server.persistence.dao.AttachmentReplyFieldDAO;
import fi.metatavu.metaform.server.persistence.dao.AttachmentReplyFieldItemDAO;
import fi.metatavu.metaform.server.persistence.dao.BooleanReplyFieldDAO;
import fi.metatavu.metaform.server.persistence.dao.ListReplyFieldDAO;
import fi.metatavu.metaform.server.persistence.dao.ListReplyFieldItemDAO;
import fi.metatavu.metaform.server.persistence.dao.NumberReplyFieldDAO;
import fi.metatavu.metaform.server.persistence.dao.ReplyDAO;
import fi.metatavu.metaform.server.persistence.dao.StringReplyFieldDAO;
import fi.metatavu.metaform.server.persistence.dao.TableReplyFieldDAO;
import fi.metatavu.metaform.server.persistence.dao.TableReplyFieldNumberRowCellDAO;
import fi.metatavu.metaform.server.persistence.dao.TableReplyFieldRowDAO;
import fi.metatavu.metaform.server.persistence.dao.TableReplyFieldStringRowCellDAO;
import fi.metatavu.metaform.server.persistence.model.Attachment;
import fi.metatavu.metaform.server.persistence.model.AttachmentReplyField;
import fi.metatavu.metaform.server.persistence.model.AttachmentReplyFieldItem;
import fi.metatavu.metaform.server.persistence.model.BooleanReplyField;
import fi.metatavu.metaform.server.persistence.model.ListReplyField;
import fi.metatavu.metaform.server.persistence.model.Metaform;
import fi.metatavu.metaform.server.persistence.model.NumberReplyField;
import fi.metatavu.metaform.server.persistence.model.Reply;
import fi.metatavu.metaform.server.persistence.model.ReplyField;
import fi.metatavu.metaform.server.persistence.model.StringReplyField;
import fi.metatavu.metaform.server.persistence.model.TableReplyField;
import fi.metatavu.metaform.server.persistence.model.TableReplyFieldRow;
import fi.metatavu.metaform.server.persistence.model.TableReplyFieldRowCell;
import fi.metatavu.metaform.server.script.FormRuntimeContext;
import fi.metatavu.metaform.server.script.ScriptController;
import fi.metatavu.metaform.server.xlsx.CellSource;
import fi.metatavu.metaform.server.xlsx.CellSourceType;
import fi.metatavu.metaform.server.xlsx.XlsxBuilder;
import fi.metatavu.metaform.server.xlsx.XlsxException;

/**
 * Reply controller
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class ReplyController {

  private static final MetaformTableColumnType[] SUPPORTED_TABLE_COLUMN_TYPES = new MetaformTableColumnType[] {
    MetaformTableColumnType.TEXT,
    MetaformTableColumnType.NUMBER
  };

  private static final String REPLY_RESOURCE_URI_TEMPLATE = "/v1/metaforms/%s/replies/%s";
  private static final String REPLY_RESOURCE_NAME_TEMPLATE = "reply-%s";

  @Inject
  private Logger logger;
 
  @Inject
  private ReplyDAO replyDAO;

  @Inject
  private AnyReplyFieldDAO anyReplyFieldDAO;

  @Inject
  private StringReplyFieldDAO stringReplyFieldDAO;

  @Inject
  private BooleanReplyFieldDAO booleanReplyFieldDAO;

  @Inject
  private NumberReplyFieldDAO numberReplyFieldDAO;

  @Inject
  private ListReplyFieldDAO listReplyFieldDAO;

  @Inject
  private ListReplyFieldItemDAO listReplyFieldItemDAO;

  @Inject
  private AttachmentReplyFieldDAO attachmentReplyFieldDAO;

  @Inject
  private AttachmentReplyFieldItemDAO attachmentReplyFieldItemDAO;
  
  @Inject
  private TableReplyFieldDAO tableReplyFieldDAO;

  @Inject
  private TableReplyFieldNumberRowCellDAO tableReplyFieldNumberRowCellDAO;

  @Inject
  private AnyTableReplyFieldRowCellDAO anyTableReplyFieldRowCellDAO;
  
  @Inject
  private TableReplyFieldRowDAO tableReplyFieldRowDAO;

  @Inject
  private TableReplyFieldStringRowCellDAO tableReplyFieldStringRowCellDAO;
  
  @Inject
  private AttachmentController attachmentController;
  
  @Inject
  private FileController fileController;

  @Inject
  private ExportThemeFreemarkerRenderer exportThemeFreemarkerRenderer; 

  @Inject
  private PdfPrinter pdfPrinter; 

  @Inject
  private ScriptController scriptController;

  @Inject
  private CryptoController cryptoController;
  
  @Inject
  private FormRuntimeContext formRuntimeContext;
  
  /**
   * Creates new reply
   * 
   * @param userId user id 
   * @param metaform Metaform
   * @param privateKey private key
   * @return Created reply
   */
  public Reply createReply(UUID userId, Metaform metaform, PrivateKey privateKey) {
    UUID id = UUID.randomUUID();
    return replyDAO.create(id, userId, metaform, null, privateKey != null ? privateKey.getEncoded() : null);
  }
  
  /**
   * Lists field names used in reply
   * 
   * @param reply reply
   * @return field names used in reply
   */
  public List<String> listFieldNames(Reply reply) {
    return anyReplyFieldDAO.listNamesByReply(reply);
  }

  /**
   * Finds reply by id
   * 
   * @param replyId replyId
   * @return reply
   */
  public Reply findReplyById(UUID replyId) {
    return replyDAO.findById(replyId);
  }
  
  /**
   * Fields active (non revisioned) reply by metaform and user id
   * 
   * @param metaform metaform
   * @param userId user id
   * @return found reply
   */
  public Reply findActiveReplyByMetaformAndUserId(Metaform metaform, UUID userId) {
    return replyDAO.findByMetaformAndUserIdAndRevisionNull(metaform, userId);
  }
  
  /**
   * Updates reply authorization resource id
   * 
   * @param reply reply
   * @param resourceId authorization resource id
   * @return reply
   */
  public Reply updateResourceId(Reply reply, UUID resourceId) {
    replyDAO.updateResourceId(reply, resourceId);
    return reply;
  }

  /**
   * Validates field value
   * 
   * @param metaformField field
   * @param value value
   * @return whether value is valid or not
   */
  public boolean isValidFieldValue(MetaformField metaformField, Object value) {
    if (metaformField.getType() == MetaformFieldType.TABLE) {
      Map<String, MetaformTableColumn> columnMap = getTableColumnMap(metaformField);
      return getTableValue(columnMap, value) != null;
    }
    
    return true;
  }


  /**
   * Sets reply field value
   * 
   * @param reply reply
   * @param name name 
   * @param value value
   * @return updated field
   */
  public ReplyField setReplyField(MetaformField field, Reply reply, String name, Object value) {
    MetaformFieldType fieldType = field.getType();
    if (fieldType == MetaformFieldType.FILES) {
      return setFileReplyField(reply, name, value);
    } else if (fieldType == MetaformFieldType.TABLE) {
      return setTableReplyField(field, reply, name, value);
    }

    if (value instanceof Boolean) {
      return setReplyField(reply, name, (Boolean) value);
    } else if (value instanceof Number) {
      return setReplyField(reply, name, (Number) value);
    } else if (value instanceof String) {
      return setReplyField(reply, name, (String) value);
    } else if (value instanceof List) {
      return setReplyField(reply, name, (List<?>) value);
    } else {
      if (logger.isErrorEnabled()) {
        logger.error(String.format("Unsupported to type (%s) for field %s in reply %s", value.getClass().getName(), name, reply.getId().toString()));
      }
    }
    
    return null;
  }
  
  /**
   * Sets reply field value
   * 
   * @param reply reply
   * @param name name 
   * @param value value
   * @return updated field
   */
  public ReplyField setReplyField(Reply reply, String name, String value) {
    ReplyField replyField = anyReplyFieldDAO.findByReplyAndName(reply, name);
    if (replyField != null && !(replyField instanceof StringReplyField)) {
      deleteField(replyField);
      replyField = null;
    }
    
    if (replyField == null) {
      return stringReplyFieldDAO.create(UUID.randomUUID(), reply, name, value);
    } else {
      return stringReplyFieldDAO.updateValue((StringReplyField) replyField, value);
    }
  }

  /**
   * Sets reply field value
   * 
   * @param reply reply
   * @param name name 
   * @param value value
   * @return updated field
   */
  public ReplyField setReplyField(Reply reply, String name, Boolean value) {
    ReplyField replyField = anyReplyFieldDAO.findByReplyAndName(reply, name);
    if (replyField != null && !(replyField instanceof BooleanReplyField)) {
      deleteField(replyField);
      replyField = null;
    }
    
    if (replyField == null) {
      return booleanReplyFieldDAO.create(UUID.randomUUID(), reply, name, value);
    } else {
      return booleanReplyFieldDAO.updateValue((BooleanReplyField) replyField, value);
    }
  }

  /**
   * Sets reply field value
   * 
   * @param reply reply
   * @param name name 
   * @param value value
   * @return updated field
   */
  public ReplyField setReplyField(Reply reply, String name, Number value) {
    ReplyField replyField = anyReplyFieldDAO.findByReplyAndName(reply, name);
    if (replyField != null && !(replyField instanceof NumberReplyField)) {
      deleteField(replyField);
      replyField = null;
    }
    
    if (replyField == null) {
      return numberReplyFieldDAO.create(UUID.randomUUID(), reply, name, value.doubleValue());
    } else {
      return numberReplyFieldDAO.updateValue((NumberReplyField) replyField, value.doubleValue());
    }
  }

  /**
   * Sets reply field value
   * 
   * @param reply reply
   * @param name name 
   * @param value values
   * @return updated field
   */
  @SuppressWarnings("unchecked")
  public ReplyField setFileReplyField(Reply reply, String name, Object value) {
    List<String> fileRefs = null;
    
    if (value instanceof List<?>) {
      fileRefs = (List<String>) value;
    } else {
      fileRefs = Arrays.asList(String.valueOf(value));
    }
    
    ReplyField replyField = anyReplyFieldDAO.findByReplyAndName(reply, name);
    if (replyField != null && !(replyField instanceof AttachmentReplyField)) {
      deleteField(replyField);
      replyField = null;
    }
    
    AttachmentReplyField attachmentReplyField = (AttachmentReplyField) replyField;
    List<AttachmentReplyFieldItem> items = null;
    if (attachmentReplyField == null) {
      items = Collections.emptyList();
      attachmentReplyField = attachmentReplyFieldDAO.create(UUID.randomUUID(), reply, name);
    } else {
      items = attachmentReplyFieldItemDAO.listByField(attachmentReplyField);
      removeUnusedAttachmentReplyItems(items, fileRefs);
    }
    
    attachUsedReplyItems(reply.getUserId(), attachmentReplyField, items, fileRefs);

    return attachmentReplyField;
  }
  
  /**
   * Deletes specified fields from the reply
   * 
   * @param reply reply
   * @param fieldNames field names to be deleted
   */
  public void deleteReplyFields(Reply reply, List<String> fieldNames) {
    for (String fieldName : fieldNames) {
      ReplyField replyField = anyReplyFieldDAO.findByReplyAndName(reply, fieldName);
      if (replyField != null) {
        deleteField(replyField);
      }
    }
  }

  /**
   * Deletes a reply
   * 
   * @param reply reply
   */
  public void deleteReply(Reply reply) {
    anyReplyFieldDAO.listByReply(reply).stream()
      .forEach(this::deleteField);
    
    replyDAO.delete(reply);
  }

  /**
   * Lists replies
   * 
   * @param metaform Metaform
   * @param userId userId
   * @param createdBefore filter results by created before specified time.
   * @param createdAfter filter results by created after specified time.
   * @param modifiedBefore filter results by modified before specified time.
   * @param modifiedAfter filter results by modified after specified time.
   * @param includeRevisions 
   * @return replies list of replies
   * @return replies
   */
  @SuppressWarnings ("squid:S00107")
  public List<Reply> listReplies(Metaform metaform, UUID userId, OffsetDateTime createdBefore, OffsetDateTime createdAfter, OffsetDateTime modifiedBefore, OffsetDateTime modifiedAfter, boolean includeRevisions, FieldFilters fieldFilters) {
    return replyDAO.list(metaform, userId, includeRevisions, createdBefore, createdAfter, modifiedBefore, modifiedAfter, fieldFilters);
  }

  /**
   * Lists reply fields by reply
   * 
   * @param reply reply
   * @return reply fields
   */
  public List<ReplyField> listReplyFields(Reply reply) {
    return anyReplyFieldDAO.listByReply(reply);
  }

  /**
   * Renders Reply as PDF document
   * 
   * @param metaformEntity Metaform
   * @param replyEntity Reply
   * @param locale locale
   * @return Pdf bytes
   * @throws PdfRenderException throw when rendering fails
   */
  public byte[] getReplyPdf(String exportThemeName, fi.metatavu.metaform.api.spec.model.Metaform metaformEntity, fi.metatavu.metaform.api.spec.model.Reply replyEntity, Map<String, fi.metatavu.metaform.api.spec.model.Attachment> attachmentMap, Locale locale) throws PdfRenderException {
    ReplyExportDataModel dataModel = new ReplyExportDataModel(metaformEntity, replyEntity, attachmentMap, getDate(replyEntity.getCreatedAt()), getDate(replyEntity.getModifiedAt()));
    String html = exportThemeFreemarkerRenderer.render(String.format("%s/reply/pdf.ftl", exportThemeName), dataModel, locale);
    
    try (InputStream htmlStream = IOUtils.toInputStream(html, StandardCharsets.UTF_8)) {
      try (ByteArrayOutputStream pdfStream = new ByteArrayOutputStream()) {
        pdfPrinter.printHtmlAsPdf(htmlStream, pdfStream);
        return pdfStream.toByteArray();
      }
    } catch (IOException e) {
      throw new PdfRenderException("Pdf rendering failed", e);
    }
  }

  /**
   * Returns metaform replies as XLSX binary
   * 
   * @param metaform metaform
   * @param metaformEntity metaform REST entity
   * @param replyEntities metaform reply entites
   * @return replies as XLSX binary
   * @throws XlsxException thrown when export fails
   */
  public byte[] getRepliesAsXlsx(Metaform metaform, fi.metatavu.metaform.api.spec.model.Metaform metaformEntity, List<fi.metatavu.metaform.api.spec.model.Reply> replyEntities) throws XlsxException {
    String title = metaformEntity.getTitle();
    if (StringUtils.isBlank(title)) {
      title = metaform.getSlug();
    }

    try (ByteArrayOutputStream output = new ByteArrayOutputStream(); XlsxBuilder xlsxBuilder = new XlsxBuilder()) { 
      String sheetId = xlsxBuilder.createSheet(title);
        
      List<MetaformField> fields = metaformEntity.getSections().stream()
        .map(MetaformSection::getFields)
        .flatMap(List::stream)
        .filter(field -> StringUtils.isNotEmpty(field.getName()))
        .filter(field -> StringUtils.isNotEmpty(field.getTitle()))
        .collect(Collectors.toList());
      
      // Headers 
      
      for (int i = 0; i < fields.size(); i++) {
        MetaformField field = fields.get(i);
        xlsxBuilder.setCellValue(sheetId, 0, i, field.getTitle(), new CellSource(field, CellSourceType.HEADER));
      }
      
      // Values
      
      for (int columnIndex = 0; columnIndex < fields.size(); columnIndex++) {
        MetaformField field = fields.get(columnIndex);

        for (int replyIndex = 0; replyIndex < replyEntities.size(); replyIndex++) {          
          Map<String, Object> replyData = replyEntities.get(replyIndex).getData();
          int rowIndex = replyIndex + 1;
          CellSource cellSource = new CellSource(field, CellSourceType.VALUE);

          Object value = replyData.get(field.getName());
          if (value != null) {
            switch (field.getType()) {
              case DATE:
              case DATE_TIME:
                xlsxBuilder.setCellValue(sheetId, rowIndex, columnIndex, OffsetDateTime.parse(value.toString()), cellSource);
              break;
              case SELECT:
              case RADIO:
                String selectedValue = field.getOptions().stream()
                  .filter(option -> option.getName().equals(value.toString()))
                  .map(MetaformFieldOption::getText)
                  .findFirst()
                  .orElse(value.toString());
                
                xlsxBuilder.setCellValue(sheetId, rowIndex, columnIndex, selectedValue, cellSource);
              break;
              case TABLE:
                String sheetName = createXlsxTableSheet(xlsxBuilder, replyIndex, field, value);
                if (StringUtils.isNotBlank(sheetName)) {
                  xlsxBuilder.setCellLink(sheetId, rowIndex, columnIndex, HyperlinkType.DOCUMENT, sheetName, sheetName, cellSource);
                }
              break;
              default:
                xlsxBuilder.setCellValue(sheetId, rowIndex, columnIndex, value.toString(), cellSource);
              break;
            }
          }
        }
      }
      
      formRuntimeContext.setXlsxBuilder(xlsxBuilder);
      
      MetaformScripts scripts = metaformEntity.getScripts();
      if (scripts != null) {
        scriptController.runScripts(scripts.getMetaformExportXlsx());
      }
      
      xlsxBuilder.write(output);
      
      return output.toByteArray();
    } catch (Exception e) {
      throw new XlsxException("Failed to create XLSX export", e);
    }
  }

  /**
   * Returns whether owner key is valid for given reply
   * 
   * @param reply reply
   * @param ownerKey owner key
   * @return whether owner key is valid for given reply
   */
  public boolean isValidOwnerKey(Reply reply, String ownerKey) {
    PublicKey publicKey = cryptoController.loadPublicKeyBase64(ownerKey);
    if (publicKey == null) {
      return false;
    }

    return isValidOwnerKey(reply, publicKey);
  }

  /**
   * Returns whether owner key is valid for given reply
   * 
   * @param reply reply
   * @param ownerKey owner key
   * @return whether owner key is valid for given reply
   */
  private boolean isValidOwnerKey(Reply reply, PublicKey ownerKey) {
    PrivateKey privateKey = cryptoController.loadPrivateKey(reply.getPrivateKey());
    if (privateKey == null) {
      return false;
    }

    byte[] signature = cryptoController.signUUID(privateKey, reply.getId());
    if (cryptoController.verifyUUID(ownerKey, signature, reply.getId())) {
      return true;
    }

    return false;
  }

  /**
   * Creates new sheet for table values 
   * 
   * @param xlsxBuilder
   * @param replyIndex
   * @param field
   * @param value
   * @return
   */
  private String createXlsxTableSheet(XlsxBuilder xlsxBuilder, int replyIndex, MetaformField field, Object value) {
    List<MetaformTableColumn> columns = field.getColumns();
    
    if (columns.isEmpty()) {
      return null;
    }
    
    Map<String, MetaformTableColumn> columnMap = getTableColumnMap(field);
    List<Map<String, Object>> tableValue = getTableValue(columnMap, value);

    if (!tableValue.isEmpty()) {
      String fieldTitle = field.getTitle(); 
      if (StringUtils.isBlank(fieldTitle)) {
        fieldTitle = field.getName();
      }
      
      String sheetName = String.format("%s - %s", StringUtils.truncate(fieldTitle, 20), replyIndex + 1);
      String sheetId = xlsxBuilder.createSheet(sheetName);
      
      // Headers 
      
      for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
        MetaformTableColumn column = columns.get(columnIndex);
        String columnTitle = column.getTitle();
        if (StringUtils.isBlank(columnTitle)) {
          columnTitle = column.getName(); 
        }
        
        xlsxBuilder.setCellValue(sheetId, 0, columnIndex, columnTitle, new CellSource(field, CellSourceType.TABLE_HEADER));
      }
      
      // Values

      for (int rowIndex = 0; rowIndex < tableValue.size(); rowIndex++) {
        CellSource cellSource = new CellSource(field, CellSourceType.TABLE_VALUE);
        Map<String, Object> row = tableValue.get(rowIndex);
        int rowNumber = rowIndex + 1;

        for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
          MetaformTableColumn column = columns.get(columnIndex);
          Object cellValue = row.get(column.getName());

          if (cellValue != null) {
            switch (column.getType()) {
              case DATE:
              case TIME:
                xlsxBuilder.setCellValue(sheetId, rowNumber, columnIndex, OffsetDateTime.parse(cellValue.toString()), cellSource);
              break;
              case NUMBER:
                xlsxBuilder.setCellValue(sheetId, rowNumber, columnIndex, NumberUtils.createDouble(cellValue.toString()), cellSource);
              break;
              default:
                xlsxBuilder.setCellValue(sheetId, rowNumber, columnIndex, cellValue.toString(), cellSource);
              break;
            }
          }
        }
      }
      
      // Sums

      for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
        MetaformTableColumn column = columns.get(columnIndex);
        
        if (Boolean.TRUE.equals(column.getCalculateSum())) {
          CellSource cellSource = new CellSource(field, CellSourceType.TABLE_SUM);
          String columnString = CellReference.convertNumToColString(columnIndex);
          xlsxBuilder.setCellFormula(sheetId, tableValue.size() + 1, columnIndex, String.format("SUM(%s%d:%s%d)", columnString, 2, columnString, tableValue.size() + 1), cellSource);
        }
      }

      return sheetName;      
    }

    return null;
  }

  /**
   * Returns OffsetDateTime as java.util.Date
   * 
   * @param offsetDateTime offset date time
   * @return java.util.Date
   */
  private Date getDate(OffsetDateTime offsetDateTime) {
    if (offsetDateTime == null) {
      return null;
    }
    
    return Date.from(offsetDateTime.toInstant());
  }
  
  /**
   * Returns table field as valid table data. 
   * 
   * If input object is not valid null is returned instead
   * 
   * @param columnMap column map
   * @param value input object
   * @return table data
   */
  @SuppressWarnings({"unchecked", "squid:S1168"})
  private List<Map<String, Object>> getTableValue(Map<String, MetaformTableColumn> columnMap, Object value) {
    if (value == null) {
      return Collections.emptyList();
    }
    
    if (!(value instanceof List)) {
      return null;
    }
    
    List<Object> listValue = (List<Object>) value;
    if (listValue.isEmpty()) {
      return Collections.emptyList();
    }
    
    for (Object listItem : listValue) {
      if (!(listItem instanceof Map)) {
        return null;
      }
      
      Map<Object, Object> mapItem = (Map<Object, Object>) listItem;
      for (Object key : mapItem.keySet()) {
        if (!(key instanceof String)) {
          return null;
        }
        
        String columnName = (String) key;
        MetaformTableColumn column = columnMap.get(columnName);
        
        if (column == null || !isSupportedTableColumnType(column.getType())) {
          return null;
        }
      }
    }
    
    return (List<Map<String, Object>>) value;
  }
  
  /**
   * Sets reply field value
   * 
   * @param field field
   * @param reply reply
   * @param name name 
   * @param value values
   * @return updated field
   */
  private ReplyField setTableReplyField(MetaformField field, Reply reply, String name, Object value) {
    Map<String, MetaformTableColumn> columnMap = getTableColumnMap(field);
    List<Map<String, Object>> tableValue = getTableValue(columnMap, value);
    if (tableValue == null) {
      if (logger.isErrorEnabled()) {
        logger.error(String.format("Invalid value (%s) passes to table field %s in reply %s", String.valueOf(value), name, reply.getId().toString()));
      }
      
      return null;
    }
    
    ReplyField replyField = anyReplyFieldDAO.findByReplyAndName(reply, name);
    if (replyField != null && !(replyField instanceof TableReplyField)) {
      deleteField(replyField);
      replyField = null;
    }
    
    TableReplyField tableReplyField = (TableReplyField) replyField;
    if (tableReplyField == null) {
      tableReplyField = tableReplyFieldDAO.create(UUID.randomUUID(), reply, name);
    } else {
      deleteTableReplyFieldRows(tableReplyField);
    }
    
    for (Map<String, Object> rowValue : tableValue) {
      createTableReplyFieldRowValue(tableReplyField, columnMap, rowValue);
    }

    return tableReplyField;
  }

  /**
   * Returns column map for a table field
   * 
   * @param field field
   * @return column map for a table field
   */
  private Map<String, MetaformTableColumn> getTableColumnMap(MetaformField field) {
    if (field.getColumns() == null) {
      return Collections.emptyMap();
    }
    
    return field.getColumns().stream().collect(Collectors.toMap(MetaformTableColumn::getName, column -> column));
  }
  
  /**
   * Creates new row for table field reply
   * 
   * @param tableReplyField table field
   * @param columnMap column map
   * @param rowValue row value
   * @return created row
   */
  private TableReplyFieldRow createTableReplyFieldRowValue(TableReplyField tableReplyField, Map<String, MetaformTableColumn> columnMap, Map<String, Object> rowValue) {
    TableReplyFieldRow row = createTableReplyFieldRow(tableReplyField);
    
    rowValue.entrySet().stream()
      .filter(cell -> cell.getValue() != null)
      .filter(cell -> !isBlankString(cell.getValue()))
      .filter(cell -> columnMap.containsKey(cell.getKey()))
      .forEach(cell -> {
        MetaformTableColumn column = columnMap.get(cell.getKey());
        createTableReplyFieldRowCell(row, column, cell.getValue());
      });
    
    return row;
  }
  
  /**
   * Returns whether object is a blank string
   * 
   * @param object object
   * @return whether object is a blank string
   */
  private boolean isBlankString(Object object) {
    if (object instanceof String) {
      String string = (String) object;
      return StringUtils.isBlank(string);
    }
    
    return false;
  }
  
  /**
   * Creates new table reply row
   * 
   * @param field field
   * @return created row
   */
  private TableReplyFieldRow createTableReplyFieldRow(TableReplyField field) {
    return tableReplyFieldRowDAO.create(UUID.randomUUID(), field);
  }
  
  /**
   * Returns whether table column type is supported or not
   * 
   * @param type type
   * @return whether table column type is supported or not
   */
  private boolean isSupportedTableColumnType(MetaformTableColumnType type) {
    return ArrayUtils.contains(SUPPORTED_TABLE_COLUMN_TYPES, type); 
  }
  
  /**
   * Creates table reply field cell
   * 
   * @param row row
   * @param column column
   * @param value value
   * @return created cell
   */
  private TableReplyFieldRowCell createTableReplyFieldRowCell(TableReplyFieldRow row, MetaformTableColumn column, Object value) {
    switch (column.getType()) {
      case TEXT:
        return tableReplyFieldStringRowCellDAO.create(UUID.randomUUID(), row, column.getName(), String.valueOf(value));
      case NUMBER:
        Double numberValue = null;
        
        if (value instanceof Number) {
          numberValue = ((Number) value).doubleValue();
        } else if (value instanceof String) {
          numberValue = NumberUtils.createDouble((String) value); 
        }
        
        if (numberValue != null) {        
          tableReplyFieldNumberRowCellDAO.create(UUID.randomUUID(), row, column.getName(), numberValue);
        } else {
          if (logger.isErrorEnabled()) {
            logger.error("Could not save value {} for tableReplyFieldNumberRowCell", value);
          }
        }
      break;
      default:
        if (logger.isErrorEnabled()) {
          logger.error("Unsupported table column type {}", column.getType());
        }
      break;
    }
    
    return null;
  }

  /**
   * Sets reply field value
   * 
   * @param reply reply
   * @param name name 
   * @param values values
   * @return updated field
   */
  private ReplyField setReplyField(Reply reply, String name, List<?> values) {
    ReplyField replyField = anyReplyFieldDAO.findByReplyAndName(reply, name);
    if (replyField != null && !(replyField instanceof ListReplyField)) {
      deleteField(replyField);
      replyField = null;
    }
    
    ListReplyField listReplyField = (ListReplyField) replyField;
    if (values == null || values.isEmpty()) {
      if (listReplyField != null) {
        deleteListReplyFieldItems(listReplyField); 
        listReplyFieldDAO.delete(listReplyField);
      }
      
      return null;
    }
    
    if (listReplyField == null) {
      listReplyField = listReplyFieldDAO.create(UUID.randomUUID(), reply, name);
    } else {
      deleteListReplyFieldItems(listReplyField); 
    }
    
    for (Object value : values) {
      if (value != null) {
        listReplyFieldItemDAO.create(UUID.randomUUID(), listReplyField, value.toString());
      }
    }
    
    return listReplyField;
  }
  
  /**
   * Removes unused attachment reply items
   * 
   * @param items items
   * @param savedFileRefs saved file refs
   */
  private void removeUnusedAttachmentReplyItems(List<AttachmentReplyFieldItem> items, List<String> savedFileRefs) {
    items.stream()
      .filter(attachmentReplyFieldItem -> {
        String fileRef = attachmentReplyFieldItem.getAttachment().getId().toString();
        return !savedFileRefs.contains(fileRef);
      })
      .forEach(this::deleteAttachmentReplyFieldItem);
  }

  /**
   * Deletes attachment reply field item
   * 
   * @param item item
   */
  private void deleteAttachmentReplyFieldItem(AttachmentReplyFieldItem item) {
    Attachment attachment = item.getAttachment();
    attachmentReplyFieldItemDAO.delete(item);
    attachmentController.deleteAttachment(attachment);
  }
  
  /**
   * Attaches attachments to reply
   * 
   * @param userId user
   * @param field field
   * @param items items
   * @param savedFileRefs saved file refs
   */
  private void attachUsedReplyItems(UUID userId, AttachmentReplyField field, List<AttachmentReplyFieldItem> items, List<String> savedFileRefs) {
    List<String> usedFileRefs = items.stream()
      .map(AttachmentReplyFieldItem::getAttachment)
      .map(Attachment::getId)
      .map(UUID::toString)
      .collect(Collectors.toList());
    
    savedFileRefs.stream()
      .filter(savedFileRef -> !usedFileRefs.contains(savedFileRef))
      .forEach(id -> {
        Attachment attachment = retrieveOrPersistAttachment(UUID.fromString(id), userId);
        if (attachment != null) {
          attachmentReplyFieldItemDAO.create(UUID.randomUUID(), field, attachment);
        } else {
          logger.error("Could not find attachment with id {}", id);
        }        
      });
  }
  
  /**
   * Converts reply into a revision by updating the modifiedAt field into the revision field.
   * 
   * @param reply reply to be converted into a revision
   */
  public void convertToRevision(Reply reply) {
    replyDAO.updateRevision(reply, reply.getModifiedAt());
  }

  /**
   * Deletes a reply field
   * 
   * @param replyField reply field
   */
  private void deleteField(ReplyField replyField) {
    if (replyField instanceof ListReplyField) {
      ListReplyField listReplyField = (ListReplyField) replyField;
      deleteListReplyFieldItems(listReplyField);
    }
    
    if (replyField instanceof AttachmentReplyField) {
      AttachmentReplyField attachmentReplyField = (AttachmentReplyField) replyField;
      deleteAttachmentReplyFieldItems(attachmentReplyField);
    }
    
    if (replyField instanceof TableReplyField) {
      TableReplyField tableReplyField = (TableReplyField) replyField;
      deleteTableReplyFieldRows(tableReplyField);
    }
    
    anyReplyFieldDAO.delete(replyField);
  }

  /**
   * Deletes all rows from table reply field
   * 
   * @param tableReplyField table reply field
   */
  private void deleteTableReplyFieldRows(TableReplyField tableReplyField) {
    tableReplyFieldRowDAO.listByField(tableReplyField).forEach(this::deleteTableReplyFieldRow);
  }

  /**
   * Deletes table reply field row
   * 
   * @param tableReplyFieldRow row
   */
  private void deleteTableReplyFieldRow(TableReplyFieldRow tableReplyFieldRow) {
    anyTableReplyFieldRowCellDAO.listByRow(tableReplyFieldRow).stream().forEach(anyTableReplyFieldRowCellDAO::delete);
    tableReplyFieldRowDAO.delete(tableReplyFieldRow);
  }

  /**
   * Removes all items from list reply field
   * 
   * @param listReplyField field
   */
  private void deleteListReplyFieldItems(ListReplyField listReplyField) {
    listReplyFieldItemDAO.listByField(listReplyField).stream().forEach(listReplyFieldItemDAO::delete);
  }
  
  /**
   * Removes all items from attachment reply field
   * 
   * @param attachmentReplyField field
   */
  private void deleteAttachmentReplyFieldItems(AttachmentReplyField attachmentReplyField) {
    attachmentReplyFieldItemDAO.listByField(attachmentReplyField).stream().forEach(this::deleteAttachmentReplyFieldItem);
  }

  /**
   * Retrieves existing attachment or persists one from previously uploaded one.
   * 
   * @param id attachment id
   * @param userId user id
   * @return attachment
   */
  private Attachment retrieveOrPersistAttachment(UUID id, UUID userId) {
    Attachment attachment = attachmentController.findAttachmentById(id);
    if (attachment == null) {
      return persistAttachment(id, userId);
    }
    
    return attachment;
  }

  /**
   * Persists previously uploaded file as attachment
   * 
   * @param id attachment id / fileRef
   * @param userId user id
   * @return 
   */
  private Attachment persistAttachment(UUID id, UUID userId) {
    File file = fileController.popFileData(id.toString());
    String name = file.getMeta().getFileName();
    byte[] content = file.getData();
    String contentType = file.getMeta().getContentType();
    return attachmentController.create(id, name, content, contentType, userId);
  }

  /**
   * Returns resource name for a reply
   *
   * @param reply reply
   * @return resource name
   */
  public String getReplyResourceName(fi.metatavu.metaform.server.persistence.model.Reply reply) {
    if (reply == null) {
      return null;
    }

    return String.format(REPLY_RESOURCE_NAME_TEMPLATE, reply.getId());
  }

  /**
   * Returns resource URI for reply
   *
   * @param reply reply
   * @return resource URI
   */
  public String getReplyResourceUri(fi.metatavu.metaform.server.persistence.model.Reply reply) {
    if (reply == null) {
      return null;
    }

    fi.metatavu.metaform.server.persistence.model.Metaform metaform = reply.getMetaform();
    return getReplyResourceUri(metaform.getId(), reply.getId());
  }

  /**
   * Returns resource URI for reply
   *
   * @param metaformId Metaform id
   * @param replyId reply id
   * @return resource URI
   */
  private String getReplyResourceUri(UUID metaformId, UUID replyId) {
    return String.format(REPLY_RESOURCE_URI_TEMPLATE, metaformId, replyId);
  }

  /**
   * Resolves reply object when creating new reply
   *
   * @param replyMode reply mode
   * @param metaform metaform
   * @param anonymous is user anonymous
   * @param userId user id
   * @return reply object
   */
  public fi.metatavu.metaform.server.persistence.model.Reply createReplyResolveReply(ReplyMode replyMode, fi.metatavu.metaform.server.persistence.model.Metaform metaform, boolean anonymous, UUID userId, PrivateKey privateKey) {
    fi.metatavu.metaform.server.persistence.model.Reply reply;

    if (anonymous || replyMode == ReplyMode.CUMULATIVE) {
      reply = createReply(userId, metaform, privateKey);
    } else {
      reply = findActiveReplyByMetaformAndUserId(metaform, userId);
      if (reply == null) {
        reply = createReply(userId, metaform, privateKey);
      } else {
        if (replyMode == ReplyMode.REVISION) {
          // If there is already an existing reply but we are not updating it
          // We need to change the existing reply into a revision and create new reply
          convertToRevision(reply);
          reply = createReply(userId, metaform, privateKey);
        }
      }
    }

    return reply;
  }

}
