package cquerybuilder.matchers;

import cquerybuilder.exceptions.NotSupportedException;

import javax.persistence.criteria.*;
import java.util.Map;

/**
 * Created by vankor on 1/16/16.
 */
public interface PredicateMatcher<T> {

    Predicate predicate(Root<T> root, CriteriaBuilder cb, Map<String, Expression> mappings) throws NotSupportedException;

}
