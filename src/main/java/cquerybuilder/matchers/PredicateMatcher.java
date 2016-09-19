package com.projecta.bobby.commons.cquerybuilder.matchers;

import com.projecta.bobby.commons.cquerybuilder.exceptions.NotSupportedException;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Map;

/**
 * Created by vankor on 1/16/16.
 */
public interface PredicateMatcher<T> {

    Predicate predicate(Root<T> root, CriteriaBuilder cb, Map<String, Expression> mappings) throws NotSupportedException;

}
