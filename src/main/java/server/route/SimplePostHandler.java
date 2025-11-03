package server.route;

import server.http.HttpRequest;
import server.http.HttpResponse;
import server.util.Logger;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

// 기본 POST 핸들러. 받은 내용을 그대로 요약해서 돌려준다.
public final class SimplePostHandler implements Handler {

    @Override
    public HttpResponse handle(HttpRequest request) {
        byte[] body = request.body();
        String contentType = request.header("content-type");
        Charset charset = extractCharset(contentType);
        String bodyText = new String(body, charset);

        StringBuilder responseText = new StringBuilder();
        responseText.append("POST 요청을 처리했습니다.\n");
        responseText.append("수신 시각: ").append(Instant.now()).append('\n');
        responseText.append("Content-Type: ").append(contentType != null ? contentType : "없음").append('\n');
        responseText.append("본문 길이: ").append(body.length).append(" byte\n\n");
        if (body.length > 0) {
            responseText.append(bodyText);
        } else {
            responseText.append("(본문이 비어 있습니다)");
        }

        Logger.info("POST body size=" + body.length + " bytes");

        return HttpResponse.builder(201, "Created")
                .header("Content-Type", "text/plain; charset=UTF-8")
                .body(responseText.toString().getBytes(StandardCharsets.UTF_8))
                .build();
    }

    private Charset extractCharset(String contentType) {
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
            // 모르는 인코딩이면 그냥 UTF-8로 처리한다.
            return StandardCharsets.UTF_8;
        }
    }
}
