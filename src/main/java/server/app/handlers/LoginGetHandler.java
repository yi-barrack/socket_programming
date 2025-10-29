package server.app.handlers;

import server.http.HttpRequest;
import server.http.HttpResponse;
import server.route.Handler;

import java.nio.charset.StandardCharsets;

/**
 * 로그인 폼을 렌더링하는 GET 핸들러.
 * 실제 UI는 여기서 자유롭게 수정 가능하다.
 */
public final class LoginGetHandler implements Handler {
    @Override
    public HttpResponse handle(HttpRequest request) {
        String html = """
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                  <meta charset="UTF-8">
                  <title>로그인</title>
                </head>
                <body>
                  <h1>로그인</h1>
                  <form method="post" action="/login">
                    <label>
                      아이디:
                      <input type="text" name="username" autocomplete="username">
                    </label>
                    <br>
                    <label>
                      비밀번호:
                      <input type="password" name="password" autocomplete="current-password">
                    </label>
                    <br>
                    <button type="submit">로그인</button>
                  </form>
                </body>
                </html>
                """;
        return HttpResponse.builder(200, "OK")
                .header("Content-Type", "text/html; charset=UTF-8")
                .body(html.getBytes(StandardCharsets.UTF_8))
                .build();
    }
}
