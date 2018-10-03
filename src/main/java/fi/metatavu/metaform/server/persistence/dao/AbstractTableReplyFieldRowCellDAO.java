package fi.metatavu.metaform.server.persistence.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import fi.metatavu.metaform.server.persistence.model.TableReplyFieldRow;
import fi.metatavu.metaform.server.persistence.model.TableReplyFieldRowCell;
import fi.metatavu.metaform.server.persistence.model.TableReplyFieldRowCell_;

/**
 * DAO class for TableReplyFieldRow entity
 * 
 * @author Antti Leppä
 */
public abstract class AbstractTableReplyFieldRowCellDAO<T extends TableReplyFieldRowCell> extends AbstractDAO<T> {

  /**
   * Lists reply field rows by field
   * 
   * @param row list reply field
   * @return list of rows
   */
  public List<TableReplyFieldRowCell> listByRow(TableReplyFieldRow row) {
    EntityManager entityManager = getEntityManager();
    
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<TableReplyFieldRowCell> criteria = criteriaBuilder.createQuery(TableReplyFieldRowCell.class);
    Root<TableReplyFieldRowCell> root = criteria.from(TableReplyFieldRowCell.class);
    criteria.select(root);
    criteria.where(criteriaBuilder.equal(root.get(TableReplyFieldRowCell_.row), row));
    
    return entityManager.createQuery(criteria).getResultList();
  }
}
