package server.filter;

import server.http.HttpRequest;
import server.http.HttpResponse;
import server.http.HttpParseException;

import static server.http.ErrorResponses.*;

// 핸들러에서 터지는 예외를 잡아 사용자 친화적인 에러 페이지로 바꿔준다.
public final class ExceptionMappingFilter implements Filter {
  private final String home;
  public ExceptionMappingFilter(String home) { this.home = home; }

  @Override public HttpResponse doFilter(HttpRequest req, FilterChain chain) {
    try {
      // 다음 필터나 핸들러로 넘겨본다.
      return chain.doFilter(req);
    } catch (HttpParseException e) {
      return badRequestAlert(req, "요청 구문이 올바르지 않습니다.", home);
    } catch (SecurityException e) {
      return forbiddenAlert(req, "접근이 허용되지 않았습니다.", home);
    } catch (UnsupportedOperationException e) {
      return serverErrorAlert(req, "아직 지원하지 않습니다.", home);
    } catch (Throwable t) {
      return serverErrorAlert(req, "서버 오류가 발생했습니다.", home);
    }
  }
}
