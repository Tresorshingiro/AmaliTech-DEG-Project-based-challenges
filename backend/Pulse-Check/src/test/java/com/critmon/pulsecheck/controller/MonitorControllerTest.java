package com.critmon.pulsecheck.controller;

import com.critmon.pulsecheck.dto.MonitorStatusResponse;
import com.critmon.pulsecheck.exception.MonitorAlreadyExistsException;
import com.critmon.pulsecheck.exception.MonitorNotFoundException;
import com.critmon.pulsecheck.model.Monitor;
import com.critmon.pulsecheck.service.MonitorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MonitorController.class)
class MonitorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MonitorService monitorService;

    @Test
    void createMonitor_returns201OnSuccess() throws Exception {
        Monitor monitor = new Monitor("device-1", 60, "admin@test.com");
        when(monitorService.register("device-1", 60, "admin@test.com"))
            .thenReturn(monitor);

        mockMvc.perform(post("/monitors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"device-1\",\"timeout\":60,\"alert_email\":\"admin@test.com\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.message").value("Monitor device-1 registered successfully"))
            .andExpect(jsonPath("$.id").value("device-1"));
    }

    @Test
    void createMonitor_returns409WhenDuplicateId() throws Exception {
        when(monitorService.register(anyString(), anyInt(), anyString()))
            .thenThrow(new MonitorAlreadyExistsException("device-1"));

        mockMvc.perform(post("/monitors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"device-1\",\"timeout\":60,\"alert_email\":\"admin@test.com\"}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("Monitor already exists: device-1"));
    }

    @Test
    void heartbeat_returns200OnSuccess() throws Exception {
        Monitor monitor = new Monitor("device-1", 60, "admin@test.com");
        when(monitorService.heartbeat("device-1")).thenReturn(monitor);

        mockMvc.perform(post("/monitors/device-1/heartbeat"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Heartbeat received for device-1"));
    }

    @Test
    void heartbeat_returns404WhenNotFound() throws Exception {
        when(monitorService.heartbeat("nonexistent"))
            .thenThrow(new MonitorNotFoundException("nonexistent"));

        mockMvc.perform(post("/monitors/nonexistent/heartbeat"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Monitor not found: nonexistent"));
    }

    @Test
    void pause_returns200OnSuccess() throws Exception {
        Monitor monitor = new Monitor("device-1", 60, "admin@test.com");
        when(monitorService.pause("device-1")).thenReturn(monitor);

        mockMvc.perform(post("/monitors/device-1/pause"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Monitor device-1 paused"));
    }

    @Test
    void pause_returns404WhenNotFound() throws Exception {
        when(monitorService.pause("nonexistent"))
            .thenThrow(new MonitorNotFoundException("nonexistent"));

        mockMvc.perform(post("/monitors/nonexistent/pause"))
            .andExpect(status().isNotFound());
    }

    @Test
    void getStatus_returns200WithFullMonitorInfo() throws Exception {
        MonitorStatusResponse response = new MonitorStatusResponse(
            "device-1", "active", 45L, "admin@test.com"
        );
        when(monitorService.getStatus("device-1")).thenReturn(response);

        mockMvc.perform(get("/monitors/device-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("device-1"))
            .andExpect(jsonPath("$.status").value("active"))
            .andExpect(jsonPath("$.remainingSeconds").value(45))
            .andExpect(jsonPath("$.alertEmail").value("admin@test.com"));
    }

    @Test
    void getStatus_returns404WhenNotFound() throws Exception {
        when(monitorService.getStatus("nonexistent"))
            .thenThrow(new MonitorNotFoundException("nonexistent"));

        mockMvc.perform(get("/monitors/nonexistent"))
            .andExpect(status().isNotFound());
    }
}
