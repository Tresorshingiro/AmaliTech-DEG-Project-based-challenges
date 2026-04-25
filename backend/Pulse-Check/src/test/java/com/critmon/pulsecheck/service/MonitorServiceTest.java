package com.critmon.pulsecheck.service;

import com.critmon.pulsecheck.dto.MonitorStatusResponse;
import com.critmon.pulsecheck.exception.MonitorAlreadyExistsException;
import com.critmon.pulsecheck.exception.MonitorNotFoundException;
import com.critmon.pulsecheck.model.Monitor;
import com.critmon.pulsecheck.model.MonitorStatus;
import com.critmon.pulsecheck.repository.MonitorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MonitorServiceTest {

    private MonitorRepository repository;
    private ScheduledExecutorService scheduler;
    @SuppressWarnings("rawtypes")
    private ScheduledFuture mockFuture;
    private MonitorService service;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        repository = new MonitorRepository();
        scheduler = mock(ScheduledExecutorService.class);
        mockFuture = mock(ScheduledFuture.class);

        when(scheduler.schedule(any(Runnable.class), anyLong(), any(TimeUnit.class)))
            .thenReturn(mockFuture);
        when(mockFuture.getDelay(TimeUnit.SECONDS)).thenReturn(30L);
        when(mockFuture.isDone()).thenReturn(false);
        when(mockFuture.cancel(false)).thenReturn(true);

        service = new MonitorService(repository, scheduler);
    }

    // --- register ---

    @Test
    void register_createsMonitorWithActiveStatus() {
        Monitor result = service.register("device-1", 60, "admin@test.com");

        assertThat(result.getId()).isEqualTo("device-1");
        assertThat(result.getStatus()).isEqualTo(MonitorStatus.ACTIVE);
    }

    @Test
    void register_schedulesAlertWithCorrectTimeout() {
        service.register("device-1", 60, "admin@test.com");

        verify(scheduler).schedule(any(Runnable.class), eq(60L), eq(TimeUnit.SECONDS));
    }

    @Test
    void register_throwsWhenDuplicateId() {
        service.register("device-1", 60, "admin@test.com");

        assertThatThrownBy(() -> service.register("device-1", 60, "admin@test.com"))
            .isInstanceOf(MonitorAlreadyExistsException.class)
            .hasMessageContaining("device-1");
    }

    // --- heartbeat ---

    @Test
    void heartbeat_resetsTimerAndKeepsActiveStatus() {
        service.register("device-1", 60, "admin@test.com");

        Monitor result = service.heartbeat("device-1");

        assertThat(result.getStatus()).isEqualTo(MonitorStatus.ACTIVE);
        verify(scheduler, times(2)).schedule(any(Runnable.class), eq(60L), eq(TimeUnit.SECONDS));
        verify(mockFuture).cancel(false);
    }

    @Test
    void heartbeat_throwsWhenMonitorNotFound() {
        assertThatThrownBy(() -> service.heartbeat("nonexistent"))
            .isInstanceOf(MonitorNotFoundException.class)
            .hasMessageContaining("nonexistent");
    }

    @Test
    void heartbeat_unpausesPausedMonitor() {
        service.register("device-1", 60, "admin@test.com");
        service.pause("device-1");

        Monitor result = service.heartbeat("device-1");

        assertThat(result.getStatus()).isEqualTo(MonitorStatus.ACTIVE);
    }

    // --- pause ---

    @Test
    void pause_cancelsTimerAndSetsPausedStatus() {
        service.register("device-1", 60, "admin@test.com");

        Monitor result = service.pause("device-1");

        assertThat(result.getStatus()).isEqualTo(MonitorStatus.PAUSED);
        verify(mockFuture).cancel(false);
    }

    @Test
    void pause_throwsWhenMonitorNotFound() {
        assertThatThrownBy(() -> service.pause("nonexistent"))
            .isInstanceOf(MonitorNotFoundException.class);
    }

    // --- getStatus ---

    @Test
    void getStatus_returnsActiveWithRemainingSeconds() {
        service.register("device-1", 60, "admin@test.com");

        MonitorStatusResponse response = service.getStatus("device-1");

        assertThat(response.id()).isEqualTo("device-1");
        assertThat(response.status()).isEqualTo("active");
        assertThat(response.remainingSeconds()).isEqualTo(30L);
        assertThat(response.alertEmail()).isEqualTo("admin@test.com");
    }

    @Test
    void getStatus_returnsZeroRemainingWhenPaused() {
        service.register("device-1", 60, "admin@test.com");
        service.pause("device-1");

        MonitorStatusResponse response = service.getStatus("device-1");

        assertThat(response.status()).isEqualTo("paused");
        assertThat(response.remainingSeconds()).isEqualTo(0L);
    }

    @Test
    void getStatus_throwsWhenMonitorNotFound() {
        assertThatThrownBy(() -> service.getStatus("nonexistent"))
            .isInstanceOf(MonitorNotFoundException.class);
    }
}
