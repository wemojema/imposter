package com.wemojema.imposter.api;

public class MissingHandlerException extends RuntimeException {
    public MissingHandlerException(String message) {
        super(message);
    }
}
