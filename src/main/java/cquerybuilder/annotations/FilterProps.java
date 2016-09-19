package com.projecta.bobby.commons.cquerybuilder.annotations;

import java.lang.annotation.*;

/**
 * Created by vankor on 1/20/16.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FilterProps {
    String[] value();
}
