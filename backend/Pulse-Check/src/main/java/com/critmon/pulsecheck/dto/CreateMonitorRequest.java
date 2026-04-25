package com.critmon.pulsecheck.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreateMonitorRequest(
    String id,
    int timeout,
    @JsonProperty("alert_email") String alertEmail
) {}
