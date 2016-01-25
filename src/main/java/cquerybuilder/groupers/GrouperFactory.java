package cquerybuilder.groupers;

/**
 * Created by vankor on 1/19/16.
 */
public class GrouperFactory {

    public static  QueryGrouper createGrouper(String ... groupingFields) throws IllegalArgumentException{
        return new QueryGrouper(groupingFields);
    }

}
