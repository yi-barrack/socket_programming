package server.core;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import server.config.ServerConfig;
import server.filter.Filter;
import server.route.Router;
import server.util.Logger;
import server.util.SslContextProvider;

// 새 연결을 받아서 워커 스레드에게 던지는 역할. 소켓 열고 닫는 것도 여기서 한다.
public final class NetAcceptor implements Closeable {
    private final Router router;
    private final List<Filter> filters;
    private volatile boolean running;
    private ExecutorService executor;
    private ServerSocket serverSocket;

    public NetAcceptor(Router router, List<Filter> filters) {
        this.router = router;
        this.filters = List.copyOf(filters);
    }

    public void start() throws IOException {
        // 여러 번 start() 호출되지 않게 동기화
        synchronized (this) {
            if (running) {
                return;
            }
            // 워커 스레드 만들고 소켓을 연다.
            executor = Executors.newFixedThreadPool(ServerConfig.WORKER_THREADS);
            serverSocket = createServerSocket();
            serverSocket.setSoTimeout(1000);
            running = true;
        }
        Logger.info((ServerConfig.HTTPS_ENABLED ? "HTTPS" : "HTTP") + " server listening on port " + ServerConfig.PORT);
        try {
            while (isRunning()) {
                try {
                    // 새 연결이 오면 ConnectionWorker에게 넘겨 처리.
                    Socket socket = serverSocket.accept();
                    executor.execute(new ConnectionWorker(socket, router, filters));
                } catch (SocketTimeoutException e) {
                    // 1초마다 빠져나와서 running 상태 체크
                } catch (IOException e) {
                    if (isRunning()) {
                        Logger.error("Accept failed", e);
                    }
                }
            }
        } finally {
            cleanup();
        }
    }

    private boolean isRunning() {
        return running;
    }

    @Override
    public void close() throws IOException {
        stop();
    }

    public void stop() throws IOException {
        synchronized (this) {
            if (!running) {
                return;
            }
            // 더 이상 새 연결 받지 않도록 플래그 내려주고 소켓 닫기
            running = false;
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        }
        cleanup();
        Logger.info("Server stopped");
    }

    private void cleanup() {
        // 스레드풀 정리. 깔끔히 끝나면 좋고 아니면 강제 종료.
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                executor.shutdownNow();
            }
            executor = null;
        }
        serverSocket = null;
    }

    private ServerSocket createServerSocket() throws IOException {
        if (!ServerConfig.HTTPS_ENABLED) {
            // 일반 HTTP 모드일 때 사용하는 ServerSocket
            ServerSocket socket = new ServerSocket();
            socket.bind(new InetSocketAddress(ServerConfig.PORT), ServerConfig.ACCEPT_BACKLOG);
            return socket;
        }
        // HTTPS 모드면 키스토어로부터 SSL 소켓을 만든다.
        SSLServerSocketFactory factory = SslContextProvider.serverSocketFactory();
        SSLServerSocket socket = (SSLServerSocket) factory.createServerSocket(ServerConfig.PORT, ServerConfig.ACCEPT_BACKLOG);
        socket.setEnabledProtocols(ServerConfig.ENABLED_PROTOCOLS);
        socket.setNeedClientAuth(false);
        return socket;
    }
}
