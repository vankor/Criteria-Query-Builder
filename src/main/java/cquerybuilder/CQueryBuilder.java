package cquerybuilder;


import cquerybuilder.exceptions.NotSupportedException;
import cquerybuilder.exceptions.PassingConstructorException;
import cquerybuilder.extractors.ConstructorPassingExtractor;
import cquerybuilder.extractors.FieldsPassingExtractor;
import cquerybuilder.extractors.PassingExtractor;
import cquerybuilder.groupers.GrouperFactory;
import cquerybuilder.groupers.QueryGrouper;
import cquerybuilder.matchers.MatcherFactory;
import cquerybuilder.matchers.Matchers;
import cquerybuilder.matchers.PredicateMatcher;
import cquerybuilder.utils.JpaUtils;
import javafx.util.Pair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.lang.reflect.Field;
import java.util.*;

public class CQueryBuilder<S, D> {

    protected Predicate commonPredicate;
    protected CriteriaBuilder cb;
    protected Root<S> root;
    protected CriteriaQuery<D> cq;
    protected Class<S> entityClass;
    protected Class<D> targetClass;
    protected EntityManager em;
    protected QueryGrouper<S> grouper;
    protected Map<String, List<Pair<String, JoinType>>> joinsQueues = new HashMap<>();
    protected Map<String, Expression> mappings = new HashMap<>();

    public CQueryBuilder(EntityManager em) {
        this.em = em;
        this.cb = em.getCriteriaBuilder();
    }

    public CQueryBuilder(EntityManager em, Class<S> entityClass, Class<D> targetClass) {
        this.em = em;
        this.cb = em.getCriteriaBuilder();
        this.entityClass = entityClass;
        this.cq = cb.createQuery(targetClass);
        this.root = cq.from(entityClass);
    }

    public CriteriaBuilder getCb() {
        return cb;
    }

    public void setCb(CriteriaBuilder cb) {
        this.cb = cb;
    }

    public Root<S> getRoot() {
        return root;
    }

    public void setRoot(Root<S> root) {
        this.root = root;
    }

    public CriteriaQuery<?> getCq() {
        return cq;
    }

    public void setCq(CriteriaQuery<D> cq) {
        this.cq = cq;
    }

    public Predicate getCommonPredicate() {
        return commonPredicate;
    }

    public void setCommonPredicate(Predicate commonPredicate) {
        this.commonPredicate = commonPredicate;
    }

