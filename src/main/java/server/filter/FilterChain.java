package server.filter;

import server.http.HttpRequest;
import server.http.HttpResponse;
import server.route.Router;

import java.util.List;

// 등록된 필터들을 순서대로 실행하고 마지막에 라우터까지 넘겨주는 체인
public final class FilterChain {
  private final List<Filter> filters;
  private final Router router;
  private int index = 0;

  public FilterChain(List<Filter> filters, Router router) {
    this.filters = filters;
    this.router = router;
  }

  public HttpResponse doFilter(HttpRequest req) throws Exception {
    if (index < filters.size()) {
      // 아직 남은 필터가 있으면 다음 필터 실행
      return filters.get(index++).doFilter(req, this);
    }
    // 필터가 끝까지 돌았으면 최종 라우터 호출
    return router.route(req);
  }
}
