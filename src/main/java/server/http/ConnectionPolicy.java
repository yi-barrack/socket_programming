package server.http;

import server.config.ServerConfig;

// HTTP 버전/헤더를 보고 keep-alive를 유지할지 말지 정하는 헬퍼
public final class ConnectionPolicy {

    public boolean shouldKeepAlive(HttpRequest request, int requestsServed) {
        String version = request.version();
        String connectionHeader = request.header("connection");
        if ("HTTP/1.1".equals(version)) {
            if (connectionHeader != null && "close".equalsIgnoreCase(connectionHeader)) {
                return false;
            }
            return requestsServed < ServerConfig.KEEP_ALIVE_MAX_REQUESTS;
        }
        if ("HTTP/1.0".equals(version)) {
            return connectionHeader != null
                    && "keep-alive".equalsIgnoreCase(connectionHeader)
                    && requestsServed < ServerConfig.KEEP_ALIVE_MAX_REQUESTS;
        }
        return false;
    }

    public void applyResponseHeaders(HttpResponse.Builder builder, boolean keepAlive) {
        if (keepAlive) {
            // 계속 연결 유지할 거면 Connection/Keep-Alive 헤더를 직접 적어준다.
            builder.header("Connection", "keep-alive");
            builder.header("Keep-Alive", "timeout=" + (ServerConfig.KEEP_ALIVE_TIMEOUT_MILLIS / 1000)
                    + ", max=" + ServerConfig.KEEP_ALIVE_MAX_REQUESTS);
        } else {
            builder.header("Connection", "close");
        }
    }
}
