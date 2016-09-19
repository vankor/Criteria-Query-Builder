package com.projecta.bobby.commons.cquerybuilder.matchers;

import com.projecta.bobby.commons.cquerybuilder.exceptions.NotSupportedException;
import com.projecta.bobby.commons.cquerybuilder.extractors.ExpressionPathExtractor;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Map;

/**
 * Created by vankor on 1/16/16.
 */
public class InMatcher<T extends Comparable<? super T>> extends ExpressionPathExtractor<T> implements PredicateMatcher<T> {

    private List<T> fieldValues;

    public InMatcher(String fieldName, List<T> fieldValues) {
        this.fieldName = fieldName;
        this.fieldValues = fieldValues;
    }

    public Object getFieldValue() {
        return fieldValues;
    }

    public void setFieldValue(List<T> fieldValues) {
        this.fieldValues = fieldValues;
    }

    @Override
    public Predicate predicate(Root<T> root, CriteriaBuilder cb, Map<String, Expression> mappings) throws NotSupportedException {
        Predicate predicate = null;
        Expression objectPath = mappings.get(this.fieldName);
        if (objectPath == null) {
            objectPath = getExpressionPath(root, cb);
        }
        return objectPath.in(fieldValues);
    }


}
