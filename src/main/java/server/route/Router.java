package server.route;

import server.http.HttpRequest;
import server.http.HttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 요청 메소드/경로에 따라 적절한 핸들러로 분기해 주는 라우터.
 * 1) 애플리케이션 전용 라우트(RouteRegistry) → 2) 정적 파일 → 3) 기본 POST 핸들러 순으로 처리한다.
 */
public final class Router {
    private final Handler staticFileHandler;
    private final Handler defaultPostHandler;
    private final RouteRegistry routeRegistry;

    public Router(Handler staticFileHandler, Handler defaultPostHandler, RouteRegistry routeRegistry) {
        this.staticFileHandler = staticFileHandler;
        this.defaultPostHandler = defaultPostHandler;
        this.routeRegistry = routeRegistry;
    }

    public HttpResponse route(HttpRequest request) throws IOException {
        String method = request.method().toUpperCase();
        String path = extractPath(request.target());

        // 1) 애플리케이션 라우트가 존재하면 우선 처리
        if (routeRegistry != null) {
            Handler appHandler = routeRegistry.find(method, path).orElse(null);
            if (appHandler == null && "HEAD".equals(method)) {
                // HEAD는 GET 핸들러를 재사용할 수 있도록 폴백
                appHandler = routeRegistry.find("GET", path).orElse(null);
            }
            if (appHandler != null) {
                return appHandler.handle(request);
            }
        }

        // 2) 정적 파일 핸들링 (GET/HEAD)
        if ("GET".equals(method) || "HEAD".equals(method)) {
            return staticFileHandler.handle(request);
        }

        // 3) 기본 POST 핸들러 (라우트에 없을 때)
        if ("POST".equals(method) && defaultPostHandler != null) {
            return defaultPostHandler.handle(request);
        }

        return notAllowed(path);
    }

    private String extractPath(String target) {
        if (target == null || target.isEmpty()) {
            return "/";
        }
        int queryIndex = target.indexOf('?');
        String path = queryIndex >= 0 ? target.substring(0, queryIndex) : target;
        return path.isEmpty() ? "/" : path;
    }

    private HttpResponse notAllowed(String path) {
        String allowHeader = buildAllowHeader(path);
        return HttpResponse.builder(405, "Method Not Allowed")
                .header("Allow", allowHeader)
                .header("Content-Type", "text/plain; charset=UTF-8")
                .body("Method Not Allowed".getBytes(StandardCharsets.UTF_8))
                .build();
    }

    private String buildAllowHeader(String path) {
        Set<String> methods = new LinkedHashSet<>();
        // 정적 서비스는 GET/HEAD를 항상 지원
        methods.add("GET");
        methods.add("HEAD");

        if (routeRegistry != null) {
            methods.addAll(routeRegistry.allowedMethods(path));
        }

        if (defaultPostHandler != null) {
            methods.add("POST");
        }
        return String.join(", ", methods);
    }
}
