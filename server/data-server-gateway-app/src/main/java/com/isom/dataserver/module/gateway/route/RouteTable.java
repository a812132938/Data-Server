package com.isom.dataserver.module.gateway.route;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class RouteTable {

    private volatile Map<String, ApiRouteEntry> routes = Collections.emptyMap();
    private volatile RouteTableStatus status = RouteTableStatus.initial();

    public void put(ApiRouteEntry entry) {
        Map<String, ApiRouteEntry> next = new LinkedHashMap<>(routes);
        next.put(buildKey(entry.getPath(), entry.getMethod()), entry);
        routes = Collections.unmodifiableMap(next);
    }

    public void remove(String path, String method) {
        Map<String, ApiRouteEntry> next = new LinkedHashMap<>(routes);
        next.remove(buildKey(path, method));
        routes = Collections.unmodifiableMap(next);
    }

    public void removeByApiId(Long apiId) {
        Map<String, ApiRouteEntry> next = new LinkedHashMap<>(routes);
        next.entrySet().removeIf(e -> e.getValue().getApiId().equals(apiId));
        routes = Collections.unmodifiableMap(next);
    }

    public ApiRouteEntry match(String path, String method) {
        return routes.get(buildKey(path, method));
    }

    public Collection<ApiRouteEntry> all() {
        return routes.values();
    }

    public void clear() {
        routes = Collections.emptyMap();
    }

    public void replaceAll(Collection<ApiRouteEntry> entries) {
        Map<String, ApiRouteEntry> next = new LinkedHashMap<>();
        for (ApiRouteEntry entry : entries) {
            next.put(buildKey(entry.getPath(), entry.getMethod()), entry);
        }
        routes = Collections.unmodifiableMap(next);
        status = RouteTableStatus.success(next.size());
    }

    public void markReloadFailed(String errorMessage) {
        status = RouteTableStatus.failed(routes.size(), errorMessage);
    }

    public RouteTableStatus status() {
        return status;
    }

    private String buildKey(String path, String method) {
        return method.toUpperCase() + ":" + path;
    }

    @Data
    public static class RouteTableStatus {
        private int routeCount;
        private LocalDateTime lastReloadAt;
        private boolean lastReloadSuccess;
        private String lastReloadError;

        private static RouteTableStatus initial() {
            RouteTableStatus status = new RouteTableStatus();
            status.setRouteCount(0);
            status.setLastReloadSuccess(false);
            status.setLastReloadError("Route table has not loaded yet");
            return status;
        }

        private static RouteTableStatus success(int routeCount) {
            RouteTableStatus status = new RouteTableStatus();
            status.setRouteCount(routeCount);
            status.setLastReloadAt(LocalDateTime.now());
            status.setLastReloadSuccess(true);
            status.setLastReloadError(null);
            return status;
        }

        private static RouteTableStatus failed(int routeCount, String errorMessage) {
            RouteTableStatus status = new RouteTableStatus();
            status.setRouteCount(routeCount);
            status.setLastReloadAt(LocalDateTime.now());
            status.setLastReloadSuccess(false);
            status.setLastReloadError(errorMessage);
            return status;
        }
    }
}
