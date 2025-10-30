package server.route;

import server.http.HttpRequest;
import server.http.HttpResponse;
import server.util.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 요청 메소드와 경로에 따라 적절한 핸들러로 분기해 주는 단순 라우터입니다.
 * POST 요청 시, /api/posts 경로에 대해서는 postCreationHandler로 분기합니다.
 */
public final class Router {
    private final Handler staticFileHandler;
    private final Handler postCreationHandler;
    private final Handler postDeleteHandler; // DELETE 요청을 위한 새 핸들러
    private final Handler postHandler;

    public Router(Handler staticFileHandler) {
        this(staticFileHandler, null, null, null);
    }

    public Router(Handler staticFileHandler, Handler postCreationHandler, Handler postDeleteHandler, Handler postHandler) {
        this.staticFileHandler = staticFileHandler;
        this.postCreationHandler = postCreationHandler;
        this.postDeleteHandler = postDeleteHandler; // 필드 초기화
        this.postHandler = postHandler;
    }

    public HttpResponse route(HttpRequest request) throws IOException {
        String method = request.method();
        String uri = request.target();

        // GET 및 HEAD 요청 처리
        if ("GET".equals(method) || "HEAD".equals(method)) {
            return staticFileHandler.handle(request);
        }

        // POST 요청 처리
        if ("POST".equals(method)) {
            // 1. /api/posts 요청 처리 (글 생성)
            if ("/api/posts".equals(uri) && postCreationHandler != null) {
                Logger.info("Routing POST request to PostCreationHandler: " + uri);
                return postCreationHandler.handle(request);
            }

            // 2. 그 외의 POST 요청 처리
            if (postHandler != null) {
                Logger.info("Routing POST request to General Post Handler: " + uri);
                return postHandler.handle(request);
            }

            // POST 핸들러가 없으면 405 응답
            return notAllowed("GET, HEAD");
        }

        // DELETE 요청 처리
        if ("DELETE".equals(method)) {
            // /api/posts/{id} 경로 패턴 확인 및 postDeleteHandler 호출
            if (uri.startsWith("/api/posts/") && postDeleteHandler != null) {
                Logger.info("Routing DELETE request to PostDeleteHandler: " + uri);
                return postDeleteHandler.handle(request);
            }
            // DELETE 핸들러가 없거나 경로가 다르면 405 응답
            return notAllowed(getAllowHeader());
        }

        // 지원하지 않는 메소드 처리
        return notAllowed(getAllowHeader());
    }

    /**
     * 지원되는 HTTP 메소드를 Allow 헤더 문자열로 반환합니다.
     */
    private String getAllowHeader() {
        StringBuilder allow = new StringBuilder("GET, HEAD");
        if (postCreationHandler != null || postHandler != null) {
            allow.append(", POST");
        }
        if (postDeleteHandler != null) {
            allow.append(", DELETE");
        }
        return allow.toString();
    }


    private HttpResponse notAllowed(String allowHeader) {
        return HttpResponse.builder(405, "Method Not Allowed")
                .header("Allow", allowHeader)
                .header("Content-Type", "text/plain; charset=UTF-8")
                .body("Method Not Allowed".getBytes(StandardCharsets.UTF_8))
                .build();
    }
}