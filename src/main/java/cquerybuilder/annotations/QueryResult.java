package cquerybuilder.annotations;

import java.lang.annotation.*;

/**
 * Created by vankor on 1/20/16.
 */
@Target({ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface QueryResult {
}
