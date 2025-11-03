package server.filter;

import server.http.HttpRequest;
import server.http.HttpResponse;

// 요청을 가로채서 전처리/후처리하는 필터 기본 인터페이스
public interface Filter {
  HttpResponse doFilter(HttpRequest req, FilterChain chain) throws Exception;
}
