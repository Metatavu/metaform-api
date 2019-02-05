package fi.metatavu.metaform.server.persistence.dao;

import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import fi.metatavu.metaform.server.persistence.model.ListReplyField;
import fi.metatavu.metaform.server.persistence.model.ListReplyFieldItem;
import fi.metatavu.metaform.server.persistence.model.ListReplyFieldItem_;

/**
 * DAO class for ListReplyFieldItem entity
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class ListReplyFieldItemDAO extends AbstractDAO<ListReplyFieldItem> {
  
  /**
   * Creates new list reply field item
   * 
   * @param id id
   * @param field field
   * @param value value
   * @return created ListReplyFieldItem
   */
  public ListReplyFieldItem create(UUID id, ListReplyField field, String value) {
    ListReplyFieldItem listReplyFieldItem = new ListReplyFieldItem(); 
    listReplyFieldItem.setId(id);
    listReplyFieldItem.setField(field);
    listReplyFieldItem.setValue(value);
    return persist(listReplyFieldItem);
  }
  
  /**
   * Lists reply field items by field
   * 
   * @param field list reply field
   * @return list of items
   */
  public List<ListReplyFieldItem> listByField(ListReplyField field) {
    EntityManager entityManager = getEntityManager();
    
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<ListReplyFieldItem> criteria = criteriaBuilder.createQuery(ListReplyFieldItem.class);
    Root<ListReplyFieldItem> root = criteria.from(ListReplyFieldItem.class);
    criteria.select(root);
    criteria.where(criteriaBuilder.equal(root.get(ListReplyFieldItem_.field), field));
    
    return entityManager.createQuery(criteria).getResultList();
  }

  /**
   * Lists item values by field
   * 
   * @param field field
   * @return values
   */
  public List<String> listItemValuesByField(ListReplyField field) {
    EntityManager entityManager = getEntityManager();
    
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<String> criteria = criteriaBuilder.createQuery(String.class);
    Root<ListReplyFieldItem> root = criteria.from(ListReplyFieldItem.class);
    
    criteria.select(root.get(ListReplyFieldItem_.value));
    criteria.where(criteriaBuilder.equal(root.get(ListReplyFieldItem_.field), field));
    
    return entityManager.createQuery(criteria).getResultList();
  }

}
