package com.critmon.pulsecheck.dto;

public record MonitorStatusResponse(
    String id,
    String status,
    long remainingSeconds,
    String alertEmail
) {}
