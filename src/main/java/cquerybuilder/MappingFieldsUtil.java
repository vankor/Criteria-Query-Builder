package cquerybuilder;

import cquerybuilder.exceptions.PassingFieldsException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

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
}
