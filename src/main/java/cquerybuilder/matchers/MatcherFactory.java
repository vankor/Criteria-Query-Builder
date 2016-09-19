package com.projecta.bobby.commons.cquerybuilder.matchers;

import java.util.Arrays;

/**
 * Created by vankor on 1/19/16.
 */
public class MatcherFactory {

    public static <T extends Comparable<T>> PredicateMatcher createPredicate(String fieldName, Matchers matcherType, T... values) throws IllegalArgumentException {
        PredicateMatcher<T> predicateMather = null;
        switch (matcherType) {
            case EQUALS:
                if (values.length != 1) {
                    throw new IllegalArgumentException("You have to only one argument to equals!");
                }
                predicateMather = new EqualsMatcher<T>(fieldName, values[0]);
                break;
            case BETWEEN:
                if (values.length != 2) {
                    throw new IllegalArgumentException("You have to set upper and lower bounds!");
                }
                predicateMather = new BetweenMatcher<T>(fieldName, values[0], values[1]);
                break;
            case CONTAINS:
                if (values.length != 1) {
                    throw new IllegalArgumentException("You have to pass only one argument to CONTAINS!");
                }
                predicateMather = new ContainsMatcher<T>(fieldName, values[0]);
                break;
            case IN:
                if (values.length < 1) {
                    throw new IllegalArgumentException("You have to pass at least one argument to IN!");
                }
                predicateMather = new InMatcher<T>(fieldName, Arrays.asList(values));
                break;
            case COLLECTION_CONTAINS:
                if (values.length < 1) {
                    throw new IllegalArgumentException("You have to pass at least one argument to COLLECTION_CONTAINS!");
                }
                predicateMather = new ContainsCollectionMatcher<T>(fieldName, values[0]);
                break;
        }

        return predicateMather;
    }
}
