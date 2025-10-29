package server.route;

import server.http.HttpRequest;
import server.http.HttpResponse;

import java.io.IOException;

/**
 * 요청 메소드/경로에 따라 적절한 핸들러로 분기해 주는 단순 라우터.
 */
public final class Router {
    private final Handler staticFileHandler;

    public Router(Handler staticFileHandler) {
        this.staticFileHandler = staticFileHandler;
    }

    public HttpResponse route(HttpRequest request) throws IOException {
        String method = request.method();
        if ("GET".equals(method) || "HEAD".equals(method)) {
            return staticFileHandler.handle(request);
        }
        if ("POST".equals(method)) {
            // 아직 POST 처리를 구현하지 않았으므로 501을 반환한다.
            return HttpResponse.builder(501, "Not Implemented")
                    .header("Content-Type", "text/plain; charset=UTF-8")
                    .body("POST not supported yet".getBytes(java.nio.charset.StandardCharsets.UTF_8))
                    .build();
        }
        // 허용되지 않은 메소드를 호출하면 405와 Allow 헤더를 돌려준다.
        return HttpResponse.builder(405, "Method Not Allowed")
                .header("Allow", "GET, HEAD")
                .header("Content-Type", "text/plain; charset=UTF-8")
                .body("Method Not Allowed".getBytes(java.nio.charset.StandardCharsets.UTF_8))
                .build();
    }
}
