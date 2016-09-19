package com.projecta.bobby.commons.cquerybuilder.extractors;

import com.projecta.bobby.commons.cquerybuilder.exceptions.NotSupportedException;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import java.util.Map;

/**
 * Created by vankor on 1/20/16.
 */
public interface PassingExtractor<T> {

    String[] extractNames(String... fields) throws NoSuchFieldException;

    Selection[] extractSelections(Root<?> root, CriteriaBuilder cb) throws NotSupportedException, NoSuchFieldException;

    Map<String, Expression> getMappings();

    void extractFilterProps(Root<?> root, CriteriaBuilder cb) throws NotSupportedException, NoSuchFieldException;
}
