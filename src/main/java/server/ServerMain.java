package server;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

import server.config.ServerConfig;
import server.core.NetAcceptor;
import server.filter.BodyLimitFilter;
import server.filter.ContentTypeFilter;
import server.filter.ExceptionMappingFilter;
import server.filter.Filter;
import server.filter.HeadFilter;
import server.filter.LoggingFilter;
import server.filter.PathTraversalFilter;
import server.filter.SessionFilter;
import server.route.AuthHandler;
import server.route.PostCreationHandler;
import server.route.PostDeleteHandler;
import server.route.PostListHandler;
import server.route.RoutedPostHandler;
import server.route.Router;
import server.route.SimplePostHandler;
import server.route.StaticFileHandler;
import server.service.PostService;
import server.util.Logger;

// 서버 전체를 켜고 끄는 메인 클래스. 여기서 필요한 것들 전부 묶어서 실행한다.
public final class ServerMain {

    public static void main(String[] args) {
        ensureWebRoot(); // 기본 www 폴더랑 index.html 챙김
        // 정적 파일 핸들러 만들기. www 아래 파일을 그대로 내려준다.
        StaticFileHandler staticHandler = new StaticFileHandler(ServerConfig.WEB_ROOT);
        // POST 요청 기본 동작은 여기로 보낸다. 필요하면 다른 경로로 덮어쓴다.
        SimplePostHandler defaultPostHandler = new SimplePostHandler();
        // 로그인, 회원가입, 로그아웃을 처리하는 핸들러
        AuthHandler authHandler = new AuthHandler();
        // 게시판 저장/삭제 담당 서비스
        PostService postService = new PostService();
        PostCreationHandler postCreationHandler = new PostCreationHandler(postService);
        PostDeleteHandler postDeleteHandler = new PostDeleteHandler(postService);
        PostListHandler postListHandler = new PostListHandler(postService);

        // POST 라우팅 테이블. 기본 핸들러에 없는 경로는 여기서 덮어쓴다.
        RoutedPostHandler routedPostHandler = new RoutedPostHandler(defaultPostHandler);
        routedPostHandler.register("/login", authHandler);
        routedPostHandler.register("/register", authHandler);
        routedPostHandler.register("/logout", authHandler);
        routedPostHandler.register("/posts/create", postCreationHandler);
        routedPostHandler.register("/posts/delete", postDeleteHandler);
        routedPostHandler.register("/posts/list", postListHandler);

        // 최종 라우터. GET/HEAD는 정적 파일, POST는 위에서 만든 테이블로 간다.
        Router router = new Router(staticHandler, routedPostHandler);
        // 요청이 들어올 때마다 순서대로 돌릴 필터 목록
        List<Filter> filters = List.of(
                new LoggingFilter(),
                new ExceptionMappingFilter("/"),
                new SessionFilter(
                        Set.of("/login", "/login.html", "/register", "/register.html"),
                        Set.of("/login", "/register"),
                        "/login.html"
                ),
                new BodyLimitFilter(ServerConfig.MAX_BODY_SIZE, "/"),
                ContentTypeFilter.withDefaults("/"),
                new PathTraversalFilter(ServerConfig.WEB_ROOT, "/"),
                new HeadFilter()
        );
        // NetAcceptor가 실질적으로 소켓 열고 워커 스레드 돌린다.
        NetAcceptor acceptor = new NetAcceptor(router, filters);
        // 프로그램이 꺼질 때 정리 작업 하도록 훅 추가
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                acceptor.stop();
            } catch (IOException e) {
                Logger.error("Error during shutdown", e);
            }
        }));
        try {
            acceptor.start();
        } catch (IOException e) {
            Logger.error("Failed to start server", e);
        }
    }

    // www 폴더가 없으면 만들어주고, 기본 index.html 도 같이 만들어준다.
    private static void ensureWebRoot() {
        try {
            Files.createDirectories(ServerConfig.WEB_ROOT);
            var index = ServerConfig.WEB_ROOT.resolve("index.html");
            if (Files.notExists(index)) {
                Files.writeString(index, """
                        <!DOCTYPE html>
                        <html>
                        <head>
                          <meta charset="UTF-8">
                          <title>Simple Java Server</title>
                        </head>
                        <body>
                          <h1>Simple Java Server</h1>
                          <p>Your server is up and serving files from the <code>www</code> directory.</p>
                        </body>
                        </html>
                        """);
            }
        } catch (IOException e) {
            Logger.error("Could not create web root directory", e);
        }
    }
}
