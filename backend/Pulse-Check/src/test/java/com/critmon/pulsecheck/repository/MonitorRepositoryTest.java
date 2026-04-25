package com.critmon.pulsecheck.repository;

import com.critmon.pulsecheck.model.Monitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class MonitorRepositoryTest {

    private MonitorRepository repository;

    @BeforeEach
    void setUp() {
        repository = new MonitorRepository();
    }

    @Test
    void save_and_findById_returnsMonitor() {
        Monitor monitor = new Monitor("device-1", 60, "admin@test.com");
        repository.save(monitor);

        Optional<Monitor> found = repository.findById("device-1");

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo("device-1");
        assertThat(found.get().getAlertEmail()).isEqualTo("admin@test.com");
    }

    @Test
    void findById_whenNotFound_returnsEmpty() {
        assertThat(repository.findById("nonexistent")).isEmpty();
    }

    @Test
    void existsById_returnsTrueWhenPresent() {
        repository.save(new Monitor("device-2", 30, "test@test.com"));
        assertThat(repository.existsById("device-2")).isTrue();
    }

    @Test
    void existsById_returnsFalseWhenAbsent() {
        assertThat(repository.existsById("ghost")).isFalse();
    }

    @Test
    void deleteById_removesMonitor() {
        repository.save(new Monitor("device-3", 30, "test@test.com"));
        repository.deleteById("device-3");
        assertThat(repository.existsById("device-3")).isFalse();
    }

    @Test
    void save_overwritesExistingEntry() {
        repository.save(new Monitor("device-4", 30, "first@test.com"));
        repository.save(new Monitor("device-4", 60, "second@test.com"));

        Optional<Monitor> found = repository.findById("device-4");
        assertThat(found).isPresent();
        assertThat(found.get().getAlertEmail()).isEqualTo("second@test.com");
    }
}
