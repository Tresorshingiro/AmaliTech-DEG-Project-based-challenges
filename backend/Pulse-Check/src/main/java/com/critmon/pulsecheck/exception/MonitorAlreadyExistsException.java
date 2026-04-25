package com.critmon.pulsecheck.exception;

public class MonitorAlreadyExistsException extends RuntimeException {
    public MonitorAlreadyExistsException(String id) {
        super("Monitor already exists: " + id);
    }
}
