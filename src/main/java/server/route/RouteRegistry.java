package server.route;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 애플리케이션 전용 라우트를 관리하는 레지스트리.
 * HTTP 메소드와 경로를 기준으로 핸들러를 검색할 수 있다.
 */
public final class RouteRegistry {
    private final Map<String, Map<String, Handler>> routingTable = new LinkedHashMap<>();

    public void register(String method, String path, Handler handler) {
        String normalizedMethod = method.toUpperCase();
        String normalizedPath = normalizePath(path);
        routingTable
                .computeIfAbsent(normalizedMethod, key -> new LinkedHashMap<>())
                .put(normalizedPath, handler);
    }

    public Optional<Handler> find(String method, String path) {
        Map<String, Handler> handlers = routingTable.get(method.toUpperCase());
        if (handlers == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(handlers.get(normalizePath(path)));
    }

    public Set<String> allowedMethods(String path) {
        String normalizedPath = normalizePath(path);
        Set<String> methods = new LinkedHashSet<>();
        routingTable.forEach((method, handlerMap) -> {
            if (handlerMap.containsKey(normalizedPath)) {
                methods.add(method);
            }
        });
        return Collections.unmodifiableSet(methods);
    }

    private String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }
        String normalized = path.startsWith("/") ? path : "/" + path;
        if (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
