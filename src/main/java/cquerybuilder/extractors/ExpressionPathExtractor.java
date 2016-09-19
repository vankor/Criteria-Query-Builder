package com.projecta.bobby.commons.cquerybuilder.extractors;

import com.projecta.bobby.commons.cquerybuilder.exceptions.NotSupportedException;
import com.projecta.bobby.commons.cquerybuilder.exceptions.NotValidExpressionException;
import org.hibernate.MappingException;
import org.jsoup.helper.StringUtil;

import javax.persistence.criteria.*;

/**
 * Created by vankor on 1/16/16.
 */
public abstract class ExpressionPathExtractor<T> {

    protected String fieldName;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Expression<?> getExpressionPath(Root<T> root, CriteriaBuilder cb) throws NotSupportedException {
        return calculateExpression(root, this.fieldName, cb);
    }

    public static <T> Expression<? extends Comparable> getFieldPath(Root<T> root, String fieldName) {


        String[] parts = new String[]{fieldName};
        Path<? extends Comparable> objectPath = null;
        if (fieldName.contains(".")) {
            parts = fieldName.split("\\.");
        }

        int i = 0;
        Join currentJoin = null;
        objectPath = root.<String>get(parts[i]);
        for (Join join : root.getJoins()) {
            if (parts[i].equals(join.getAttribute().getName())) {
                currentJoin = join;
                break;
            }
        }

        i++;

        if (i == parts.length) {
            return (currentJoin != null) ? currentJoin : objectPath;
        }

        while (i < parts.length) {
            if (currentJoin != null) {
                try {
                    objectPath = currentJoin.<String>get(parts[i]);
                } catch (MappingException ex) {

                }
                for (Object el : currentJoin.getJoins()) {
                    Join join = (Join) el;
                    if (parts[i].equals(join.getAttribute().getName())) {
                        currentJoin = join;
                        break;
                    }
                }
            } else {
                objectPath = objectPath.<String>get(parts[i]);
            }
            i++;
        }

        return objectPath;
    }

    private static String getTextBetweenBrackets(String full) {
        if (!MathExpressionResolver.isValidExpression(full)) {
            throw new NotValidExpressionException("Not valid brackets in expression: " + full + "!");
        }
        int start = full.indexOf("(");
        int end = full.lastIndexOf(")");
        return full.substring(start + 1, end);
    }


    public static <T> Expression<?> calculateExpression(Root<T> root, String fieldName, CriteriaBuilder cb) throws NotSupportedException {
        String funcName = null;

        Boolean isExpression = MathExpressionResolver.isMathExpression(fieldName);

        Boolean isAggregateFunction = AggregateFunctionExpressionResolver.isAggregateFunction(fieldName);

        Boolean isNumeric = StringUtil.isNumeric(fieldName);

        if (isAggregateFunction) {
            funcName = fieldName.split("\\(")[0];
        } else {
            if (isNumeric) {
                return cb.literal(Double.parseDouble(fieldName));
            }
            if (isExpression) {
                return MathExpressionResolver.resolveMathExpression(fieldName, cb, root);
            }
            return getFieldPath(root, fieldName);
        }

        String agrument = getTextBetweenBrackets(fieldName);

        return AggregateFunctionExpressionResolver.resolveAggregateFunction(funcName, agrument, root, cb);

    }

}
