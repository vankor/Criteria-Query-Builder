package com.projecta.bobby.commons.cquerybuilder.matchers;

import com.projecta.bobby.commons.cquerybuilder.exceptions.NotSupportedException;
import com.projecta.bobby.commons.cquerybuilder.extractors.ExpressionPathExtractor;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Map;

/**
 * Created by vankor on 1/16/16.
 */
public class BetweenMatcher<T extends Comparable<? super T>> extends ExpressionPathExtractor<T> implements PredicateMatcher<T> {

    private T fieldValueFrom;
    private T fieldValueTo;

    public BetweenMatcher(String fieldName, T fieldValueFrom, T fieldValueTo) {
        this.fieldName = fieldName;
        this.fieldValueFrom = fieldValueFrom;
        this.fieldValueTo = fieldValueTo;
    }

    public T getFieldValueFrom() {
        return fieldValueFrom;
    }

    public void setFieldValueFrom(T fieldValueFrom) {
        this.fieldValueFrom = fieldValueFrom;
    }

    public T getFieldValueTo() {
        return fieldValueTo;
    }

    public void setFieldValueTo(T fieldValueTo) {
        this.fieldValueTo = fieldValueTo;
    }

    @Override
    public Predicate predicate(Root<T> root, CriteriaBuilder cb, Map<String, Expression> mappings) throws NotSupportedException {
        Predicate predicate = null;
        if (fieldName != null) {
            Expression objectPath = mappings.get(this.fieldName);
            if (objectPath == null) {
                objectPath = getExpressionPath(root, cb);
            }
            predicate = cb.between(objectPath, fieldValueFrom, fieldValueTo);
        }
        return predicate;

    }


}
