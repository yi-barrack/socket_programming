package server.app;

import server.app.handlers.LoginGetHandler;
import server.app.handlers.LoginPostHandler;
import server.route.RouteRegistry;

/**
 * 애플리케이션 전용 라우트 등록을 담당.
 * 추후 다른 기능(회원가입, 게시판 등)을 추가할 때 이 클래스를 수정하거나 새 메소드를 추가하면 된다.
 */
public final class AppRoutes {

    private AppRoutes() {}

    public static RouteRegistry createDefaultRoutes() {
        RouteRegistry registry = new RouteRegistry();

        // 로그인 화면 및 제출 처리 라우트 예시
        registry.register("GET", "/login", new LoginGetHandler());
        registry.register("POST", "/login", new LoginPostHandler());

        return registry;
    }
}
