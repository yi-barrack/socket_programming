package server.route;

import server.http.HttpRequest;
import server.http.HttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

// POST 요청을 경로별로 나눠서 다른 핸들러에 전달하는 래퍼
public final class RoutedPostHandler implements Handler {
    private final Map<String, Handler> routes = new HashMap<>();
    private final Handler fallback;

    public RoutedPostHandler(Handler fallback) {
        this.fallback = fallback;
    }

    // 원하는 경로에 POST 핸들러를 붙일 수 있게 해준다.
    public void register(String path, Handler handler) {
        if (path == null || handler == null) {
            return;
        }
        routes.put(normalize(path), handler);
    }

    @Override
    public HttpResponse handle(HttpRequest request) throws IOException {
        Handler handler = routes.get(normalize(request.target()));
        if (handler != null) {
            return handler.handle(request);
        }
        if (fallback != null) {
            return fallback.handle(request);
        }
        return notFound();
    }

    private String normalize(String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }
        String normalized = path;
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        // 뒤에 쿼리 스트링이 붙어 있다면 잘라낸다.
        int queryIndex = normalized.indexOf('?');
        if (queryIndex >= 0) {
            normalized = normalized.substring(0, queryIndex);
        }
        return normalized;
    }

    private HttpResponse notFound() {
        return HttpResponse.builder(404, "Not Found")
                .header("Content-Type", "text/plain; charset=UTF-8")
                .body("Not Found".getBytes(StandardCharsets.UTF_8))
                .build();
    }
}
