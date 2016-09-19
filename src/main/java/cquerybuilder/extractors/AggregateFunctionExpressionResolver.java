package com.projecta.bobby.commons.cquerybuilder.extractors;

import com.google.common.collect.Sets;
import com.projecta.bobby.commons.cquerybuilder.exceptions.NotSupportedException;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import java.util.Set;

import static com.projecta.bobby.commons.cquerybuilder.extractors.ExpressionPathExtractor.calculateExpression;

/**
 * Created by vankor on 4/14/16.
 */
public class AggregateFunctionExpressionResolver {


    public static Set<String> aggregateFunctions = Sets.newHashSet("SUM", "AVG", "COUNT", "MAX", "MIN");

    public static boolean isAggregateFunction(String text) {
        for (String func : aggregateFunctions) {
            if (text.startsWith(func)) {
                return true;
            }
        }
        return false;
    }

    public static Expression<? extends Number> resolveAggregateFunction(String funcName, String agrument, Root<?> root, CriteriaBuilder cb) {
        Expression<? extends Number> objectPath = (Expression<? extends Number>) calculateExpression(root, agrument, cb);
        switch (funcName) {
            case "SUM":
                objectPath = cb.sum(objectPath);
                break; //optional
            case "AVG":
                objectPath = cb.avg(objectPath);
                break; //optional
            case "COUNT":
                objectPath = cb.count(objectPath);
                break;
            case "MAX":
                objectPath = cb.max(objectPath);
                break;
            case "MIN":
                objectPath = cb.max(objectPath);
                break;
            default:
                throw new NotSupportedException("Function " + funcName + " is not supported!");
        }

        return objectPath;
    }
}
