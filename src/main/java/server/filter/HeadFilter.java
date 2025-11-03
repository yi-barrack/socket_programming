package server.filter;

import server.http.HttpRequest;
import server.http.HttpResponse;

// HEAD 요청이면 본문만 비워서 돌려주는 필터
public final class HeadFilter implements Filter {
  @Override public HttpResponse doFilter(HttpRequest req, FilterChain chain) throws Exception {
    HttpResponse res = chain.doFilter(req);
    if ("HEAD".equals(req.method())) {
      HttpResponse.Builder builder = HttpResponse.builder(res.statusCode(), res.reasonPhrase());
      res.headers().forEach(builder::header);
      builder.body(new byte[0]);
      return builder.build();
    }
    return res;
  }
}


// HEAD 요청이면 본문 없이 응답해야 하니까 여기서 비워서 반환한다.
