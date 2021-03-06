package com.projecta.bobby.commons.cquerybuilder.extractors;

import com.google.common.collect.Lists;
import com.projecta.bobby.commons.cquerybuilder.exceptions.NotSupportedException;
import com.projecta.bobby.commons.cquerybuilder.utils.MappingFieldsUtil;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by vankor on 1/20/16.
 */
public class FieldsPassingExtractor<T> implements PassingExtractor<T> {

    private Class<T> targetClass;

    String[] paramNames;

    Map<String, Expression> mappings = new HashMap<>();

    public FieldsPassingExtractor(Class<T> targetClass) {
        this.targetClass = targetClass;
    }

    @Override
    public String[] extractNames(String... fields) throws NoSuchFieldException {
        Field[] targetFiels = targetClass.getDeclaredFields();
        targetFiels = MappingFieldsUtil.getResultMappingFields(targetFiels);
        List<String> resultFieldNames = Lists.newArrayList();
        for (Field fName : targetFiels) {
            fName.setAccessible(true);
            resultFieldNames.add(fName.getName());
        }
        paramNames = new String[resultFieldNames.size()];
        paramNames = resultFieldNames.toArray(paramNames);

        return paramNames;
    }


    @Override
    public Selection[] extractSelections(Root<?> root, CriteriaBuilder cb) throws NotSupportedException, NoSuchFieldException {
        Field[] targetFiels = targetClass.getDeclaredFields();
        targetFiels = MappingFieldsUtil.getResultMappingFields(targetFiels);
        MappingFieldsUtil.sortFieldsByOrder(targetFiels);
        List<Selection> listSelection = new ArrayList<>();
        for (Field field : targetFiels) {
            field.setAccessible(true);
            String path = MappingFieldsUtil.getStringPathByFieldName(field.getName(), targetClass);
            Expression expression = ExpressionPathExtractor.calculateExpression(root, path, cb);
            mappings.put(field.getName(), expression);
            listSelection.add(expression);
        }

        Selection[] arraySelection = new Selection[listSelection.size()];
        return listSelection.toArray(arraySelection);
    }

    @Override
    public void extractFilterProps(Root<?> root, CriteriaBuilder cb) throws NotSupportedException, NoSuchFieldException {
        Map<String, String> filterMappings = MappingFieldsUtil.getFilterMappingFields(targetClass);
        for (Map.Entry<String, String> entry : filterMappings.entrySet()) {
            Expression expression = ExpressionPathExtractor.calculateExpression(root, entry.getValue(), cb);
            mappings.put(entry.getKey(), expression);
        }
    }

    @Override
    public Map<String, Expression> getMappings() {
        return mappings;
    }
}
