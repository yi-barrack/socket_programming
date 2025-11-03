package server.route;

import server.http.HttpRequest;
import server.http.HttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

// HTTP 메서드에 맞춰 정적 파일 핸들러나 POST 핸들러로 넘겨주는 단순 라우터
public final class Router {
    private final Handler staticFileHandler;
    private final Handler postHandler;

    public Router(Handler staticFileHandler) {
        this(staticFileHandler, null);
    }

    public Router(Handler staticFileHandler, Handler postHandler) {
        this.staticFileHandler = staticFileHandler;
        this.postHandler = postHandler;
    }

    public HttpResponse route(HttpRequest request) throws IOException {
        String method = request.method();
        if ("GET".equals(method) || "HEAD".equals(method)) {
            return staticFileHandler.handle(request);
        }
        if ("POST".equals(method)) {
            if (postHandler != null) {
                return postHandler.handle(request);
            }
            return notAllowed("GET, HEAD");
        }
        return notAllowed(postHandler != null ? "GET, HEAD, POST" : "GET, HEAD");
    }

    private HttpResponse notAllowed(String allowHeader) {
        // 허용 목록을 알려주면서 405 응답을 돌려준다.
        return HttpResponse.builder(405, "Method Not Allowed")
                .header("Allow", allowHeader)
                .header("Content-Type", "text/plain; charset=UTF-8")
                .body("Method Not Allowed".getBytes(StandardCharsets.UTF_8))
                .build();
    }
}
