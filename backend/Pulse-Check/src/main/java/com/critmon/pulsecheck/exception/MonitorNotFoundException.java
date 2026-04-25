package com.critmon.pulsecheck.exception;

public class MonitorNotFoundException extends RuntimeException {
    public MonitorNotFoundException(String id) {
        super("Monitor not found: " + id);
    }
}
