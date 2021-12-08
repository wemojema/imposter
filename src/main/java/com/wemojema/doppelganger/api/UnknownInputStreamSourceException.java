package com.wemojema.doppelganger.api;

public class UnknownInputStreamSourceException extends RuntimeException {

    public UnknownInputStreamSourceException(String message) {
        super(message);
    }

    public UnknownInputStreamSourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownInputStreamSourceException(Throwable cause) {
        super(cause);
    }

    public UnknownInputStreamSourceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
