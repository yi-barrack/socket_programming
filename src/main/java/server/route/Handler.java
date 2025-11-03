package server.route;

import server.http.HttpRequest;
import server.http.HttpResponse;

import java.io.IOException;

// 라우터가 고른 뒤 실제 일을 처리하는 핸들러 인터페이스
public interface Handler {
    HttpResponse handle(HttpRequest request) throws IOException;
}
