package com.projecta.bobby.commons.cquerybuilder.exceptions;

/**
 * Created by parkee on 2/29/16.
 */
public class FieldNotFoundException extends RuntimeException {

    public FieldNotFoundException() {
    }

    public FieldNotFoundException(String message) {
        super(message);
    }

    public FieldNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public FieldNotFoundException(Throwable cause) {
        super(cause);
    }
}
