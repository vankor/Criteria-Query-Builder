package com.projecta.bobby.commons.cquerybuilder;

import com.google.common.collect.Lists;
import com.projecta.bobby.commons.cquerybuilder.exceptions.FieldNotFoundException;
import com.projecta.bobby.commons.cquerybuilder.exceptions.NotSupportedException;
import com.projecta.bobby.commons.cquerybuilder.extractors.ConstructorPassingExtractor;
import com.projecta.bobby.commons.cquerybuilder.extractors.FieldsPassingExtractor;
import com.projecta.bobby.commons.cquerybuilder.extractors.PassingExtractor;
import com.projecta.bobby.commons.cquerybuilder.groupers.GrouperFactory;
import com.projecta.bobby.commons.cquerybuilder.groupers.QueryGrouper;
import com.projecta.bobby.commons.cquerybuilder.matchers.MatcherFactory;
import com.projecta.bobby.commons.cquerybuilder.matchers.Matchers;
import com.projecta.bobby.commons.cquerybuilder.matchers.PredicateMatcher;
import com.projecta.bobby.commons.cquerybuilder.utils.JpaUtils;
import com.projecta.framework.backend.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
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
    protected Map<String, Set<Pair<String, JoinType>>> joinsQueues = new HashMap<>();
    protected Map<String, Expression> mappings = new HashMap<>();
    private static final Logger LOG = LoggerFactory.getLogger(CQueryBuilder.class);

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
            Set<Pair<String, JoinType>> queue = joinsQueues.get(base);
            if (i == 0) {
                if (queue == null) {
                    queue = new LinkedHashSet<>();
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
        cq = cq.where(commonPredicate);
        return this;
    }

    public CQueryBuilder<S, D> into(Class<D> tagretClass) {
        this.targetClass = tagretClass;
        cq = cb.createQuery(tagretClass);
        root = cq.from(entityClass);
        applyJoins(root);
        PassingExtractor<D> extractor = new FieldsPassingExtractor<>(targetClass);
        Selection[] selections = new Selection[0];
        try {
            selections = extractor.extractSelections(root, cb);
            extractor.extractFilterProps(root, cb);
        } catch (NoSuchFieldException e) {
            throw new FieldNotFoundException(e);
        }
        mappings = extractor.getMappings();
        cq.select(cb.construct(this.targetClass, selections));

        return this;
    }

    public CQueryBuilder<S, D> passing(String... fields) {
        Selection[] selections = new Selection[fields.length];
        applyJoins(root);
        PassingExtractor<D> extractor = new ConstructorPassingExtractor<>(targetClass);
        try {
            extractor.extractNames(fields);
            selections = extractor.extractSelections(root, cb);
        } catch (NoSuchFieldException e) {
            throw new FieldNotFoundException(e);
        }
        mappings = extractor.getMappings();
        cq.select(cb.construct(this.targetClass, selections));
        return this;
    }

    public static <S, D> CQueryBuilder<S, D> from(Class<S> entityClass, EntityManager em) {
        CQueryBuilder<S, D> builder = new CQueryBuilder<>(em);
        builder.entityClass = entityClass;
        builder.targetClass = (Class<D>) entityClass;
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
        TypedQuery<D> limitedQuery = null;
        filterNotAnnotatedFieldsFromOrdering();
        if (targetClass == null || entityClass.getCanonicalName().equals(targetClass.getCanonicalName())) {
            limitedQuery = em.createQuery(query);
        } else {
            limitedQuery = em.createQuery(cq);
        }
        limitedQuery.setFirstResult(pageable.getOffset());
        limitedQuery.setMaxResults(pageable.getPageSize());
        List<D> results = limitedQuery.getResultList();
        Page<D> page = new PageImpl<>(results, pageable, count);

        JpaUtils.aliasCount = 0;
        return page;
    }

    private void filterNotAnnotatedFieldsFromOrdering() {
        Iterator<Order> iter = cq.getOrderList().iterator();
        while (iter.hasNext()) {
            Order order = iter.next();
            if (order.getExpression() == null) {
                iter.remove();
            }
        }
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

    public Long count() throws NotSupportedException {
        List<Long> counts = Lists.newArrayList();
        Long result = 0L;
        counts = JpaUtils.count(em, cq);
        if (grouper != null) {
            result = (long) counts.size();
        } else {
            result = (counts.isEmpty()) ? 0L : counts.get(0);
        }
        JpaUtils.aliasCount = 0;
        return result;
    }


    public List<D> list() {
        try {
            return em.createQuery(cq).getResultList();
        } finally {
            JpaUtils.aliasCount = 0;
        }
    }

    public D single() {
        return em.createQuery(cq).getSingleResult();
    }

    private void applyJoins(Root<S> root) {
        Join lastJoin = null;
        for (Map.Entry<String, Set<Pair<String, JoinType>>> entry : joinsQueues.entrySet()) {
            Integer i = 0;
            for (Pair<String, JoinType> currentJoinParams : entry.getValue()) {
                if (i == 0) {
                    lastJoin = root.join(currentJoinParams.getFirst(), currentJoinParams.getSecond());
                    lastJoin.alias(JpaUtils.getOrCreateAlias(lastJoin));
                    i++;
                    continue;
                }
                lastJoin = lastJoin.join(currentJoinParams.getFirst(), currentJoinParams.getSecond());
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