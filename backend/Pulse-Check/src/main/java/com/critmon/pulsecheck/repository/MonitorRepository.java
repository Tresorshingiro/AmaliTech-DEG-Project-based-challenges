package com.critmon.pulsecheck.repository;

import com.critmon.pulsecheck.model.Monitor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class MonitorRepository {

    private final ConcurrentHashMap<String, Monitor> store = new ConcurrentHashMap<>();

    public void save(Monitor monitor) {
        store.put(monitor.getId(), monitor);
    }

    public Optional<Monitor> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    public boolean existsById(String id) {
        return store.containsKey(id);
    }

    public void deleteById(String id) {
        store.remove(id);
    }
}
