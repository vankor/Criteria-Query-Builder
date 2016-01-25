package cquerybuilder.matchers;

import cquerybuilder.ExpressionPathExtractor;
import cquerybuilder.PredicateMatcher;
import cquerybuilder.exceptions.NotSupportedException;

import javax.persistence.criteria.*;
import java.util.Map;

/**
 * Created by vankor on 1/16/16.
 */
public class ContainsMatcher<T extends Comparable <? super T>> extends ExpressionPathExtractor<T> implements PredicateMatcher<T> {

    private T fieldValue;

    public ContainsMatcher(String fieldName, T fieldValue) {
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
        if(objectPath == null){
            objectPath = getExpressionPath(root, cb);
        }
        predicate = cb.like(objectPath, "%" + ((fieldValue == null) ? "" : fieldValue) + "%");

        return predicate;

    }





}
