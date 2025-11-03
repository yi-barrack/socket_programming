package server.filter;

import server.http.HttpRequest;
import server.http.HttpResponse;

import static server.http.ErrorResponses.*;

import java.util.Optional;

// 요청 본문 크기나 전송 방식 같은 걸 체크해서 너무 큰 요청은 막는다.
public final class BodyLimitFilter implements Filter {
  private final long maxBodyBytes;
  private final String home;

  public BodyLimitFilter(long maxBodyBytes, String home) {
    this.maxBodyBytes = maxBodyBytes;
    this.home = home;
  }

  @Override public HttpResponse doFilter(HttpRequest req, FilterChain chain) throws Exception {
    String te = Optional.ofNullable(req.header("transfer-encoding")).orElse("");
    
    if (!te.isEmpty() && te.contains("chunked")) {
      // 아직 chunked를 처리할 코드가 없어서 바로 막는다.
      return badRequestAlert(req, "chunked 인코딩은 지원하지 않습니다.", home);
    }
    long len = 0L;
    String contentLength = req.header("content-length");
    if (contentLength != null && !contentLength.isEmpty()) {
      try {
        len = Long.parseLong(contentLength);
      } catch (NumberFormatException e) {
        return badRequestAlert(req, "Content-Length 헤더가 잘못되었습니다.", home);
      }
    }
    if (len > maxBodyBytes) {
      return payloadTooLargeAlert(req, "요청 본문이 너무 큽니다.", home);
    }
    return chain.doFilter(req);
  }
}
