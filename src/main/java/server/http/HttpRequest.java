package server.http;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

// 파서가 읽어낸 HTTP 요청 내용을 담는 간단한 DTO 느낌의 클래스
public final class HttpRequest {
    private final String method;
    private final String target;
    private final String version;
    private final Map<String, String> headers;
    private final byte[] body;

    public HttpRequest(String method,
                       String target,
                       String version,
                       Map<String, String> headers,
                       byte[] body) {
        this.method = method;
        this.target = target;
        this.version = version;
        this.headers = Collections.unmodifiableMap(new LinkedHashMap<>(headers));
        this.body = body == null ? new byte[0] : body.clone();
    }

    public String method() {
        return method;
    }

    public String target() {
        return target;
    }

    // 쿼리스트링을 떼고 순수 경로만 돌려준다.
    public String path() {
        int idx = target.indexOf('?');
        if (idx == -1) {
            return target.isEmpty() ? "/" : target;
        }
        String path = target.substring(0, idx);
        return path.isEmpty() ? "/" : path;
    }

    public String version() {
        return version;
    }

    public Map<String, String> headers() {
        return headers;
    }

    // 헤더 키는 전부 소문자로 정리해두었으니 소문자로 조회해야 한다.
    public String header(String name) {
        return headers.get(name.toLowerCase());
    }

    public byte[] body() {
        // 외부에서 바꾸지 못하게 복사본을 넘겨준다.
        return body.clone();
    }
}
