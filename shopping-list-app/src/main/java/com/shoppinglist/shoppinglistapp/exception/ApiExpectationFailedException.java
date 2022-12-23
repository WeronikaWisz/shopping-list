package com.shoppinglist.shoppinglistapp.exception;

public class ApiExpectationFailedException extends RuntimeException {

    public ApiExpectationFailedException(String message) {
        super(message);
    }

    public ApiExpectationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
