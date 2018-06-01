package fi.metatavu.metaform.server.persistence.dao;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.slf4j.Logger;

import fi.metatavu.metaform.server.metaforms.FieldFilter;
import fi.metatavu.metaform.server.metaforms.FieldFilterOperator;
import fi.metatavu.metaform.server.metaforms.FieldFilters;
import fi.metatavu.metaform.server.metaforms.StoreDataType;
import fi.metatavu.metaform.server.persistence.model.BooleanReplyField;
import fi.metatavu.metaform.server.persistence.model.BooleanReplyField_;
import fi.metatavu.metaform.server.persistence.model.ListReplyField;
import fi.metatavu.metaform.server.persistence.model.ListReplyFieldItem;
import fi.metatavu.metaform.server.persistence.model.ListReplyFieldItem_;
import fi.metatavu.metaform.server.persistence.model.ListReplyField_;
import fi.metatavu.metaform.server.persistence.model.Metaform;
import fi.metatavu.metaform.server.persistence.model.NumberReplyField;
import fi.metatavu.metaform.server.persistence.model.NumberReplyField_;
import fi.metatavu.metaform.server.persistence.model.Reply;
import fi.metatavu.metaform.server.persistence.model.ReplyField;
import fi.metatavu.metaform.server.persistence.model.ReplyField_;
import fi.metatavu.metaform.server.persistence.model.Reply_;
import fi.metatavu.metaform.server.persistence.model.StringReplyField;
import fi.metatavu.metaform.server.persistence.model.StringReplyField_;

