package server.filter;

import server.http.HttpRequest;
import server.http.HttpResponse;
import java.nio.file.Path;

import static server.http.ErrorResponses.*;

// ../../ 같은 경로 탈출 시도를 막는 필터
public final class PathTraversalFilter implements Filter {
  private final Path webRoot;
  private final String home;

  public PathTraversalFilter(Path webRoot, String home) {
    this.webRoot = webRoot.normalize().toAbsolutePath();
    this.home = home;
  }

  @Override public HttpResponse doFilter(HttpRequest req, FilterChain chain) throws Exception {
    String path = req.path();
    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    // 요청 경로를 실제 파일 경로로 바꿔보고, webRoot 밖으로 나가면 바로 차단
    Path resolved = webRoot.resolve(path.isEmpty() ? "." : path).normalize();
    if (!resolved.startsWith(webRoot)) {
      return forbiddenAlert(req, "잘못된 요청입니다.", home);
    }
    return chain.doFilter(req);
  }
}


// 항상 웹 루트 아래로만 접근하도록 지켜주는 안전장치라고 보면 된다.
