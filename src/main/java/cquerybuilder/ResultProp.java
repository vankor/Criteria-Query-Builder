package cquerybuilder;

import java.lang.annotation.*;

/**
 * Created by vankor on 1/20/16.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResultProp {
    String value();
    int order();
}