/**
 * DAO class for Reply entity
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class ReplyDAO extends AbstractDAO<Reply> {
  
  @Inject
  private Logger logger;
  
  /**
   * Creates new reply
   * 
   * @param id id
   * @param userId user id
   * @param metaform Metaform
   * @return created Metaform
   */
  public Reply create(UUID id, UUID userId, Metaform metaform) {
    Reply reply = new Reply(); 
    reply.setId(id);
    reply.setMetaform(metaform);
    reply.setUserId(userId);
    return persist(reply);
  }
  
  /**
   * Finds reply by Metaform, user id and null revision.
   * 
   * @param metaform Metaform
   * @param userId userId
   * @return reply
   */
  public Reply findByMetaformAndUserIdAndRevisionNull(Metaform metaform, UUID userId) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Reply> criteria = criteriaBuilder.createQuery(Reply.class);
    Root<Reply> root = criteria.from(Reply.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.and(
        criteriaBuilder.equal(root.get(Reply_.metaform), metaform),
        criteriaBuilder.equal(root.get(Reply_.userId), userId),
        criteriaBuilder.isNull(root.get(Reply_.revision))
      ) 
    );
    
    return getSingleResult(entityManager.createQuery(criteria));
  }

  /**
   * Lists replies by Metaform
   * 
   * @param metaform Metaform
   * @return list of replies
   */
  public List<Reply> listByMetaform(Metaform metaform) {
    EntityManager entityManager = getEntityManager();
    
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Reply> criteria = criteriaBuilder.createQuery(Reply.class);
    Root<Reply> root = criteria.from(Reply.class);
    criteria.select(root);
    criteria.where(criteriaBuilder.equal(root.get(Reply_.metaform), metaform));
    
    TypedQuery<Reply> query = entityManager.createQuery(criteria);
    
    return query.getResultList();
  }

  /**
   * List replies by multiple filters.
   * 
   * All parameters can be nulled. Nulled parameters will be ignored.
   * 
   * @param metaform Metaform
   * @param userId userId
   * @param revisionNull true to include only null replies with null revision, false to only non null revisions.
   * @param createdBefore filter results by created before specified time.
   * @param createdAfter filter results by created after specified time.
   * @param modifiedBefore filter results by modified before specified time.
   * @param modifiedAfter filter results by modified after specified time.
   * @param fieldFilters field filters
   * @return replies list of replies
   */
  @SuppressWarnings ("squid:S00107")
  public List<Reply> list(Metaform metaform, UUID userId, boolean includeRevisions, OffsetDateTime createdBefore, OffsetDateTime createdAfter, OffsetDateTime modifiedBefore, OffsetDateTime modifiedAfter, FieldFilters fieldFilters) {
    EntityManager entityManager = getEntityManager();
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Reply> criteria = criteriaBuilder.createQuery(Reply.class);
    Root<Reply> root = criteria.from(Reply.class);

    List<Predicate> restrictions = new ArrayList<>();
    
    if (metaform != null) {
      restrictions.add(criteriaBuilder.equal(root.get(Reply_.metaform), metaform));
    }
    
    if (userId != null) {
      restrictions.add(criteriaBuilder.equal(root.get(Reply_.userId), userId));
    }
    
    if (!includeRevisions) {
      restrictions.add(criteriaBuilder.isNull(root.get(Reply_.revision)));
    }
    
    if (createdBefore != null) {
      restrictions.add(criteriaBuilder.lessThanOrEqualTo(root.get(Reply_.createdAt), createdBefore));
    }
  
    if (createdAfter != null) {
      restrictions.add(criteriaBuilder.greaterThanOrEqualTo(root.get(Reply_.createdAt), createdAfter));
    }
    
    if (modifiedBefore != null) {
      restrictions.add(criteriaBuilder.lessThanOrEqualTo(root.get(Reply_.modifiedAt), modifiedBefore));
    }
  
    if (modifiedAfter != null) {
      restrictions.add(criteriaBuilder.greaterThanOrEqualTo(root.get(Reply_.modifiedAt), modifiedAfter));
    }
    
    if (fieldFilters != null) {
      restrictions.add(criteriaBuilder.and(      
        createFieldFilterSubqueries(criteriaBuilder, criteria, fieldFilters).stream()
          .filter(Objects::nonNull)
          .map(fieldSubquery -> (Predicate) criteriaBuilder.in(root).value(fieldSubquery))
          .collect(Collectors.toList())
          .toArray(new Predicate[0])
      ));
    }
    
    criteria.select(root);    
    criteria.where(criteriaBuilder.and(restrictions.toArray(new Predicate[0])));
    TypedQuery<Reply> query = entityManager.createQuery(criteria);
    
    return query.getResultList();
  }
  
  /**
   * Updates reply revision field
   * 
   * @param reply reply
   * @param revision revision time
   * @return updated reply
   */
  public Reply updateRevision(Reply reply, OffsetDateTime revision) {
    reply.setRevision(revision);
    return persist(reply);
  }
  
  /**
   * Creates sub queries for field filters
   * 
   * @param criteriaBuilder criteria builder
   * @param criteria criteria
   * @param fieldFilters filters
   * @return
   */
  private List<Subquery<Reply>> createFieldFilterSubqueries(CriteriaBuilder criteriaBuilder, CriteriaQuery<Reply> criteria, FieldFilters fieldFilters) {
    return Arrays.asList(StoreDataType.values()).stream().filter(value -> value != StoreDataType.NONE).map(storeDataType -> {
      List<FieldFilter> filters = fieldFilters.getFilters(storeDataType);
      if (filters.isEmpty()) {
        return null;
      }
      
      return createFieldFilterSubquery(criteriaBuilder, criteria, storeDataType, filters);
    })
    .collect(Collectors.toList());
  }

  /**
   * Creates field filter sub query
   * 
   * @param criteriaBuilder criteria builder
   * @param criteria criteria
   * @param storeDataType store data type
   * @param filters filters
   * @return field filter sub query
   */
  private Subquery<Reply> createFieldFilterSubquery(CriteriaBuilder criteriaBuilder, CriteriaQuery<Reply> criteria, StoreDataType storeDataType, List<FieldFilter> filters) {
    if (storeDataType == StoreDataType.LIST) {
      return createListFieldFilterSubquery(criteriaBuilder, criteria, filters);
    } else {
      Class<? extends ReplyField> rootClass = getFieldFilterSubqueryRootClass(storeDataType);
      if (rootClass == null) {
        return null;
      }
      
      return createFieldFilterSubquery(rootClass, filters, criteriaBuilder, criteria, fieldRoot -> getFieldFilterSubqueryReplyField(storeDataType, fieldRoot));
    }
  }

  /**
   * Returns field filter reply field
   * 
   * @param storeDataType store data type
   * @param fieldRoot field root
   * @return field filter reply field
   */
  @SuppressWarnings("unchecked")
  private Expression<?> getFieldFilterSubqueryReplyField(StoreDataType storeDataType, Root<? extends ReplyField> fieldRoot) {
    switch (storeDataType) {
      case STRING:
        return ((Root<StringReplyField>) fieldRoot).get(StringReplyField_.value);
      case BOOLEAN:
        return ((Root<BooleanReplyField>) fieldRoot).get(BooleanReplyField_.value);
      case NUMBER:
        return ((Root<NumberReplyField>) fieldRoot).get(NumberReplyField_.value);
      default:
    }
    
    logger.error("Could not resolve reply field for {}", storeDataType);
    
    return null;
  }
  
  /**
   * Resolves field filter root class
   * 
   * @param storeDataType store data type
   * @return field filter root class
   */
  private Class<? extends ReplyField> getFieldFilterSubqueryRootClass(StoreDataType storeDataType) {
    switch (storeDataType) {
      case STRING:
        return StringReplyField.class;
      case BOOLEAN:
        return BooleanReplyField.class;
      case NUMBER:
        return NumberReplyField.class;
      default:
    }
    
    logger.error("Could not resolve root class for {}", storeDataType);
    
    return null;
  }
  
  /**
   * Creates a field filter subquery
   * 
   * @param criteriaBuilder criteria builder
   * @param criteria criteria
   * @param filters filters
   * @return field filter subquery
   */
  private Subquery<Reply> createListFieldFilterSubquery(CriteriaBuilder criteriaBuilder, CriteriaQuery<Reply> criteria, List<FieldFilter> filters) {
    Subquery<Reply> fieldSubquery = criteria.subquery(Reply.class);
    Root<ListReplyFieldItem> root = fieldSubquery.from(ListReplyFieldItem.class);
    Join<ListReplyFieldItem, ListReplyField> fieldJoin = root.join(ListReplyFieldItem_.field);

    fieldSubquery.select(fieldJoin.get(ListReplyField_.reply));
    fieldSubquery.where(criteriaBuilder.or(filters.stream().map(filter -> {
      Predicate valuePredicate = filter.getOperator() == FieldFilterOperator.NOT_EQUALS
          ? criteriaBuilder.notEqual(root.get(ListReplyFieldItem_.value), filter.getValue())
          : criteriaBuilder.equal(root.get(ListReplyFieldItem_.value), filter.getValue());

      return criteriaBuilder.and(
        valuePredicate, 
        criteriaBuilder.equal(fieldJoin.get(ListReplyField_.name), filter.getField())
      );
    }).toArray(Predicate[]::new)));
    
    return fieldSubquery;
  }

  /**
   * Creates a field filter subquery
   * 
   * @param rootClass root class
   * @param fieldFilters filters
   * @param criteriaBuilder criteria builder
   * @param criteria criteria
   * @param valueFieldFunction function for resolving value field
   * @return field filter subquery
   */
  private <T extends ReplyField> Subquery<Reply> createFieldFilterSubquery(Class<T> rootClass, List<FieldFilter> fieldFilters, CriteriaBuilder criteriaBuilder, CriteriaQuery<Reply> criteria, Function<Root<T>, Expression<?>> valueFieldFunction) {
    Subquery<Reply> fieldSubquery = criteria.subquery(Reply.class);
    Root<T> fieldRoot = fieldSubquery.from(rootClass);
    fieldSubquery.select(fieldRoot.get(ReplyField_.reply));
    
    Expression<?> valueField = valueFieldFunction.apply(fieldRoot);
    
    fieldSubquery.where(criteriaBuilder.or(fieldFilters.stream().map(filter -> {
      Predicate valuePredicate = filter.getOperator() == FieldFilterOperator.EQUALS 
          ? criteriaBuilder.equal(valueField, filter.getValue())
          : criteriaBuilder.notEqual(valueField, filter.getValue());
      
      return criteriaBuilder.and(criteriaBuilder.equal(fieldRoot.get(ReplyField_.name), filter.getField()), valuePredicate);
    }).toArray(Predicate[]::new)));
    return fieldSubquery;
  }
  
}
