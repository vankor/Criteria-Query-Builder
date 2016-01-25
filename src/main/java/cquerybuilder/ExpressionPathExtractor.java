package cquerybuilder;

import cquerybuilder.exceptions.NotSupportedException;

import javax.persistence.criteria.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by vankor on 1/16/16.
 */
public abstract class ExpressionPathExtractor<T>  {

    protected String fieldName;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Expression<?> getExpressionPath(Root<T> root, CriteriaBuilder cb) throws NotSupportedException {
         return aggregateFunc(root, this.fieldName, cb);
    }

    public String[] getFieldPrefix(String fullName){
        String[] parts = this.fieldName.split("\\.");
        String[] result = new String[2];
        String delim = ".";
        if(parts.length > 1){
            result[0] = parts[0];
            result[1] = "";
            for(int i = 1; i < parts.length; i++){
                if(i == parts.length - 1){delim = "";}
                result[1] += parts[i]+delim;
            }
        }
        return result;
    }

    public static <T> Expression<? extends Comparable> getFieldPath(Root<T> root, String fieldName){
        String[] parts = null;
        Path<? extends Comparable> objectPath = null;
        if(fieldName.contains(".")){
            parts = fieldName.split("\\.");
        }

        if(parts != null){
            int i = 0;
            Join currentJoin = null;
            objectPath = root.<String>get(parts[i]);
                for(Join join : root.getJoins()){
                    if(parts[i].equals(join.getAttribute().getName())){
                        currentJoin = join;
                        break;
                    }
                }

            i++;

            while(i < parts.length){
                if(currentJoin != null) {
                    objectPath = currentJoin.<String>get(parts[i]);
                    for (Object el : currentJoin.getJoins()) {
                        Join join = (Join) el;
                        if (parts[i].equals(join.getAttribute().getName())) {
                            currentJoin = join;
                            break;
                        }
                    }
                }
                else {
                    objectPath = objectPath.<String>get(parts[i]);
                }
                i++;
            }
        }

        else if(fieldName != null) {
                objectPath = root.<String>get(fieldName);
        }

        return (Expression<? extends Comparable>)objectPath;
    }

    public static <T> Expression<?> aggregateFunc(Root<T> root, String fieldName, CriteriaBuilder cb) throws NotSupportedException {
        String agrument = null;
        String funcName = null;
        if(fieldName.contains("(")){
            funcName = fieldName.split("\\(")[0];
        }
        else{
            return getFieldPath(root, fieldName);
        }

        Pattern p = Pattern.compile("\\((.*?)\\)");
        Matcher m = p.matcher(fieldName);
        if(m.find()) {
            agrument =  m.group(1);
        }

        Path<? extends Number> objectPath = (Path<? extends Number>)getFieldPath(root, agrument);

        Expression<? extends Number> aggregateExpression = null;

        switch(funcName){
            case "SUM" :
                aggregateExpression = cb.sum(objectPath);
                break; //optional
            case "AVG" :
                aggregateExpression = cb.avg(objectPath);
                break; //optional
            case "COUNT" :
                aggregateExpression = cb.count(objectPath);
                break;
            case "MAX" :
                aggregateExpression = cb.max(objectPath);
                break;
            case "MIN" :
                aggregateExpression = cb.max(objectPath);
                break;
            default :
                throw new NotSupportedException("Function "+funcName+" is not supported!");
        }

        return aggregateExpression;

    }

}
