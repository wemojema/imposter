package com.wemojema.doppelganger.api;

public class MissingHandlerException extends RuntimeException {
    public MissingHandlerException(String message) {
        super(message);
    }
}
