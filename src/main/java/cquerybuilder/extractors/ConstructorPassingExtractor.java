package com.projecta.bobby.commons.cquerybuilder.extractors;

import com.projecta.bobby.commons.cquerybuilder.annotations.QueryResult;
import com.projecta.bobby.commons.cquerybuilder.exceptions.NotSupportedException;
import com.projecta.bobby.commons.cquerybuilder.exceptions.PassingConstructorException;
import com.projecta.bobby.commons.cquerybuilder.utils.MappingFieldsUtil;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import java.lang.reflect.Constructor;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by vankor on 1/20/16.
 */
public class ConstructorPassingExtractor<T> implements PassingExtractor<T> {

    private Class<T> targetClass;

    String[] paramNames;
    Map<String, Expression> mappings = new HashMap<>();
    String[] fields;


    public ConstructorPassingExtractor(Class<T> targetClass) {
        this.targetClass = targetClass;
    }

    @Override
    public String[] extractNames(String... fields) {
        Constructor passingConstructor = null;
        for (Constructor constructor : targetClass.getDeclaredConstructors()) {
            if (constructor.getAnnotation(QueryResult.class) != null) {
                passingConstructor = constructor;
                break;
            }
        }

        if (passingConstructor == null) {
            throw new PassingConstructorException("There is no passing constructor annotated with @QueryResult in the target class " + targetClass.getName());
        }

        TypeVariable[] params = passingConstructor.getTypeParameters();

        if (params.length != fields.length) {
            throw new PassingConstructorException("Passing parameters count does not match passing constructor parameter count!");
        }

        String[] defaultParamNames = null;
        String[] paramNames = new String[params.length];

        for (int i = 0; i < params.length; i++) {
            ParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();
            defaultParamNames = discoverer.getParameterNames(passingConstructor);
            if (defaultParamNames == null) {
                throw new PassingConstructorException("Cannot pass constructor parameters!");
            }
            paramNames[i] = defaultParamNames[i];
        }

        this.paramNames = paramNames;
        this.fields = fields;

        return paramNames;
    }

    @Override
    public Selection[] extractSelections(Root<?> root, CriteriaBuilder cb) throws NotSupportedException, NoSuchFieldException {
        Selection[] selections = null;
        if (paramNames != null && fields != null) {
            selections = new Selection[paramNames.length];
            for (int i = 0; i < selections.length; i++) {
                Expression expression = ExpressionPathExtractor.calculateExpression(root, fields[i], cb);
                mappings.put(paramNames[i], expression);
                selections[i] = expression;
            }
        }
        return selections;
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
