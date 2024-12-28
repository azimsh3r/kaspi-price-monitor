package com.metaorta.kaspi.exception;

public class SessionExpiredException extends Exception {

    public SessionExpiredException(String sessionExpired) {
        super(sessionExpired);
    }
}
