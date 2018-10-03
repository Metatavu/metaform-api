package fi.metatavu.metaform.server.persistence.dao;

import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import fi.metatavu.metaform.server.persistence.model.TableReplyField;
import fi.metatavu.metaform.server.persistence.model.TableReplyFieldRow;
import fi.metatavu.metaform.server.persistence.model.TableReplyFieldRow_;

/**
 * DAO class for TableReplyFieldRowCell entity
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class TableReplyFieldRowDAO extends AbstractDAO<TableReplyFieldRow> {

  /**
   * Creates new tableReplyFieldRow
   *
   * @param id id
   * @param field field
   * @return created tableReplyFieldRow
   */
   public TableReplyFieldRow create(UUID id, TableReplyField field) {
     TableReplyFieldRow tableReplyFieldRow = new TableReplyFieldRow();
     tableReplyFieldRow.setId(id);
     tableReplyFieldRow.setField(field);
     return persist(tableReplyFieldRow);
   }

  /**
   * Lists reply field rows by field
   * 
   * @param field list reply field
   * @return list of rows
   */
  public List<TableReplyFieldRow> listByField(TableReplyField field) {
    EntityManager entityManager = getEntityManager();
    
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<TableReplyFieldRow> criteria = criteriaBuilder.createQuery(TableReplyFieldRow.class);
    Root<TableReplyFieldRow> root = criteria.from(TableReplyFieldRow.class);
    criteria.select(root);
    criteria.where(criteriaBuilder.equal(root.get(TableReplyFieldRow_.field), field));
    
    return entityManager.createQuery(criteria).getResultList();
  }

}
