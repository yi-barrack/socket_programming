package server.route;

import server.http.HttpRequest;
import server.http.HttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 요청 메소드/경로에 따라 적절한 핸들러로 분기해 주는 라우터.
 * GET/HEAD는 정적 파일 핸들러로 처리하고, POST는 경로별 등록 핸들러를 우선 적용한다.
 */
public final class Router {
    private final Handler staticFileHandler;
    private final Handler defaultPostHandler;
    private final Map<String, Handler> postHandlers = new LinkedHashMap<>(); // 경로별 POST 핸들러 맵
    // string에 경로가 오고, handler가 그에 따라 매핑된다.
    // ex) map<"/login", LoginHandler>

    public Router(Handler staticFileHandler) {
        this(staticFileHandler, null);
    }

    public Router(Handler staticFileHandler, Handler defaultPostHandler) {
        this.staticFileHandler = staticFileHandler;
        this.defaultPostHandler = defaultPostHandler;
    }

    /**
     * POST 전용 라우트를 등록한다. 동일한 경로가 이미 있을 경우 덮어쓴다.
     */
    public void registerPost(String path, Handler handler) {
        postHandlers.put(normalizePath(path), handler);
    }

    /**
     * 테스트나 디버깅을 위해 등록된 POST 라우트를 읽기 전용으로 확인한다.
     */
    public Map<String, Handler> postRoutes() {
        return Collections.unmodifiableMap(postHandlers);
    }

    public HttpResponse route(HttpRequest request) throws IOException {
        String method = request.method();
        if ("GET".equals(method) || "HEAD".equals(method)) {
            return staticFileHandler.handle(request);
        }
        if ("POST".equals(method)) {
            String path = normalizePath(extractPath(request.target())); // request.target()에서 경로 부분만 추출
            Handler handler = postHandlers.get(path); // 경로에 등록된 핸들러 조회
            if (handler != null) {
                return handler.handle(request);
            }
            if (defaultPostHandler != null) {
                return defaultPostHandler.handle(request);
            }
            return notAllowed(Set.of("GET", "HEAD"));
        }
        Set<String> allow = defaultPostHandler != null || !postHandlers.isEmpty()
                ? Set.of("GET", "HEAD", "POST")
                : Set.of("GET", "HEAD");
        return notAllowed(allow);
    }

    private String extractPath(String target) {
        if (target == null || target.isEmpty()) {
            return "/";
        }
        int queryIndex = target.indexOf('?');
        String path = queryIndex >= 0 ? target.substring(0, queryIndex) : target;
        return path.isEmpty() ? "/" : path;
    }

    private String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }
        String value = path.startsWith("/") ? path : "/" + path;
        if (value.length() > 1 && value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }

    private HttpResponse notAllowed(Set<String> allow) {
        return HttpResponse.builder(405, "Method Not Allowed")
                .header("Allow", String.join(", ", allow))
                .header("Content-Type", "text/plain; charset=UTF-8")
                .body("Method Not Allowed".getBytes(StandardCharsets.UTF_8))
                .build();
    }
}
