package com.projecta.bobby.commons.cquerybuilder.utils;

import com.projecta.bobby.commons.cquerybuilder.annotations.FilterProps;
import com.projecta.bobby.commons.cquerybuilder.annotations.ResultProp;
import com.projecta.bobby.commons.cquerybuilder.exceptions.PassingFieldsException;

import javax.persistence.criteria.Expression;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by vankor on 1/20/16.
 */
public class MappingFieldsUtil<T> {
    public static <T> String getStringPathByFieldName(String fieldName, Class<T> targetClass) throws NoSuchFieldException {
        Field field = targetClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        ResultProp resultProp = field.getAnnotation(ResultProp.class);
        if(resultProp == null){
            throw new PassingFieldsException("Result field "+field.getName()+" is not annotated with @ResultProp in the target class " + targetClass.getName());
        }
        return resultProp.value();
    }

    public static void sortFieldsByOrder(Field[] fields) throws NoSuchFieldException {

        Arrays.sort(fields, new Comparator<Field>() {
            @Override
            public int compare(Field f1, Field f2) {
                ResultProp rp1 = f1.getAnnotation(ResultProp.class);
                ResultProp rp2 = f2.getAnnotation(ResultProp.class);
                return Integer.compare(rp1.order(), rp2.order());
            }
        });

    }

    public static Field[] getResultMappingFields(Field[] fields) throws NoSuchFieldException {

        List<Field> listResult = new ArrayList<>();
        for(Field field : fields){
            field.setAccessible(true);
            if(field.getAnnotation(ResultProp.class) !=null){
                listResult.add(field);
            }
        }
        Field[] result = new Field[listResult.size()];
        return listResult.toArray(result);

    }

    public static Map<String, String> getFilterMappingFields(Class clazz) throws NoSuchFieldException {
        Map<String, String> res = new HashMap<>();
        FilterProps filterMappings = null;
         if(clazz.isAnnotationPresent(FilterProps.class)){
             filterMappings = (FilterProps)clazz.getAnnotation(FilterProps.class);
         }
        if(filterMappings != null){
            String[] arr = filterMappings.value();
            for(int i = 0; i < arr.length-1; i++){
                res.put(arr[i], arr[i+1]);
            }
        }
        return res;

    }
}
