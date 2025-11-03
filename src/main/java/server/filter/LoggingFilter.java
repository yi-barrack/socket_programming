package server.filter;

import server.http.HttpRequest;
import server.http.HttpResponse;

// 요청이 들어올 때마다 간단한 로그만 찍고 다음 필터로 넘긴다.
public final class LoggingFilter implements Filter {
  @Override public HttpResponse doFilter(HttpRequest req, FilterChain chain) throws Exception {
    long t0 = System.nanoTime();
    HttpResponse res = chain.doFilter(req);
    long ms = (System.nanoTime() - t0) / 1_000_000;
    System.out.printf("%s %s -> %d (%d ms)%n", req.method(), req.path(), res.statusCode(), ms);
    return res;
  }
}


// 실패 성공 상관없이 로그만 남기고 끝나는 단순 필터다.