    public Class<S> getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class<S> entityClass) {
        this.entityClass = entityClass;
    }


    public CQueryBuilder<S, D> leftJoin(String fieldName) {
        resolveJoin(fieldName, JoinType.LEFT);
        return this;
    }


    public CQueryBuilder<S, D> innerJoin(String fieldName) {
        resolveJoin(fieldName, JoinType.INNER);
        return this;
    }

    public CQueryBuilder<S, D> rightJoin(String fieldName) {
        resolveJoin(fieldName, JoinType.RIGHT);
        return this;
    }


    private void resolveJoin(String fieldName, JoinType type) {
        String[] parts = fieldName.split("\\.");
        String base = parts[0];
        Integer i = 0;
        for (String part : parts) {
            List<Pair<String, JoinType>> queue = joinsQueues.get(base);
            if (i == 0) {
                if (queue == null) {
                    queue = new LinkedList<>();
                } else {
                    i++;
                    continue;
                }
            }
            Pair<String, JoinType> pair = new Pair<String, JoinType>(part, type);
            queue.add(pair);
            joinsQueues.put(base, queue);
            i++;
        }
    }

    public <T extends Comparable> CQueryBuilder<S, D> where(String fieldName, Matchers matcherType, T... values) throws NotSupportedException {
        PredicateMatcher matcher = MatcherFactory.createPredicate(fieldName, matcherType, values);
        return where(matcher);
    }

    public <T extends Comparable> CQueryBuilder<S, D> and(String fieldName, Matchers matcherType, T... values) throws NotSupportedException {
        PredicateMatcher matcher = MatcherFactory.createPredicate(fieldName, matcherType, values);
        return and(matcher);
    }

    public <T extends Comparable> CQueryBuilder<S, D> or(String fieldName, Matchers matcherType, T... values) throws NotSupportedException {
        PredicateMatcher matcher = MatcherFactory.createPredicate(fieldName, matcherType, values);
        return or(matcher);
    }

    public CQueryBuilder<S, D> where(PredicateMatcher matcher) throws NotSupportedException {
        commonPredicate = matcher.predicate(root, cb, mappings);
        return this;
    }

    public CQueryBuilder<S, D> into(Class<D> tagretClass) throws NoSuchFieldException, NotSupportedException {
        this.targetClass = tagretClass;
        cq = cb.createQuery(tagretClass);
        root = cq.from(entityClass);
        applyJoins(root);
        PassingExtractor<D> extractor = new FieldsPassingExtractor<>(targetClass);
        Selection[] selections = extractor.extractSelections(root, cb);
        mappings = extractor.getMappings();
        cq.select(cb.construct(this.targetClass, selections));

        return this;
    }

    public CQueryBuilder<S, D> passing(String... fields) throws PassingConstructorException, NoSuchFieldException, NotSupportedException {
        Selection[] selections = new Selection[fields.length];
        applyJoins(root);
        PassingExtractor<D> extractor = new ConstructorPassingExtractor<>(targetClass);
        extractor.extractNames(fields);
        selections = extractor.extractSelections(root, cb);
        mappings = extractor.getMappings();
        cq.select(cb.construct(this.targetClass, selections));
        return this;
    }

    public static <S, D> CQueryBuilder<S, D> from(Class<S> entityClass, EntityManager em) {
        CQueryBuilder<S, D> builder = new CQueryBuilder<>(em);
        builder.entityClass = entityClass;
        builder.cq = (CriteriaQuery<D>) builder.cb.createQuery(entityClass);
        builder.root = builder.cq.from(entityClass);
        return builder;
    }

    public CQueryBuilder<S, D> and(PredicateMatcher matcher) throws NotSupportedException {
        if (commonPredicate == null) {
            return where(matcher);
        } else {
            commonPredicate = cb.and(commonPredicate, matcher.predicate(root, cb, mappings));
        }
        cq = cq.where(commonPredicate);
        return this;
    }

    public CQueryBuilder<S, D> or(PredicateMatcher matcher) throws NotSupportedException {
        if (commonPredicate == null) {
            return where(matcher);
        } else {
            commonPredicate = cb.or(commonPredicate, matcher.predicate(root, cb, mappings));
        }
        cq = cq.where(commonPredicate);
        return this;
    }

    public CriteriaQuery<D> query() {
        return cq;
    }

    public Page<D> listPageable(Pageable pageable) throws NotSupportedException {
        CriteriaQuery<D> query = cb.createQuery(targetClass);
        JpaUtils.copyCriteriaWithoutSelectionAndOrder(cq, query, true);
        long count = count();
        sort(pageable);
        TypedQuery<D> limitedQuery = em.createQuery(cq);
        limitedQuery.setFirstResult(pageable.getOffset());
        limitedQuery.setMaxResults(pageable.getPageSize());
        List<D> results = limitedQuery.getResultList();
        Page<D> page = new PageImpl<>(results, pageable, count);
        return page;
    }

    public void sort(Pageable pageable) {
        if (pageable.getSort() != null) {
            List<Order> orderList = new ArrayList();
            for (Sort.Order order : pageable.getSort()) {
                String propName = order.getProperty();
                Expression expression = mappings.get(propName);
                if (order.getDirection().equals(Sort.Direction.ASC)) {
                    orderList.add(cb.asc(expression));
                }
                if (order.getDirection().equals(Sort.Direction.DESC)) {
                    orderList.add(cb.desc(expression));
                }
            }
            cq.orderBy(orderList);
        }
    }

    public String getRootIdFieldName() {
        String idFieldName = null;
        for (Field field : entityClass.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.getAnnotation(Id.class) != null) {
                idFieldName = field.getName();
            }

        }

        return idFieldName;
    }

    public Long count() throws NotSupportedException {
        List<Long> counts = new ArrayList<>();
        Long result = 0L;
        counts = JpaUtils.count(em, cq);
        if (grouper != null) {
            result = (long) counts.size();
        } else {
            result = (counts.isEmpty()) ? 0L : counts.get(0);
        }

        return result;
    }


    public List<D> list() {
        return em.createQuery(cq).getResultList();
    }

    private void applyJoins(Root<S> root) {
        Join lastJoin = null;
        for (Map.Entry<String, List<Pair<String, JoinType>>> entry : joinsQueues.entrySet()) {
            Integer i = 0;
            for (Pair<String, JoinType> currentJoinParams : entry.getValue()) {
                if (i == 0) {
                    lastJoin = root.join(currentJoinParams.getKey(), currentJoinParams.getValue());
                    lastJoin.alias(JpaUtils.getOrCreateAlias(lastJoin));
                    i++;
                    continue;
                }
                lastJoin = lastJoin.join(currentJoinParams.getKey(), currentJoinParams.getValue());
                lastJoin.alias(JpaUtils.getOrCreateAlias(lastJoin));
                i++;
            }
        }
    }

    public CQueryBuilder<S, D> groupBy(String... fields) {
        QueryGrouper<S> grouper = GrouperFactory.createGrouper(fields);

        return groupBy(grouper);
    }

    public CQueryBuilder<S, D> groupBy(QueryGrouper<S> grouper) {

        this.grouper = grouper;

        if (commonPredicate != null) {
            cq = cq.where(commonPredicate);
        }

        if (grouper != null) {
            cq = grouper.getGroupedQuery(root, cq);
        }

        return this;
    }


}