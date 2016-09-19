package com.projecta.bobby.commons.cquerybuilder.groupers;

import com.google.common.collect.Lists;
import com.projecta.bobby.commons.cquerybuilder.extractors.ExpressionPathExtractor;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import java.util.Arrays;
import java.util.List;

/**
 * Created by vankor on 1/16/16.
 */
public class QueryGrouper<T> {

    private List<String> groupingFields;

    public QueryGrouper(String... groupingFields) {
        this.groupingFields = Arrays.asList(groupingFields);
    }

    public <D> CriteriaQuery<D> getGroupedQuery(Root<T> root, CriteriaQuery<D> query) {
        List<Expression<?>> expresions = Lists.newArrayList();
        for (String fieldName : groupingFields) {
            Expression<?> expression = ExpressionPathExtractor.getFieldPath(root, fieldName);
            if (expression != null) {
                expresions.add(expression);
            }
        }


        return query.groupBy(expresions);

    }
}
