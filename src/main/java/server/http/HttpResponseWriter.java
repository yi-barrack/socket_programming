package server.http;

import server.config.ServerConfig;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

// HttpResponse 객체를 실제 소켓에 쓸 수 있는 HTTP 응답 텍스트로 바꿔준다.
public final class HttpResponseWriter {

    public void write(OutputStream out, HttpResponse response, boolean includeBody) throws IOException {
        Map<String, String> headers = new LinkedHashMap<>(response.headers());
        // Date, Server 헤더가 빠져 있으면 기본값으로 채워준다.
        headers.putIfAbsent("Date", DateTimeFormatter.RFC_1123_DATE_TIME
                .format(ZonedDateTime.now(java.time.ZoneOffset.UTC)));
        headers.putIfAbsent("Server", ServerConfig.SERVER_NAME);

        // HEAD 요청처럼 본문이 필요 없을 때는 빈 배열로 대체한다.
        byte[] body = includeBody ? response.body() : new byte[0];
        headers.put("Content-Length", Integer.toString(body.length));

        String statusLine = "HTTP/1.1 " + response.statusCode() + " " + response.reasonPhrase() + "\r\n";
        out.write(statusLine.getBytes(StandardCharsets.US_ASCII));
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String headerLine = entry.getKey() + ": " + entry.getValue() + "\r\n";
            out.write(headerLine.getBytes(StandardCharsets.US_ASCII));
        }
        out.write("\r\n".getBytes(StandardCharsets.US_ASCII));
        if (includeBody && body.length > 0) {
            out.write(body);
        }
        out.flush();
    }
}
