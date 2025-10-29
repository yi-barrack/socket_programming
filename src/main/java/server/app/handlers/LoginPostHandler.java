package server.app.handlers;

import server.http.HttpRequest;
import server.http.HttpResponse;
import server.route.Handler;
import server.util.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 로그인 제출을 처리하는 POST 핸들러 예시.
 * 실제 인증 로직은 TODO 구간을 채우면서 구현하면 된다.
 */
public final class LoginPostHandler implements Handler {
    @Override
    public HttpResponse handle(HttpRequest request) {
        Charset charset = detectCharset(request.header("content-type"));
        String body = new String(request.body(), charset);
        Map<String, String> params = parseFormUrlEncoded(body, charset);

        // TODO: 여기서 DB 조회 혹은 세션 발급 등의 실제 인증 로직을 구현하세요.
        Logger.info("로그인 시도 username=" + params.getOrDefault("username", "(없음)"));

        boolean authenticated = false; // 예시: 실제 인증 결과로 교체

        String responseText;
        int status;
        if (authenticated) {
            status = 200;
            responseText = "로그인 성공 (샘플) - 여기에 리다이렉트 또는 세션 처리 로직을 추가하세요.";
        } else {
            status = 401;
            responseText = "로그인 실패 (샘플) - 인증 로직을 구현하고 적절한 응답을 내려주세요.";
        }

        return HttpResponse.builder(status, status == 200 ? "OK" : "Unauthorized")
                .header("Content-Type", "text/plain; charset=UTF-8")
                .body(responseText.getBytes(StandardCharsets.UTF_8))
                .build();
    }

    private Charset detectCharset(String contentType) {
        if (contentType == null) {
            return StandardCharsets.UTF_8;
        }
        String lower = contentType.toLowerCase();
        int idx = lower.indexOf("charset=");
        if (idx == -1) {
            return StandardCharsets.UTF_8;
        }
        String charsetName = lower.substring(idx + 8).trim();
        int semi = charsetName.indexOf(';');
        if (semi != -1) {
            charsetName = charsetName.substring(0, semi).trim();
        }
        try {
            return Charset.forName(charsetName);
        } catch (Exception e) {
            return StandardCharsets.UTF_8;
        }
    }

    private Map<String, String> parseFormUrlEncoded(String body, Charset charset) {
        if (body.isEmpty()) {
            return Map.of();
        }
        Map<String, String> params = new LinkedHashMap<>();
        for (String pair : body.split("&")) {
            String[] tokens = pair.split("=", 2);
            String key = decode(tokens[0], charset);
            String value = tokens.length > 1 ? decode(tokens[1], charset) : "";
            params.put(key, value);
        }
        return params;
    }

    private String decode(String encoded, Charset charset) {
        try {
            return URLDecoder.decode(encoded, charset.name());
        } catch (UnsupportedEncodingException e) {
            return encoded;
        }
    }
}
