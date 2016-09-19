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
public class EqualsMatcher<T extends Comparable<? super T>> extends ExpressionPathExtractor<T> implements PredicateMatcher<T> {

    private T fieldValue;

    public EqualsMatcher() {
    }

    public EqualsMatcher(String fieldName, T fieldValue) {
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public Object getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(T fieldValue) {
        this.fieldValue = fieldValue;
    }

    @Override
    public Predicate predicate(Root<T> root, CriteriaBuilder cb, Map<String, Expression> mappings) throws NotSupportedException {
        Predicate predicate = null;
        Expression objectPath = mappings.get(this.fieldName);
        if (objectPath == null) {
            objectPath = getExpressionPath(root, cb);
        }
        if (fieldName != null) {
            predicate = cb.equal(objectPath, ((fieldValue == null) ? "" : fieldValue));
        }
        return predicate;

    }


}
