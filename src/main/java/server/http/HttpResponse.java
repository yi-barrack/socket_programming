package server.http;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

// 서버에서 만든 HTTP 응답을 담아두는 간단한 객체. 한 번 만들면 바뀌지 않는다.
public final class HttpResponse {
    private final int statusCode;
    private final String reasonPhrase;
    private final Map<String, String> headers;
    private final byte[] body;

    private HttpResponse(int statusCode,
                         String reasonPhrase,
                         Map<String, String> headers,
                         byte[] body) {
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
        this.headers = Collections.unmodifiableMap(new LinkedHashMap<>(headers));
        this.body = body == null ? new byte[0] : body.clone();
    }

    public int statusCode() {
        return statusCode;
    }

    public String reasonPhrase() {
        return reasonPhrase;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public byte[] body() {
        // 호출한 쪽에서 배열 바꿔도 원본이 안 깨지게 복사본을 준다.
        return body.clone();
    }

    public static Builder builder(int statusCode, String reasonPhrase) {
        return new Builder(statusCode, reasonPhrase);
    }

    public static final class Builder {
        private final int statusCode;
        private final String reasonPhrase;
        private final Map<String, String> headers = new LinkedHashMap<>();
        private byte[] body = new byte[0];

        private Builder(int statusCode, String reasonPhrase) {
            this.statusCode = statusCode;
            this.reasonPhrase = reasonPhrase;
        }

        // 응답 헤더 추가. 같은 이름이 오면 그냥 덮어씌운다.
        public Builder header(String name, String value) {
            headers.put(name, value);
            return this;
        }

        // 응답 본문 세팅. null 들어오면 빈 배열로 바꿔둔다.
        public Builder body(byte[] body) {
            this.body = body == null ? new byte[0] : body.clone();
            return this;
        }

        public HttpResponse build() {
            return new HttpResponse(statusCode, reasonPhrase, new LinkedHashMap<>(headers), body);
        }
    }
}
