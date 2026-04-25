package com.critmon.pulsecheck.model;

import java.util.concurrent.ScheduledFuture;

public class Monitor {

    private final String id;
    private final int timeout;
    private final String alertEmail;
    private volatile MonitorStatus status;
    private volatile ScheduledFuture<?> scheduledFuture;

    public Monitor(String id, int timeout, String alertEmail) {
        this.id = id;
        this.timeout = timeout;
        this.alertEmail = alertEmail;
        this.status = MonitorStatus.ACTIVE;
    }

    public String getId() { return id; }
    public int getTimeout() { return timeout; }
    public String getAlertEmail() { return alertEmail; }
    public MonitorStatus getStatus() { return status; }
    public ScheduledFuture<?> getScheduledFuture() { return scheduledFuture; }

    public void setStatus(MonitorStatus status) { this.status = status; }
    public void setScheduledFuture(ScheduledFuture<?> scheduledFuture) {
        this.scheduledFuture = scheduledFuture;
    }
}
