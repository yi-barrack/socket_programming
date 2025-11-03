package server.core;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import server.config.ServerConfig;
import server.filter.Filter;
import server.filter.FilterChain;
import server.http.ConnectionPolicy;
import server.http.HttpParseException;
import server.http.HttpRequest;
import server.http.HttpRequestParser;
import server.http.HttpResponse;
import server.http.HttpResponseWriter;
import server.route.Router;
import server.util.Logger;

// 하나의 소켓을 맡아서 HTTP 요청을 읽고 응답까지 보내는 워커 스레드
public final class ConnectionWorker implements Runnable {

    private final Socket socket;
    private final Router router;
    private final List<Filter> filters;
    private final HttpRequestParser parser;
    private final ConnectionPolicy policy;
    private final HttpResponseWriter writer;

    public ConnectionWorker(Socket socket, Router router, List<Filter> filters) {
        this.socket = socket;
        this.router = router;
        this.filters = filters;
        this.parser = new HttpRequestParser();
        this.policy = new ConnectionPolicy();
        this.writer = new HttpResponseWriter();
    }

    @Override
    public void run() {
        try (Socket s = socket) {
            // 아무것도 안 오는 연결이 너무 오래 붙어있지 않게 타임아웃을 건다.
            s.setSoTimeout(ServerConfig.SOCKET_TIMEOUT_MILLIS);
            BufferedInputStream in = new BufferedInputStream(s.getInputStream());
            OutputStream out = s.getOutputStream();
            int handledRequests = 0;
            boolean keepAlive;
            do {
                HttpRequest request;
                try {
                    // 요청 라인 -> 헤더 -> 바디 순으로 읽어온다.
                    request = parser.parse(in);
                } catch (SocketTimeoutException e) {
                    Logger.warn("Socket timeout from " + s.getRemoteSocketAddress());
                    break;
                } catch (HttpParseException e) {
                    Logger.warn("Bad request from " + s.getRemoteSocketAddress() + ": " + e.getMessage());
                    // 이미 연결이 끊어졌으면 굳이 에러 응답 안 보낸다.
                    if (!s.isOutputShutdown() && !s.isClosed()) {
                        sendError(out, 400, "Bad Request", e.getMessage());
                    } else {
                        Logger.warn("클라이언트가 연결을 끊어 오류 응답을 생략합니다.");
                    }
                    break;
                }
                if (request == null) {
                    // 읽을 게 없으면(연결 종료) 루프 탈출
                    break;
                }

                HttpResponse response;
                try {
                    // 필터 체인 태워서 실제 핸들러까지 실행한다.
                    FilterChain chain = new FilterChain(filters, router);
                    response = chain.doFilter(request);
                } catch (Exception e) {
                    Logger.error("Handler failure", e);
                    response = HttpResponse.builder(500, "Internal Server Error")
                            .header("Content-Type", "text/plain; charset=UTF-8")
                            .body("Internal Server Error".getBytes(StandardCharsets.UTF_8))
                            .build();
                }
                handledRequests++;
                keepAlive = policy.shouldKeepAlive(request, handledRequests);
                // 기존 응답에 keep-alive 헤더 같은 것들을 덧붙인다.
                HttpResponse.Builder builder = HttpResponse.builder(response.statusCode(), response.reasonPhrase());
                response.headers().forEach(builder::header);
                builder.body(response.body());
                policy.applyResponseHeaders(builder, keepAlive);
                HttpResponse finalResponse = builder.build();
                boolean includeBody = !"HEAD".equalsIgnoreCase(request.method());
                writer.write(out, finalResponse, includeBody);
            } while (keepAlive);
        } catch (IOException e) {
            Logger.error("IO error on connection", e);
        }
    }

    private void sendError(OutputStream out, int status, String reason, String message) {
        try {
            // 간단한 텍스트 에러 응답 만들어서 내려준다.
            HttpResponse response = HttpResponse.builder(status, reason)
                    .header("Content-Type", "text/plain; charset=UTF-8")
                    .body(message.getBytes(StandardCharsets.UTF_8))
                    .build();
            writer.write(out, response, true);
        } catch (IOException ioe) {
            if (ioe instanceof SocketException) {
                Logger.warn("오류 응답을 보내기 전에 클라이언트 연결이 종료되었습니다: " + ioe.getMessage());
            } else {
                Logger.error("Failed to send error response", ioe);
            }
        }
    }
}
