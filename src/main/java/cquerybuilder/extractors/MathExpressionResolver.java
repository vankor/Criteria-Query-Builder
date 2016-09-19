package com.projecta.bobby.commons.cquerybuilder.extractors;

import com.google.common.collect.Sets;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import java.util.*;
import java.util.stream.Collectors;

import static com.projecta.bobby.commons.cquerybuilder.extractors.ExpressionPathExtractor.calculateExpression;

/**
 * Created by vankor on 4/14/16.
 */
public class MathExpressionResolver {


    public static final Set<String> mathOperators = Sets.newHashSet("+", "-", "*", "/");

    public static <T> Expression<? extends Number> resolveMathExpression(String s, CriteriaBuilder cb, Root<T> root) {
        // delete white spaces
        s = s.replaceAll(" ", "");
        Stack<String> stack = new Stack<String>();
        Map<String, Expression<? extends Number>> expressionMap = new HashMap<String, Expression<? extends Number>>();
        char[] arr = s.toCharArray();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == ' ')
                continue;

            if (Character.isLetter(arr[i]) || Character.isDigit(arr[i]) || arr[i] == '.') {
                sb.append(arr[i]);

                if (i == arr.length - 1) {
                    stack.push(sb.toString());
                }
            } else {
                String beforePushTop = null;
                if (!stack.isEmpty()) {
                    beforePushTop = stack.peek();
                }
                if (sb.length() > 0) {
                    stack.push(sb.toString());
                    sb = new StringBuilder();
                }

                if (arr[i] == '(' && !stack.isEmpty()) {
                    String funcSuposed = stack.pop();
                    if (AggregateFunctionExpressionResolver.isAggregateFunction(funcSuposed)) {
                        stack.push(funcSuposed + arr[i]);
                    }
                    continue;
                }

                if (arr[i] != ')') {
                    stack.push(new String(new char[]{arr[i]}));
                } else {
                    if (beforePushTop != null &&
                            AggregateFunctionExpressionResolver.isAggregateFunction(beforePushTop)
                            && !beforePushTop.endsWith(")")) {
                        String top = stack.pop();
                        if (!stack.isEmpty()) {
                            stack.pop();
                        }
                        stack.push(beforePushTop + top + arr[i]);
                        continue;
                    }

                    // when meet ')', pop and calculate
                    ArrayList<String> t = new ArrayList<String>();
                    while (!stack.isEmpty()) {
                        String top1 = stack.pop();
                        if (top1.equals("(")) {
                            break;
                        } else {
                            t.add(0, top1);
                        }
                    }

                    String subexpr = t.stream().collect(Collectors.joining());
                    stack.push(subexpr);
                    expressionMap.put(subexpr, (Expression<? extends Number>) calculateExpression(root, subexpr, cb));
                }
            }
        }

        ArrayList<String> t = new ArrayList<String>();
        while (!stack.isEmpty()) {
            String elem = stack.pop();
            t.add(0, elem);
        }

        Expression<? extends Number> result = null;
        if (t.size() == 1) {
            result = (Expression<? extends Number>) calculateExpression(root, t.get(0), cb);
        }
        for (int i = t.size() - 1; i > 0; i = i - 2) {
            if (i > 1) {
                Expression<? extends Number> expr1 = expressionMap.get(t.get(i - 2));
                result = expr1 != null ? expr1 : (Expression<? extends Number>) calculateExpression(root, t.get(i - 2), cb);
            }
            Expression<? extends Number> expr2 = expressionMap.get(t.get(i));
            Expression<? extends Number> exp = expr2 != null ? expr2 : (Expression<? extends Number>) calculateExpression(root, t.get(i), cb);
            if (t.get(i - 1).equals("-")) {
                result = (result != null) ? cb.diff(result, exp) : exp;
            } else if (t.get(i - 1).equals("+")) {
                result = (result != null) ? cb.sum(result, exp) : exp;
            } else if (t.get(i - 1).equals("*")) {
                result = (result != null) ? cb.prod(result, exp) : exp;
            } else if (t.get(i - 1).equals("/")) {
                result = (result != null) ? cb.quot(result, exp) : exp;
            }
        }

        return result;
    }

    public static boolean isMathExpression(String text) {
        for (String func : mathOperators) {
            if (text.contains(func)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidExpression(String s) {
        HashMap<Character, Character> map = new HashMap<Character, Character>();
        map.put('(', ')');
        map.put('[', ']');
        map.put('{', '}');

        Stack<Character> stack = new Stack<Character>();

        for (int i = 0; i < s.length(); i++) {
            char curr = s.charAt(i);

            if (map.keySet().contains(curr)) {
                stack.push(curr);
            } else if (map.values().contains(curr)) {
                if (!stack.empty() && map.get(stack.peek()) == curr) {
                    stack.pop();
                } else {
                    return false;
                }
            }
        }

        return stack.empty();
    }
}
