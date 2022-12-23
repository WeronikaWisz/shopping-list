package com.shoppinglist.shoppinglistapp.exception;

public class ApiForbiddenException extends RuntimeException {

    public ApiForbiddenException(String message) {
        super(message);
    }

    public ApiForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }

}
