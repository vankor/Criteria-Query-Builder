package com.projecta.bobby.commons.cquerybuilder.exceptions;

/**
 * Created by parkee on 2/29/16.
 */
public class NotValidExpressionException extends RuntimeException {

    public NotValidExpressionException() {
    }

    public NotValidExpressionException(String message) {
        super(message);
    }

    public NotValidExpressionException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotValidExpressionException(Throwable cause) {
        super(cause);
    }
}
