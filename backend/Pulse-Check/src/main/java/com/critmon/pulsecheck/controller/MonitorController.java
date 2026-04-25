package com.critmon.pulsecheck.controller;

import com.critmon.pulsecheck.dto.CreateMonitorRequest;
import com.critmon.pulsecheck.dto.MonitorStatusResponse;
import com.critmon.pulsecheck.service.MonitorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/monitors")
public class MonitorController {

    private final MonitorService monitorService;

    public MonitorController(MonitorService monitorService) {
        this.monitorService = monitorService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createMonitor(
            @RequestBody CreateMonitorRequest request) {
        monitorService.register(request.id(), request.timeout(), request.alertEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "message", "Monitor " + request.id() + " registered successfully",
            "id", request.id()
        ));
    }

    @PostMapping("/{id}/heartbeat")
    public ResponseEntity<Map<String, String>> heartbeat(@PathVariable String id) {
        monitorService.heartbeat(id);
        return ResponseEntity.ok(Map.of("message", "Heartbeat received for " + id));
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<Map<String, String>> pause(@PathVariable String id) {
        monitorService.pause(id);
        return ResponseEntity.ok(Map.of("message", "Monitor " + id + " paused"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MonitorStatusResponse> getStatus(@PathVariable String id) {
        return ResponseEntity.ok(monitorService.getStatus(id));
    }
}
