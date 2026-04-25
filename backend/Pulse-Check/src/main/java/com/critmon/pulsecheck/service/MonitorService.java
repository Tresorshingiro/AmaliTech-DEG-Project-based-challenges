package com.critmon.pulsecheck.service;

import com.critmon.pulsecheck.dto.MonitorStatusResponse;
import com.critmon.pulsecheck.exception.MonitorAlreadyExistsException;
import com.critmon.pulsecheck.exception.MonitorNotFoundException;
import com.critmon.pulsecheck.model.Monitor;
import com.critmon.pulsecheck.model.MonitorStatus;
import com.critmon.pulsecheck.repository.MonitorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class MonitorService {

    private static final Logger log = LoggerFactory.getLogger(MonitorService.class);

    private final MonitorRepository repository;
    private final ScheduledExecutorService scheduler;

    public MonitorService(MonitorRepository repository, ScheduledExecutorService scheduler) {
        this.repository = repository;
        this.scheduler = scheduler;
    }

    public Monitor register(String id, int timeout, String alertEmail) {
        if (repository.existsById(id)) {
            throw new MonitorAlreadyExistsException(id);
        }
        Monitor monitor = new Monitor(id, timeout, alertEmail);
        repository.save(monitor);
        scheduleAlert(monitor);
        return monitor;
    }

    public Monitor heartbeat(String id) {
        Monitor monitor = repository.findById(id)
            .orElseThrow(() -> new MonitorNotFoundException(id));
        cancelTimer(monitor);
        monitor.setStatus(MonitorStatus.ACTIVE);
        scheduleAlert(monitor);
        return monitor;
    }

    public Monitor pause(String id) {
        Monitor monitor = repository.findById(id)
            .orElseThrow(() -> new MonitorNotFoundException(id));
        cancelTimer(monitor);
        if (monitor.getStatus() != MonitorStatus.DOWN) {
            monitor.setStatus(MonitorStatus.PAUSED);
        }
        return monitor;
    }

    public MonitorStatusResponse getStatus(String id) {
        Monitor monitor = repository.findById(id)
            .orElseThrow(() -> new MonitorNotFoundException(id));

        long remaining = 0;
        if (monitor.getStatus() == MonitorStatus.ACTIVE
                && monitor.getScheduledFuture() != null) {
            remaining = Math.max(0, monitor.getScheduledFuture().getDelay(TimeUnit.SECONDS));
        }

        return new MonitorStatusResponse(
            monitor.getId(),
            monitor.getStatus().name().toLowerCase(),
            remaining,
            monitor.getAlertEmail()
        );
    }

    private void scheduleAlert(Monitor monitor) {
        ScheduledFuture<?> future = scheduler.schedule(
            () -> fireAlert(monitor),
            monitor.getTimeout(),
            TimeUnit.SECONDS
        );
        monitor.setScheduledFuture(future);
    }

    private void cancelTimer(Monitor monitor) {
        ScheduledFuture<?> future = monitor.getScheduledFuture();
        if (future != null && !future.isDone()) {
            future.cancel(false);
        }
    }

    private void fireAlert(Monitor monitor) {
        monitor.setStatus(MonitorStatus.DOWN);
        log.error("{{\"ALERT\": \"Device {} is down!\", \"time\": \"{}\"}}",
            monitor.getId(), Instant.now());
    }
}
